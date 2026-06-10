package com.haapyProcess.domain.weather.service;

import com.haapyProcess.domain.region.entity.Region;
import com.haapyProcess.domain.region.repository.RegionRepository;
import com.haapyProcess.domain.weather.dto.*;
import com.haapyProcess.global.exception.CustomException;
import com.haapyProcess.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.DefaultUriBuilderFactory;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
@Service
@RequiredArgsConstructor
public class WeatherService {

    private final RestClient restClient;
    private final RegionRepository regionRepository;

    @Value("${weather.api.key}")
    private String apiKey;

    @Value("${weather.api.urls.pollen}")
    private String pollenBaseUrl;

    @Value("${weather.api.urls.air-pollution}")
    private String airPollutionUrl;

    @Value("${weather.api.urls.forecast}")
    private String forecastUrl;

    @Value("${weather.api.urls.living}")
    private String livingBaseUrl;

    // 외부 기상 API 4종을 동시에 호출하기 위한 전용 스레드풀
    private final ExecutorService weatherExecutor = Executors.newFixedThreadPool(8);

    // 꽃가루/자외선 폴백 후보(동/구/시)를 병렬 조회하기 위한 별도 풀 (weatherExecutor와 분리하여 중첩 고갈 방지)
    private final ExecutorService fallbackExecutor = Executors.newFixedThreadPool(8);

    // areaNo별 통합 기상 데이터 TTL 캐시 (외부 API가 느리고 데이터는 분 단위로만 바뀌므로 단기 캐시)
    private static final long CACHE_TTL_MILLIS = 5 * 60 * 1000L;
    private final Map<String, CacheEntry> weatherCache = new ConcurrentHashMap<>();

    private record CacheEntry(WeatherResponseDto data, long expiresAt) {
        boolean isValid() {
            return System.currentTimeMillis() < expiresAt;
        }
    }

    /**
     * 프론트엔드 제공용 통합 기상 데이터 조회 (단기예보, 미세먼지, 꽃가루, 자외선).
     * 외부 API 4종을 병렬 호출하고, 결과를 areaNo별로 단기 캐시한다.
     */
    public WeatherResponseDto getCombinedWeatherData(String areaNo) {
        CacheEntry cached = weatherCache.get(areaNo);
        if (cached != null && cached.isValid()) {
            return cached.data();
        }

        Region region = regionRepository.findById(areaNo)
                .orElseThrow(() -> new CustomException(ErrorCode.INVALID_CITY));

        log.info("통합 기상 정보 수집 요청 - 지역명: {}, 행정구역코드: {}", region.getDong(), areaNo);

        // 1~4. 서로 독립적인 외부 API 호출을 병렬로 실행 (순차 합산 지연 → 최대값 수준으로 단축)
        CompletableFuture<List<WeatherHourlyDto>> forecastFuture = CompletableFuture.supplyAsync(() -> {
            try {
                return getHourlyForecastList(region.getNx(), region.getNy());
            } catch (Exception e) {
                log.warn("단기예보 수집 실패 (nx:{}, ny:{}): {}", region.getNx(), region.getNy(), e.getMessage());
                return new ArrayList<WeatherHourlyDto>();
            }
        }, weatherExecutor);

        CompletableFuture<AirKoreaItemDto> dustFuture = CompletableFuture.supplyAsync(() -> {
            try {
                if (region.getStationName() != null && !region.getStationName().isBlank()) {
                    return getRealTimeFineDust(region.getStationName());
                }
                log.warn("미세먼지 수집 보류: DB에 측정소명(stationName)이 존재하지 않습니다. (areaNo: {})", areaNo);
            } catch (Exception e) {
                log.warn("미세먼지 API 통신 실패: {}", e.getMessage());
            }
            return null;
        }, weatherExecutor);

        CompletableFuture<String> pollenFuture = CompletableFuture.supplyAsync(() -> {
            try {
                return getPinePollenRiskWithFallback(areaNo);
            } catch (Exception e) {
                log.warn("꽃가루 API 통신 실패: {}", e.getMessage());
                return "0";
            }
        }, weatherExecutor);

        CompletableFuture<String> uvFuture = CompletableFuture.supplyAsync(() -> {
            try {
                return getUvRiskWithFallback(areaNo);
            } catch (Exception e) {
                log.warn("자외선 API 통신 실패: {}", e.getMessage());
                return "0";
            }
        }, weatherExecutor);

        CompletableFuture.allOf(forecastFuture, dustFuture, pollenFuture, uvFuture).join();

        List<WeatherHourlyDto> hourlyList = forecastFuture.join();
        AirKoreaItemDto dust = dustFuture.join();
        String pollen = pollenFuture.join();
        String uv = uvFuture.join();

        WeatherHourlyDto currentForecast = hourlyList.isEmpty() ?
                WeatherHourlyDto.builder().temperature("-").condition("알 수 없음").build() : hourlyList.get(0);

        WeatherResponseDto result = WeatherResponseDto.builder()
                .regionName(region.getDong())
                .temperature(currentForecast.getTemperature())
                .humidity(currentForecast.getHumidity())
                .weatherCondition(currentForecast.getCondition())
                .pm10Value(dust != null ? dust.getPm10Value() : "-")
                .pm10Grade(dust != null ? dust.getPm10Grade() : "-")
                .pm25Value(dust != null ? dust.getPm25Value() : "-")
                .pm25Grade(dust != null ? dust.getPm25Grade() : "-")
                .pollenRiskLevel(pollen)
                .uvRiskLevel(uv)
                .hourlyForecasts(hourlyList)
                .build();

        weatherCache.put(areaNo, new CacheEntry(result, System.currentTimeMillis() + CACHE_TTL_MILLIS));
        return result;
    }

