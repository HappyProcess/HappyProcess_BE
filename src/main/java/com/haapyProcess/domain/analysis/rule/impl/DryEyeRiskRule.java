package com.haapyProcess.domain.analysis.rule.impl;

import com.haapyProcess.domain.analysis.rule.DiseaseRiskRule;
import com.haapyProcess.domain.analysis.criteria.WeatherRiskCriteria;
import com.haapyProcess.domain.weather.dto.WeatherResponseDto;
import org.springframework.stereotype.Component;

@Component
public class DryEyeRiskRule implements DiseaseRiskRule {

    @Override
    public Long getConditionId() {
        return 3L;
    }

    @Override
    public String getDiseaseName() {
        return "안구건조증";
    }

    @Override
    public boolean isAtRisk(WeatherResponseDto weather) {
        return weather.getParsedHumidity() < WeatherRiskCriteria.HUMIDITY_DRY_MAX.getValue() ||
                weather.getParsedUvRisk() >= WeatherRiskCriteria.UV_HIGH_MIN.getValue();
    }
}
