package com.haapyProcess.domain.analysis.rule.impl;

import com.haapyProcess.domain.analysis.rule.DiseaseRiskRule;
import com.haapyProcess.domain.analysis.criteria.WeatherRiskCriteria;
import com.haapyProcess.domain.weather.dto.WeatherResponseDto;
import org.springframework.stereotype.Component;

@Component
public class ElderlyRiskRule implements DiseaseRiskRule {

    @Override
    public Long getConditionId() {
        return 14L;
    }

    @Override
    public String getDiseaseName() {
        return "고령";
    }

    @Override
    public boolean isAtRisk(WeatherResponseDto weather) {
        int pty = weather.getParsedCurrentPty();
        return weather.getParsedCurrentTemp() >= WeatherRiskCriteria.TEMP_HEAT_WAVE.getValue() ||
                weather.getParsedCurrentTemp() <= WeatherRiskCriteria.TEMP_COLD_WAVE.getValue() ||
                weather.getParsedPm10Value() >= WeatherRiskCriteria.PM10_NORMAL_MIN.getValue() ||
                (pty == 2 || pty == 3);
    }
}
