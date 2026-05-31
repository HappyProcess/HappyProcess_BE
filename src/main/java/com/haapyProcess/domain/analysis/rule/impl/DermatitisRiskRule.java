package com.haapyProcess.domain.analysis.rule.impl;

import com.haapyProcess.domain.analysis.rule.DiseaseRiskRule;
import com.haapyProcess.domain.analysis.criteria.WeatherRiskCriteria;
import com.haapyProcess.domain.weather.dto.WeatherResponseDto;
import org.springframework.stereotype.Component;

@Component
public class DermatitisRiskRule implements DiseaseRiskRule {

    @Override
    public Long getConditionId() {
        return 9L;
    }

    @Override
    public String getDiseaseName() {
        return "피부염/아토피";
    }

    @Override
    public boolean isAtRisk(WeatherResponseDto weather) {
        return weather.getParsedUvRisk() >= WeatherRiskCriteria.UV_HIGH_MIN.getValue() ||
                weather.getParsedHumidity() < WeatherRiskCriteria.HUMIDITY_VERY_DRY_MAX.getValue() ||
                weather.getParsedHumidity() >= WeatherRiskCriteria.HUMIDITY_WET_MIN.getValue();
    }
}