    /**
     * 기상청 단기예보 API 호출 (향후 6시간 데이터 추출)
     */
    private List<WeatherHourlyDto> getHourlyForecastList(String nx, String ny) {
        String[] baseDateTime = calculateForecastBaseTime();
        DefaultUriBuilderFactory factory = new DefaultUriBuilderFactory(forecastUrl);
        factory.setEncodingMode(DefaultUriBuilderFactory.EncodingMode.VALUES_ONLY);

        try {
            PublicDataResponse<KmaForecastItemDto> response = restClient.mutate().uriBuilderFactory(factory).build()
                    .get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/getVilageFcst")
                            .queryParam("serviceKey", apiKey)
                            .queryParam("pageNo", 1)
                            .queryParam("numOfRows", 100)
                            .queryParam("dataType", "JSON")
                            .queryParam("base_date", baseDateTime[0])
                            .queryParam("base_time", baseDateTime[1])
                            .queryParam("nx", nx)
                            .queryParam("ny", ny)
                            .build())
                    .retrieve()
                    .body(new ParameterizedTypeReference<PublicDataResponse<KmaForecastItemDto>>() {});

            validatePublicDataResponse(response);
            List<KmaForecastItemDto> items = response.getResponse().getBody().getItems().getItem();

            if (items == null || items.isEmpty()) {
                return new ArrayList<>();
            }
            return parseHourlyWeatherData(items);

        } catch (Exception e) {
            throw new CustomException(ErrorCode.EXTERNAL_API_ERROR);
        }
    }

    /**
     * 에어코리아 실시간 측정소별 미세먼지 API 호출
     */
    private AirKoreaItemDto getRealTimeFineDust(String stationName) {
        try {
            String encodedStationName = URLEncoder.encode(stationName, StandardCharsets.UTF_8);
            String directUrl = airPollutionUrl + "/getMsrstnAcctoRltmMesureDnsty"
                    + "?serviceKey=" + apiKey + "&returnType=json&numOfRows=1&pageNo=1&stationName="
                    + encodedStationName + "&dataTerm=DAILY&ver=1.4";

            AirKoreaRoot rootResponse = restClient.get()
                    .uri(URI.create(directUrl))
                    .retrieve()
                    .body(AirKoreaRoot.class);

            if (rootResponse == null || rootResponse.response() == null) return null;
            if (!"00".equals(rootResponse.response().header().resultCode())) return null;

            List<AirKoreaItemDto> items = rootResponse.response().body().items();
            if (items == null || items.isEmpty() || !items.get(0).isDataValid()) return null;

            return items.get(0);

        } catch (Exception e) {
            throw new CustomException(ErrorCode.EXTERNAL_API_ERROR);
        }
    }

    /**
     * 소나무 꽃가루 위험도 API 호출 (동/구/시 후보를 병렬 조회 후 가장 구체적인 결과 채택)
     */
    private String getPinePollenRiskWithFallback(String originalAreaNo) {
        return getLivingIndexInParallel(pollenBaseUrl, "/getPinePollenRiskIdxV3", originalAreaNo, "꽃가루");
    }

    /**
     * 자외선 지수 API 호출 (동/구/시 후보를 병렬 조회 후 가장 구체적인 결과 채택)
     */
    private String getUvRiskWithFallback(String originalAreaNo) {
        return getLivingIndexInParallel(livingBaseUrl, "/getUVIdxV4", originalAreaNo, "자외선");
    }

    /**
     * 생활기상지수(꽃가루/자외선) 폴백 후보(동->구->시)를 순차가 아닌 병렬로 조회하고,
     * 데이터가 있는 가장 구체적인 지역(동 우선)의 값을 반환한다.
     */
    private String getLivingIndexInParallel(String baseUrl, String path, String originalAreaNo, String label) {
        String baseTime = calculateLivingBaseTime();
        DefaultUriBuilderFactory factory = new DefaultUriBuilderFactory(baseUrl);
        factory.setEncodingMode(DefaultUriBuilderFactory.EncodingMode.VALUES_ONLY);
        RestClient client = restClient.mutate().uriBuilderFactory(factory).build();

        String[] targetCodes = generateFallbackCodes(originalAreaNo); // [동, 구, 시] 우선순위

        List<CompletableFuture<String>> futures = new ArrayList<>();
        for (String areaNo : targetCodes) {
            futures.add(CompletableFuture.supplyAsync(
                    () -> fetchLivingIndexForCode(client, path, areaNo, baseTime, label), fallbackExecutor));
        }
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

        // 우선순위(동->구->시) 순으로 값이 있는 첫 결과 채택
        for (CompletableFuture<String> f : futures) {
            String value = f.join();
            if (value != null) {
                return value;
            }
        }
        return "0";
    }

    /** 단일 지역코드에 대한 생활기상지수 조회. 데이터 없으면 null. */
    private String fetchLivingIndexForCode(RestClient client, String path, String areaNo, String baseTime, String label) {
        try {
            PublicDataResponse<PollenItemDto> response = client.get()
                    .uri(uriBuilder -> uriBuilder
                            .path(path)
                            .queryParam("serviceKey", apiKey)
                            .queryParam("pageNo", 1)
                            .queryParam("numOfRows", 10)
                            .queryParam("dataType", "JSON")
                            .queryParam("areaNo", areaNo)
                            .queryParam("time", baseTime)
                            .build())
                    .retrieve()
                    .body(new ParameterizedTypeReference<PublicDataResponse<PollenItemDto>>() {});

            if (response == null || response.getResponse() == null || response.getResponse().getBody() == null) return null;

            List<PollenItemDto> items = response.getResponse().getBody().getItems().getItem();
            if (items == null || items.isEmpty()) return null;

            return extractCurrentLivingIndex(items.get(0), baseTime);
        } catch (Exception e) {
            log.debug("{} API 탐색 실패 (areaNo: {})", label, areaNo);
            return null;
        }
    }

    private String extractCurrentLivingIndex(PollenItemDto item, String baseTimeStr) {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHH");
            LocalDateTime baseTime = LocalDateTime.parse(baseTimeStr, formatter);
            LocalDateTime now = LocalDateTime.now();

            long diffHours = ChronoUnit.HOURS.between(baseTime, now);

            String result;
            if (diffHours < 3) result = item.getH0();
            else if (diffHours < 6) result = item.getH3();
            else if (diffHours < 9) result = item.getH6();
            else if (diffHours < 12) result = item.getH9();
            else result = item.getH12();

            return (result != null && !result.trim().isEmpty()) ? result : "0";
        } catch (Exception e) {
            return "0";
        }
    }

    /**
     * 기상청 데이터 파싱 및 시간순 정렬
     */
    private List<WeatherHourlyDto> parseHourlyWeatherData(List<KmaForecastItemDto> items) {
        Map<String, WeatherHourlyDto> timelineMap = new LinkedHashMap<>();

        for (KmaForecastItemDto item : items) {
            String dateTimeKey = item.getFcstDate() + item.getFcstTime();

            WeatherHourlyDto dto = timelineMap.getOrDefault(dateTimeKey, WeatherHourlyDto.builder()
                    .time(item.getFcstTime())
                    .sky("1")
                    .pty("0")
                    .build());

            switch (item.getCategory()) {
                case "TMP": dto.setTemperature(item.getFcstValue()); break;
                case "SKY": dto.setSky(item.getFcstValue()); break;
                case "PTY": dto.setPty(item.getFcstValue()); break;
                case "REH": dto.setHumidity(item.getFcstValue()); break;
            }

            if (timelineMap.size() == 6 && !timelineMap.containsKey(dateTimeKey)) {
                break;
            }
            timelineMap.put(dateTimeKey, dto);
        }

        List<WeatherHourlyDto> resultList = new ArrayList<>(timelineMap.values());
        for (WeatherHourlyDto dto : resultList) {
            String condition;
            if (!"0".equals(dto.getPty())) {
                condition = switch (dto.getPty()) {
                    case "1", "4" -> "비";
                    case "2" -> "비/눈";
                    case "3" -> "눈";
                    default -> "비";
                };
            } else {
                condition = switch (dto.getSky()) {
                    case "1" -> "맑음";
                    case "3" -> "구름많음";
                    case "4" -> "흐림";
                    default -> "맑음";
                };
            }
            dto.setCondition(condition);
        }
        return resultList;
    }

    /**
     * 단기예보 발표 기준 시간(BaseTime) 계산
     */
    private String[] calculateForecastBaseTime() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime safeTime = now.minusMinutes(30);

        int hour = safeTime.getHour();
        String baseDate = safeTime.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String baseTime;

        if (hour < 2) {
            baseDate = safeTime.minusDays(1).format(DateTimeFormatter.ofPattern("yyyyMMdd"));
            baseTime = "2300";
        } else if (hour < 5) baseTime = "0200";
        else if (hour < 8) baseTime = "0500";
        else if (hour < 11) baseTime = "0800";
        else if (hour < 14) baseTime = "1100";
        else if (hour < 17) baseTime = "1400";
        else if (hour < 20) baseTime = "1700";
        else if (hour < 23) baseTime = "2000";
        else baseTime = "2300";

        return new String[]{baseDate, baseTime};
    }

    /**
     * 생활기상지수 발표 기준 시간(BaseTime) 계산
     */
    private String calculateLivingBaseTime() {
        LocalDateTime now = LocalDateTime.now();
        int hour = now.getHour();
        LocalDateTime targetTime;

        if (hour < 6) {
            targetTime = now.minusDays(1).withHour(18);
        } else if (hour < 18) {
            targetTime = now.withHour(6);
        } else {
            targetTime = now.withHour(18);
        }
        return targetTime.format(DateTimeFormatter.ofPattern("yyyyMMddHH"));
    }

    /**
     * 공공데이터포털 공통 응답 에러 검증
     */
    private void validatePublicDataResponse(PublicDataResponse<?> response) {
        if (response == null || response.getResponse() == null || response.getResponse().getHeader() == null) {
            log.error("외부 API 응답이 Null입니다.");
            throw new CustomException(ErrorCode.EXTERNAL_API_ERROR);
        }

        String resultCode = response.getResponse().getHeader().getResultCode();
        String resultMsg = response.getResponse().getHeader().getResultMsg();

        if (!"00".equals(resultCode)) {
            log.error("공공데이터 서버 에러 - 코드: {}, 메시지: {}", resultCode, resultMsg);
            throw new CustomException(ErrorCode.EXTERNAL_API_ERROR);
        }
    }

    record AirKoreaRoot(Response response) {
        record Response(Header header, Body body) {}
        record Header(String resultCode, String resultMsg) {}
        record Body(List<AirKoreaItemDto> items) {}
    }

    /**
     * [리팩토링] 지역 코드 Fallback 배열 생성기 (동 -> 구 -> 시)
     */
    private String[] generateFallbackCodes(String areaNo) {
        String sigunguCode = areaNo.substring(0, 5) + "00000";
        String sidoCode = areaNo.substring(0, 2) + "00000000";
        return new String[]{areaNo, sigunguCode, sidoCode};
    }
}