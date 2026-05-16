package com.haapyProcess.domain.weather.dto;

import lombok.Getter;

@Getter
public class KmaForecastItemDto {
    private String baseDate;  // 발표일자
    private String baseTime;  // 발표시각
    private String category;  // 자료구분코드 (예: TMP, REH, SKY, PTY)
    private String fcstDate;  // 예측일자
    private String fcstTime;  // 예측시각
    private String fcstValue; // 예보지점의 예보값
    private int nx;           // 예보지점 X 좌표
    private int ny;           // 예보지점 Y 좌표

    public String getCategoryName() {
        return switch (category) {
            case "TMP" -> "1시간 기온(℃)";
            case "REH" -> "습도(%)";
            case "SKY" -> "하늘상태";
            case "PTY" -> "강수형태";
            case "WSD" -> "풍속(m/s)";
            default -> category;
        };
    }
}