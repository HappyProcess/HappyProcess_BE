package com.haapyProcess.domain.weather.dto;

import lombok.Builder;
import lombok.Getter;
import java.util.List;

@Getter
@Builder
public class WeatherResponseDto {

    private String regionName; // 위치 정보

    // 기상청 단기예보
    private String temperature; // 기온
    private String humidity;    // 습도
    private String weatherCondition; // 상태

    // 에어코리아 실시간 미세먼지
    private String pm10Value; // 미세먼지 수치
    private String pm10Grade; // 미세먼지 등급
    private String pm25Value; // 초미세먼지 수치
    private String pm25Grade; // 초미세먼지 등급

    // 기상청 보건/생활지수
    private String pollenRiskLevel; // 꽃가루 위험도
    private String uvRiskLevel;     // 자외선 위험도

    private List<WeatherHourlyDto> hourlyForecasts;
}