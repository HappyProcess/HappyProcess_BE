package com.haapyProcess.domain.analysis.rule.impl;

import com.haapyProcess.domain.analysis.rule.DiseaseRiskRule;
import com.haapyProcess.domain.analysis.criteria.WeatherRiskCriteria;
import com.haapyProcess.domain.weather.dto.WeatherResponseDto;
import org.springframework.stereotype.Component;

@Component
public class SunAllergyRiskRule implements DiseaseRiskRule {

    @Override
    public Long getConditionId() {
        return 4L;
    }

    @Override
    public String getDiseaseName() {
        return "햇빛알러지";
    }

    @Override
    public boolean isAtRisk(WeatherResponseDto weather) {
        return weather.getParsedUvRisk() >= WeatherRiskCriteria.UV_HIGH_MIN.getValue();
    }
}