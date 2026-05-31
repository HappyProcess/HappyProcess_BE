package com.haapyProcess.domain.analysis.rule.impl;

import com.haapyProcess.domain.analysis.rule.DiseaseRiskRule;
import com.haapyProcess.domain.analysis.criteria.WeatherRiskCriteria;
import com.haapyProcess.domain.weather.dto.WeatherResponseDto;
import org.springframework.stereotype.Component;

@Component
public class HeartDiseaseRiskRule implements DiseaseRiskRule {

    @Override
    public Long getConditionId() {
        return 8L;
    }

    @Override
    public String getDiseaseName() {
        return "심장질환";
    }

    @Override
    public boolean isAtRisk(WeatherResponseDto weather) {
        return weather.getParsedCurrentTemp() >= WeatherRiskCriteria.TEMP_HEAT_WAVE.getValue() ||
                weather.getParsedCurrentTemp() <= WeatherRiskCriteria.TEMP_COLD_WAVE.getValue() ||
                weather.getParsedPm25Value() >= WeatherRiskCriteria.PM25_NORMAL_MIN.getValue();
    }
}
