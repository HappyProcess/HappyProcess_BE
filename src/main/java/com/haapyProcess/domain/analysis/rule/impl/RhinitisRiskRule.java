package com.haapyProcess.domain.analysis.rule.impl;

import com.haapyProcess.domain.analysis.rule.DiseaseRiskRule;
import com.haapyProcess.domain.analysis.criteria.WeatherRiskCriteria;
import com.haapyProcess.domain.weather.dto.WeatherResponseDto;
import org.springframework.stereotype.Component;

@Component
public class RhinitisRiskRule implements DiseaseRiskRule {

    @Override
    public Long getConditionId() {
        return 6L;
    }

    @Override
    public String getDiseaseName() {
        return "비염";
    }

    @Override
    public boolean isAtRisk(WeatherResponseDto weather) {
        return weather.getParsedPollenRisk() >= WeatherRiskCriteria.POLLEN_WARNING_MIN.getValue() ||
                weather.getParsedPm10Value() >= WeatherRiskCriteria.PM10_NORMAL_MIN.getValue() ||
                weather.getParsedPm25Value() >= WeatherRiskCriteria.PM25_NORMAL_MIN.getValue() ||
                weather.getParsedHumidity() < WeatherRiskCriteria.HUMIDITY_DRY_MAX.getValue();
    }
}