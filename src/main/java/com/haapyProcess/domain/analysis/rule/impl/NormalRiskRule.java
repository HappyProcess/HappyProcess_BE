package com.haapyProcess.domain.analysis.rule.impl;

import com.haapyProcess.domain.analysis.rule.DiseaseRiskRule;
import com.haapyProcess.domain.analysis.criteria.WeatherRiskCriteria;
import com.haapyProcess.domain.weather.dto.WeatherResponseDto;
import org.springframework.stereotype.Component;

@Component
public class NormalRiskRule implements DiseaseRiskRule {

    @Override
    public Long getConditionId() {
        return 1L;
    }

    @Override
    public String getDiseaseName() {
        return "질병없음";
    }

    @Override
    public boolean isAtRisk(WeatherResponseDto weather) {
        int pty = weather.getParsedCurrentPty();
        return weather.getParsedPm10Value() >= WeatherRiskCriteria.PM10_BAD_MIN.getValue() ||
                weather.getParsedPm25Value() >= WeatherRiskCriteria.PM25_BAD_MIN.getValue() ||
                weather.getParsedCurrentTemp() >= WeatherRiskCriteria.TEMP_HEAT_WAVE.getValue() ||
                weather.getParsedCurrentTemp() <= WeatherRiskCriteria.TEMP_COLD_WAVE.getValue() ||
                weather.getParsedUvRisk() >= WeatherRiskCriteria.UV_VERY_HIGH_MIN.getValue() ||
                weather.getTempDropIn6Hours() >= 10.0 ||
                pty == 1 || pty == 2 || pty == 3;
    }
}
