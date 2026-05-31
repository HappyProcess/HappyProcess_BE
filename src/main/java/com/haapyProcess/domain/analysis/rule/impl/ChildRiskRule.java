package com.haapyProcess.domain.analysis.rule.impl;

import com.haapyProcess.domain.analysis.rule.DiseaseRiskRule;
import com.haapyProcess.domain.analysis.criteria.WeatherRiskCriteria;
import com.haapyProcess.domain.weather.dto.WeatherResponseDto;
import org.springframework.stereotype.Component;

@Component
public class ChildRiskRule implements DiseaseRiskRule {

    @Override
    public Long getConditionId() {
        return 12L;
    }

    @Override
    public String getDiseaseName() {
        return "어린이";
    }

    @Override
    public boolean isAtRisk(WeatherResponseDto weather) {
        return weather.getParsedPm10Value() >= WeatherRiskCriteria.PM10_NORMAL_MIN.getValue() ||
                weather.getParsedPm25Value() >= WeatherRiskCriteria.PM25_NORMAL_MIN.getValue() ||
                weather.getParsedUvRisk() >= WeatherRiskCriteria.UV_HIGH_MIN.getValue() ||
                weather.getParsedCurrentTemp() >= WeatherRiskCriteria.TEMP_HEAT_WAVE.getValue();
    }
}
