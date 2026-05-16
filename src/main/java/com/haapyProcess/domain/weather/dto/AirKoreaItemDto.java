package com.haapyProcess.domain.weather.dto;

import lombok.Getter;

@Getter
public class AirKoreaItemDto {

    private String dataTime;

    // 실시간 농도 수치 (단위: ㎍/㎥)
    private String pm10Value; // 미세먼지 (PM10) 농도
    private String pm25Value; // 초미세먼지 (PM2.5) 농도

    // 24시간 예측 이동 평균 등급 (1: 좋음, 2: 보통, 3: 나쁨, 4: 매우나쁨)
    private String pm10Grade; // 미세먼지 등급
    private String pm25Grade; // 초미세먼지 등급

    // 통합 대기 환경 지수 (미세먼지, 오존 등 모두 합친 종합 점수)
    private String khaiValue; // 통합대기환경수치
    private String khaiGrade; // 통합대기환경지수 (1~4)

    // 측정소 점검 등으로 인한 데이터 부재 여부 확인
    public boolean isDataValid() {
        return pm10Value != null && !pm10Value.equals("-") &&
                pm25Value != null && !pm25Value.equals("-");
    }
}