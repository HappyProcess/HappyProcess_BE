package com.haapyProcess.domain.analysis.rule.impl;

import com.haapyProcess.domain.analysis.rule.DiseaseRiskRule;
import com.haapyProcess.domain.analysis.criteria.WeatherRiskCriteria;
import com.haapyProcess.domain.weather.dto.WeatherResponseDto;
import org.springframework.stereotype.Component;

@Component
public class DiabetesRiskRule implements DiseaseRiskRule {

    @Override
    public Long getConditionId() {
        return 7L;
    }

    @Override
    public String getDiseaseName() {
        return "당뇨";
    }

    @Override
    public boolean isAtRisk(WeatherResponseDto weather) {
        return weather.getParsedCurrentTemp() >= WeatherRiskCriteria.TEMP_HEAT_WAVE.getValue() ||
                weather.getParsedHumidity() >= WeatherRiskCriteria.HUMIDITY_WET_MIN.getValue();
    }
}
