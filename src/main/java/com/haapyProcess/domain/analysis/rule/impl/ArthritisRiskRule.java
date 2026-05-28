package com.haapyProcess.domain.analysis.rule.impl;

import com.haapyProcess.domain.analysis.rule.DiseaseRiskRule;
import com.haapyProcess.domain.weather.dto.WeatherResponseDto;
import org.springframework.stereotype.Component;

@Component
public class ArthritisRiskRule implements DiseaseRiskRule {

    @Override
    public Long getConditionId() {
        return 11L;
    }

    @Override
    public String getDiseaseName() {
        return "관절염";
    }

    @Override
    public boolean isAtRisk(WeatherResponseDto weather) {
        int pty = weather.getParsedCurrentPty();
        return weather.getTempDropIn6Hours() >= 10.0 || pty == 1 || pty == 2 || pty == 3;
    }
}
