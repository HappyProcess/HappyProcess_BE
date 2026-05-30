package com.haapyProcess.domain.analysis.criteria;

import lombok.Getter;

@Getter
public enum WeatherRiskCriteria {

    // 1. 미세먼지 (PM10, PM2.5) 기준 (단위: ㎍/㎥)
    PM10_NORMAL_MIN(31.0), // 보통
    PM10_BAD_MIN(81.0),    // 나쁨
    PM25_NORMAL_MIN(16.0), // 보통
    PM25_BAD_MIN(36.0),    // 나쁨

    // 2. 현재 기온 (폭염/한파) 기준 (단위: ℃)
    TEMP_HEAT_WAVE(33.0),  // 폭염 (33도 이상)
    TEMP_COLD_WAVE(-12.0), // 한파 (-12도 이하)

    // 3. 6시간 기온 급변/급감 기준 (단위: ℃)
    TEMP_CHANGE_GENERAL_SUMMER(5.0), // 일반인 여름(5~10월) 급변
    TEMP_CHANGE_GENERAL_WINTER(7.0), // 일반인 겨울(11~4월) 급변
    TEMP_CHANGE_HYPER_SUMMER(4.0),   // 고혈압 여름(5~10월) 급변
    TEMP_CHANGE_HYPER_WINTER(5.0),   // 고혈압 겨울(11~4월) 급변
    TEMP_DROP_SUDDEN(5.0),           // 관절염/뇌졸중 온도 5도 이상 급감

    // 4. 습도 기준 (단위: %)
    HUMIDITY_VERY_DRY_MAX(30.0), // 매우 건조 (미만)
    HUMIDITY_DRY_MAX(40.0),      // 건조 (미만)
    HUMIDITY_WET_MIN(80.0),      // 매우 습함 (이상)

    // 5. 지수형 데이터 (자외선, 꽃가루)
    UV_HIGH_MIN(6.0),        // 자외선 높음 (6 이상)
    UV_VERY_HIGH_MIN(8.0),   // 자외선 매우높음 (8 이상)
    POLLEN_WARNING_MIN(1.0); // 꽃가루 보통 (1 이상)

    private final double value;

    WeatherRiskCriteria(double value) {
        this.value = value;
    }
}