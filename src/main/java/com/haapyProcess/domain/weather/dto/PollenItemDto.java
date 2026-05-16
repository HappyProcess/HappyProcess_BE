package com.haapyProcess.domain.weather.dto;

import lombok.Getter;

@Getter
public class PollenItemDto {
    // 공공데이터포털 JSON 응답의 실제 키값과 변수명이 완벽히 일치해야 파싱(매핑)이 됩니다.
    private String date;                 // 발표시간 (예: "2026051006")
    private String areaNo;               // 행정구역 지점코드 (예: "1168000000")

    // 위험도 예측값 (0: 낮음, 1: 보통, 2: 높음, 3: 매우높음)
    private String today;                // 오늘 예측값
    private String tomorrow;             // 내일 예측값
    private String dayaftertomorrow;     // 모레 예측값
    private String twodaysaftertomorrow; // 글피 예측값
}