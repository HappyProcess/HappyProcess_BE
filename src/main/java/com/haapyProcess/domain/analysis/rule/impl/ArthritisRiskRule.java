package com.haapyProcess.domain.analysis.rule.impl;

import com.haapyProcess.domain.analysis.criteria.WeatherRiskCriteria;
import com.haapyProcess.domain.analysis.rule.DiseaseRiskRule;
import com.haapyProcess.domain.weather.dto.WeatherResponseDto;
import org.springframework.stereotype.Component;

@Component
public class ArthritisRiskRule implements DiseaseRiskRule {

    @Override
    public Long getConditionId() {
        return 10L;
    }

    @Override
    public String getDiseaseName() {
        return "관절염";
    }

    @Override
    public boolean isAtRisk(WeatherResponseDto weather) {
        int pty = weather.getParsedCurrentPty();
        return weather.getTempDropIn6Hours() >= WeatherRiskCriteria.TEMP_DROP_SUDDEN.getValue() ||
                (pty >= 1 && pty <= 3);
    }
}
