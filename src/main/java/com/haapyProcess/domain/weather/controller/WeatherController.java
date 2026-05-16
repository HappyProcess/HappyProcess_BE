package com.haapyProcess.domain.weather.controller;

import com.haapyProcess.domain.weather.dto.WeatherResponseDto;
import com.haapyProcess.domain.weather.service.WeatherService;
import io.swagger.v3.oas.annotations.Operation;
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

    @Operation(summary = "통합 날씨 데이터 조회", description = "행정구역코드를 기반으로 날씨, 미세먼지, 꽃가루, 자외선을 통합 조회합니다.")
    @GetMapping("/combined")
    public ResponseEntity<WeatherResponseDto> getCombinedWeather(
            @RequestParam String areaNo) {

        log.info("통합 날씨 데이터 요청 - 지역코드: {}", areaNo);
        return ResponseEntity.ok(weatherService.getCombinedWeatherData(areaNo));
    }
}