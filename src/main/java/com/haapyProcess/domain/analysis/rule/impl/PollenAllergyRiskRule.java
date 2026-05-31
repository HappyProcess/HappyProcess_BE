package com.haapyProcess.domain.analysis.rule.impl;

import com.haapyProcess.domain.analysis.rule.DiseaseRiskRule;
import com.haapyProcess.domain.analysis.criteria.WeatherRiskCriteria;
import com.haapyProcess.domain.weather.dto.WeatherResponseDto;
import org.springframework.stereotype.Component;

@Component
public class PollenAllergyRiskRule implements DiseaseRiskRule {

    @Override
    public Long getConditionId() {
        return 5L;
    }

    @Override
    public String getDiseaseName() {
        return "꽃가루알러지";
    }

    @Override
    public boolean isAtRisk(WeatherResponseDto weather) {
        return weather.getParsedPollenRisk() >= WeatherRiskCriteria.POLLEN_WARNING_MIN.getValue();
    }
}