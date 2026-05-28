package com.haapyProcess.domain.weather.controller;

import com.haapyProcess.domain.weather.dto.WeatherResponseDto;
import com.haapyProcess.domain.weather.service.WeatherService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Weather", description = "날씨 및 환경 지수 API")
@Slf4j
@RestController
@RequestMapping("/api/v1/weather")
@RequiredArgsConstructor
public class WeatherController {

    private final WeatherService weatherService;

    @Operation(
            summary = "통합 날씨 데이터 조회",
            description = """
                행정구역코드(areaNo)를 기반으로 기상청(단기예보, 자외선, 꽃가루)과 에어코리아(미세먼지)의 데이터를 통합하여 반환합니다.
                
                ## **📋 Request Parameters**
                | **이름** | **설명** | **타입** | **필수** |
                |---|---|---|:---:|
                | **areaNo** | 행정구역코드 (10자리 숫자) | String | ✅ |
                
                ---
                
                ## **📋 주요 Response 데이터 구조**
                * 모든 생활 지수와 기상 데이터는 **조회 시점(현재 시간)**을 기준으로 제공됩니다.
                
                | **카테고리** | **키 (Key)** | **설명** | **타입** |
                |---|---|---|---|
                | **기본 정보** | **regionName** | 조회된 동네 이름 (예: 역삼동) | String |
                | **단기 예보** | **temperature** | 현재 기온 원본 문자열 | String |
                | | **humidity** | 현재 습도 원본 문자열 | String |
                | | **weatherCondition** | 날씨 상태 (맑음, 구름많음, 흐림, 비, 눈 등) | String |
                | **미세먼지** | **pm10Value** | 미세먼지 수치 원본 문자열 | String |
                | | **pm10Grade** | 미세먼지 등급 (1:좋음 ~ 4:매우나쁨) | String |
                | | **pm25Value** | 초미세먼지 수치 원본 문자열 | String |
                | | **pm25Grade** | 초미세먼지 등급 (1:좋음 ~ 4:매우나쁨) | String |
                | **생활 지수** | **pollenRiskLevel** | **현재 시간 기준** 소나무 꽃가루 위험도 원본 | String |
                | | **uvRiskLevel** | **현재 시간 기준** 자외선 지수 원본 | String |
                | **시간별 예보**| **hourlyForecasts** | 향후 6시간 동안의 시간별 예보 배열 | List |
                
                ## **🔧 위험도 판별용 파싱 데이터 (Parsed Data)**
                * 백엔드 내부 연산 및 프론트엔드의 숫자형 연산을 돕기 위해 Double/Integer 형태로 파싱된 데이터입니다.
                
                | **키 (Key)** | **설명** | **타입** |
                |---|---|---|
                | **parsedCurrentTemp** | 현재 기온 (기본값: 20.0) | double |
                | **parsedHumidity** | 현재 습도 (기본값: 50.0) | double |
                | **parsedPm10Value** | 미세먼지 수치 (기본값: 0.0) | double |
                | **parsedPm25Value** | 초미세먼지 수치 (기본값: 0.0) | double |
                | **parsedPollenRisk** | 파싱된 꽃가루 지수 (기본값: 0.0) | double |
                | **parsedUvRisk** | 파싱된 자외선 지수 (기본값: 0.0) | double |
                | **tempDropIn6Hours** | 6시간 내 기온 급감량 (현재기온 - 6시간내 최저기온) | double |
                | **parsedCurrentPty** | 강수 형태 코드 (0:없음, 1:비, 2:비/눈, 3:눈, 4:소나기) | int |
                """
    )
    @ApiResponse(responseCode = "200", description = "날씨 데이터 조회 성공")
    @ApiResponse(responseCode = "400", description = "유효하지 않은 지역 코드 형식")
    @ApiResponse(responseCode = "404", description = "지원하지 않는 지역 코드 (DB에 존재하지 않음)")
    @ApiResponse(responseCode = "500", description = "외부 공공 API 통신 실패 또는 데이터 파싱 에러")
    @GetMapping("/combined")
    public ResponseEntity<WeatherResponseDto> getCombinedWeather(
            @Parameter(description = "조회할 대상의 행정구역코드", example = "1111051500", required = true)
            @RequestParam String areaNo) {

        log.info("통합 날씨 데이터 요청 - 지역코드: {}", areaNo);
        return ResponseEntity.ok(weatherService.getCombinedWeatherData(areaNo));
    }
}