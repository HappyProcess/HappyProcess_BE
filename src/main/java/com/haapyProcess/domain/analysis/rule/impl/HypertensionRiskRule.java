package com.haapyProcess.domain.analysis.rule.impl;

import com.haapyProcess.domain.analysis.rule.DiseaseRiskRule;
import com.haapyProcess.domain.analysis.criteria.WeatherRiskCriteria;
import com.haapyProcess.domain.weather.dto.WeatherResponseDto;
import org.springframework.stereotype.Component;

@Component
public class HypertensionRiskRule implements DiseaseRiskRule {

    @Override
    public Long getConditionId() {
        return 3L;
    }

    @Override
    public String getDiseaseName() {
        return "고혈압";
    }

    @Override
    public boolean isAtRisk(WeatherResponseDto weather) {
        return weather.getParsedCurrentTemp() >= WeatherRiskCriteria.TEMP_HEAT_WAVE.getValue() ||
                weather.getTempDropIn6Hours() >= WeatherRiskCriteria.DIURNAL_RANGE_COMMON_MIN.getValue();
    }
}
