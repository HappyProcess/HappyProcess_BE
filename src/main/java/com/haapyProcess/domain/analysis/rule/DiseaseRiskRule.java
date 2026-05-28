package com.haapyProcess.domain.analysis.rule;

import com.haapyProcess.domain.weather.dto.WeatherResponseDto;

public interface DiseaseRiskRule {

    /**
     * 해당 규칙이 담당하는 질병의 고유 ID (Condition ID)를 반환합니다.
     * 일반인(질병 없음)의 경우 0L 등 특별한 값을 사용할 수 있습니다.
     */
    Long getConditionId();

    /**
     * 프론트엔드 반환 등을 위해 질병 이름을 반환합니다.
     */
    String getDiseaseName();

    /**
     * 날씨 조건에 따라 위험 여부를 판단합니다.
     */
    boolean isAtRisk(WeatherResponseDto weather);
}
