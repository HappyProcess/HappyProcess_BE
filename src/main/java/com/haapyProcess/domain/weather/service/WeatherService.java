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
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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

    /**
     * [1] 최종 통합 기능: 프론트엔드 호출용 메인 로직
     */
    public WeatherResponseDto getCombinedWeatherData(String areaNo) {

        // 💥 1. DB에서 프론트가 넘겨준 코드로 지역의 모든 정보(nx, ny, 측정소명)를 꺼내옵니다.
        Region region = regionRepository.findById(areaNo)
                .orElseThrow(() -> new CustomException(ErrorCode.INVALID_CITY));

        log.info("통합 기상 정보 수집 시작 - 동: {}, 측정소: {}", region.getDong(), region.getStationName());

        // 2. 단기예보 (DB에서 꺼낸 nx, ny 문자열을 바로 넘깁니다!)
        List<WeatherHourlyDto> hourlyList = new ArrayList<>();
        try {
            hourlyList = getHourlyForecastList(region.getNx(), region.getNy());
        } catch (Exception e) { log.warn("단기예보 수집 실패: {}", e.getMessage()); }

        WeatherHourlyDto currentForecast = hourlyList.isEmpty() ?
                WeatherHourlyDto.builder().temperature("-").condition("알 수 없음").build() : hourlyList.get(0);

        // 3. 미세먼지 (DB에서 꺼낸 측정소명을 바로 넘깁니다!)
        AirKoreaItemDto dust = null;
        try {
            if (region.getStationName() != null) {
                dust = getRealTimeFineDust(region.getStationName());
            }
        } catch (Exception e) { log.warn("미세먼지 수집 실패: {}", e.getMessage()); }

        // 4. 소나무 꽃가루
        String pollen = "0";
        try {
            pollen = getPinePollenRiskWithFallback(areaNo);
        } catch (Exception e) { log.warn("꽃가루 수집 실패: {}", e.getMessage()); }

        // 5. 자외선
        String uv = "0";
        try {
            uv = getUvRiskWithFallback(areaNo);
        } catch (Exception e) { log.warn("자외선 수집 실패: {}", e.getMessage()); }

        return WeatherResponseDto.builder()
                .regionName(region.getDong())
                .temperature(currentForecast.getTemperature())
                .humidity("-")
                .weatherCondition(currentForecast.getCondition())
                .pm10Value(dust != null ? dust.getPm10Value() : "-")
                // ... (다른 dust 값들 추가 가능)
                .pollenRiskLevel(pollen)
                .uvRiskLevel(uv)
                .hourlyForecasts(hourlyList)
                .build();
    }

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
                            .queryParam("nx", nx) // DB 값 적용
                            .queryParam("ny", ny) // DB 값 적용
                            .build())
                    .retrieve()
                    // ... (기존 예외처리 및 응답 매핑 로직 유지)
                    .body(new ParameterizedTypeReference<PublicDataResponse<KmaForecastItemDto>>() {});

            validatePublicDataResponse(response);
            List<KmaForecastItemDto> items = response.getResponse().getBody().getItems().getItem();
            if (items == null || items.isEmpty()) return new ArrayList<>();
            return parseHourlyWeatherData(items);
        } catch (Exception e) {
            throw new CustomException(ErrorCode.EXTERNAL_API_ERROR);
        }
    }

    private AirKoreaItemDto getRealTimeFineDust(String stationName) {
        // 기존과 완벽히 동일하므로 그대로 둡니다.
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

    private String getPinePollenRiskWithFallback(String originalAreaNo) {
        String baseTime = calculateLivingBaseTime();
        DefaultUriBuilderFactory factory = new DefaultUriBuilderFactory(pollenBaseUrl);
        factory.setEncodingMode(DefaultUriBuilderFactory.EncodingMode.VALUES_ONLY);

        String sigunguCode = originalAreaNo.substring(0, 5) + "00000";
        String sidoCode = originalAreaNo.substring(0, 2) + "00000000";
        String[] targetCodes = {originalAreaNo, sigunguCode, sidoCode};

        for (String areaNo : targetCodes) {
            try {
                PublicDataResponse<PollenItemDto> response = restClient.mutate().uriBuilderFactory(factory).build()
                        .get()
                        .uri(uriBuilder -> uriBuilder
                                .path("/getPinePollenRiskIdxV3")
                                .queryParam("serviceKey", apiKey)
                                .queryParam("pageNo", 1)
                                .queryParam("numOfRows", 10)
                                .queryParam("dataType", "JSON")
                                .queryParam("areaNo", areaNo)
                                .queryParam("time", baseTime)
                                .build())
                        .retrieve()
                        .body(new ParameterizedTypeReference<PublicDataResponse<PollenItemDto>>() {});

                if (response == null || response.getResponse() == null || response.getResponse().getBody() == null) continue;
                List<PollenItemDto> items = response.getResponse().getBody().getItems().getItem();
                if (items == null || items.isEmpty()) continue;

                String todayValue = items.get(0).getToday();
                if (todayValue != null && !todayValue.trim().isEmpty()) {
                    return todayValue; // 성공하면 즉시 반환!
                }
            } catch (Exception e) {
                // 실패하면 무시하고 다음 넓은 지역으로 루프를 돕니다.
            }
        }
        return "0"; // 3단계 다 찔러봐도 없으면 0
    }

    private String getUvRiskWithFallback(String originalAreaNo) {
        String baseTime = calculateLivingBaseTime();
        DefaultUriBuilderFactory factory = new DefaultUriBuilderFactory(livingBaseUrl);
        factory.setEncodingMode(DefaultUriBuilderFactory.EncodingMode.VALUES_ONLY);

        String sigunguCode = originalAreaNo.substring(0, 5) + "00000";
        String sidoCode = originalAreaNo.substring(0, 2) + "00000000";
        String[] targetCodes = {originalAreaNo, sigunguCode, sidoCode};

        for (String areaNo : targetCodes) {
            try {
                PublicDataResponse<PollenItemDto> response = restClient.mutate().uriBuilderFactory(factory).build()
                        .get()
                        .uri(uriBuilder -> uriBuilder
                                .path("/getUVIdxV4")
                                .queryParam("serviceKey", apiKey)
                                .queryParam("pageNo", 1)
                                .queryParam("numOfRows", 10)
                                .queryParam("dataType", "JSON")
                                .queryParam("areaNo", areaNo)
                                .queryParam("time", baseTime)
                                .build())
                        .retrieve()
                        .body(new ParameterizedTypeReference<PublicDataResponse<PollenItemDto>>() {});

                if (response == null || response.getResponse() == null || response.getResponse().getBody() == null) continue;
                List<PollenItemDto> items = response.getResponse().getBody().getItems().getItem();
                if (items == null || items.isEmpty()) continue;

                String todayValue = items.get(0).getToday();
                if (todayValue != null && !todayValue.trim().isEmpty()) {
                    return todayValue;
                }
            } catch (Exception e) {
            }
        }
        return "0";
    }

    /**
     * =====================================================================
     * [3] 공통 헬퍼 메서드 (데이터 파싱 및 시간 계산)
     * =====================================================================
     */

    private List<WeatherHourlyDto> parseHourlyWeatherData(List<KmaForecastItemDto> items) {
        // LinkedHashMap을 사용하는 이유: 기상청이 주는 시간 순서(과거->미래)를 그대로 유지하기 위해서입니다.
        Map<String, WeatherHourlyDto> timelineMap = new LinkedHashMap<>();

        for (KmaForecastItemDto item : items) {
            // 밤 23시에서 자정 00시로 넘어가는 상황을 구분하기 위해 '날짜+시간'을 고유 키로 사용합니다.
            String dateTimeKey = item.getFcstDate() + item.getFcstTime();

            // 서랍(Map)에 해당 시간의 객체가 없으면 새로 만들어서 꺼냅니다.
            WeatherHourlyDto dto = timelineMap.getOrDefault(dateTimeKey, WeatherHourlyDto.builder()
                    .time(item.getFcstTime())
                    .sky("1") // 기본값 세팅
                    .pty("0") // 기본값 세팅
                    .build());

            // 카테고리별로 알맞은 값을 객체에 넣습니다.
            switch (item.getCategory()) {
                case "TMP": dto.setTemperature(item.getFcstValue()); break;
                case "SKY": dto.setSky(item.getFcstValue()); break;
                case "PTY": dto.setPty(item.getFcstValue()); break;
            }

            timelineMap.put(dateTimeKey, dto);

            // 딱 6시간 치(6개의 서랍)가 다 채워졌고, 지금 들어온 데이터가 7번째 서랍의 것이라면 수집을 멈춥니다.
            if (timelineMap.size() > 6) {
                timelineMap.remove(dateTimeKey); // 초과된 7번째 데이터는 버림
                break;
            }
        }

        // 모인 6개의 시간대 데이터를 돌면서 SKY, PTY 코드를 최종 한글 상태값으로 변환합니다.
        List<WeatherHourlyDto> resultList = new ArrayList<>(timelineMap.values());
        for (WeatherHourlyDto dto : resultList) {
            String condition = "맑음";
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

    private void validatePublicDataResponse(PublicDataResponse<?> response) {
        if (response == null || response.getResponse() == null || response.getResponse().getHeader() == null) {
            log.error("응답 자체가 Null입니다.");
            throw new CustomException(ErrorCode.EXTERNAL_API_ERROR);
        }

        String resultCode = response.getResponse().getHeader().getResultCode();
        String resultMsg = response.getResponse().getHeader().getResultMsg();

        // 00(성공)이 아닐 경우 로그에 상세 메시지를 찍습니다.
        if (!"00".equals(resultCode)) {
            log.error("공공데이터 서버 응답 에러 - 코드: {}, 메시지: {}", resultCode, resultMsg);
            // 여기서 "SERVICE_KEY_IS_NOT_REGISTERED_ERROR"가 뜨면 100% 키/권한 문제입니다.
            throw new CustomException(ErrorCode.EXTERNAL_API_ERROR);
        }
    }

    // WeatherService 클래스 내부 최하단에 추가
    record AirKoreaRoot(Response response) {
        record Response(Header header, Body body) {}
        record Header(String resultCode, String resultMsg) {}
        record Body(List<AirKoreaItemDto> items) {} // item 껍데기 없이 바로 List 반환!
    }
}