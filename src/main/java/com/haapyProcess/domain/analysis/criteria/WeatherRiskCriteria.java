package com.haapyProcess.domain.analysis.criteria;

import lombok.Getter;

@Getter
public enum WeatherRiskCriteria {

    // 1. 미세먼지 (PM10) 기준 수치 (단위: ㎍/㎥)
    PM10_NORMAL_MIN(31.0), // 보통
    PM10_BAD_MIN(81.0),    // 나쁨

    // 2. 초미세먼지 (PM2.5) 기준 수치 (단위: ㎍/㎥)
    PM25_NORMAL_MIN(16.0), // 보통
    PM25_BAD_MIN(36.0),    // 나쁨

    // 3. 현재 기온 기준 수치 (단위: ℃)
    TEMP_HEAT_WAVE(33.0),  // 폭염 (현재 기온이 이 수치 이상일 때)
    TEMP_COLD_WAVE(-12.0), // 한파 (현재 기온이 이 수치 이하일 때)

    // 4. 일교차 (최고기온 - 최저기온) 기준 수치 (단위: ℃)
    DIURNAL_RANGE_SUMMER_MIN(8.5), // 여름철(5~10월) 일교차 위험 기준
    DIURNAL_RANGE_WINTER_MIN(11.0),// 겨울철(11~4월) 일교차 위험 기준
    DIURNAL_RANGE_COMMON_MIN(8.5), // 계절 구분 없이 적용할 기본 일교차 기준

    // 5. 습도 기준 수치 (단위: %)
    HUMIDITY_VERY_DRY_MAX(30.0), // 매우 건조 (이 수치 미만)
    HUMIDITY_DRY_MAX(40.0),      // 건조 (이 수치 미만)
    HUMIDITY_WET_MIN(80.0),      // 매우 습함 (이 수치 이상)

    // 6. 지수형 데이터 (자외선, 꽃가루)
    UV_HIGH_MIN(6.0),        // 자외선 높음 시작점
    UV_VERY_HIGH_MIN(8.0),   // 자외선 매우높음 시작점
    POLLEN_WARNING_MIN(1.0); // 꽃가루 1단계 이상

    private final double value;

    WeatherRiskCriteria(double value) {
        this.value = value;
    }
}