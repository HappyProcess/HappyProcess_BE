package com.haapyProcess.domain.analysis.rule.impl;

import com.haapyProcess.domain.analysis.rule.DiseaseRiskRule;
import com.haapyProcess.domain.analysis.criteria.WeatherRiskCriteria;
import com.haapyProcess.domain.weather.dto.WeatherResponseDto;
import org.springframework.stereotype.Component;

@Component
public class StrokeRiskRule implements DiseaseRiskRule {

    @Override
    public Long getConditionId() {
        return 11L;
    }

    @Override
    public String getDiseaseName() {
        return "뇌졸중";
    }

    @Override
    public boolean isAtRisk(WeatherResponseDto weather) {
        return weather.getTempDropIn6Hours() >= WeatherRiskCriteria.TEMP_DROP_SUDDEN.getValue();
    }
}