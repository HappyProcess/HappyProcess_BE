package com.haapyProcess.domain.weather.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class WeatherHourlyDto {
    private String time;        // 예: "2300", "0000"
    private String temperature; // 기온
    private String condition;   // "맑음", "비" 등

    // 내부 연산용 임시 변수 (프론트엔드엔 안 나가도 무방함)
    private String sky;
    private String pty;
}