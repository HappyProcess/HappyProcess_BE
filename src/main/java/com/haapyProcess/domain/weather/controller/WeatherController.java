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
                * **regionName**: 조회된 동네 이름 (예: 청운효자동)
                * **temperature**: 현재 기온
                * **humidity**: 현재 습도
                * **weatherCondition**: 날씨 상태 (맑음, 흐림, 비 등)
                * **pm10 / pm25**: 미세먼지 수치 및 등급 (좋음, 보통, 나쁨 등)
                * **pollenRiskLevel**: 소나무 꽃가루 위험도 지수
                * **uvRiskLevel**: 일일 최고 자외선 지수
                * **hourlyForecasts**: 향후 6시간 동안의 시간별 예보 배열
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