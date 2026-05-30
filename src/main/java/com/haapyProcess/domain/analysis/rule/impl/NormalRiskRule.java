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
        int month = java.time.LocalDate.now().getMonthValue();
        boolean isSummer = (month >= 5 && month <= 10);

        double tempChange = Math.max(weather.getTempDropIn6Hours(), weather.getTempRiseIn6Hours());
        double changeLimit = isSummer ? WeatherRiskCriteria.TEMP_CHANGE_GENERAL_SUMMER.getValue()
                : WeatherRiskCriteria.TEMP_CHANGE_GENERAL_WINTER.getValue();
        int pty = weather.getParsedCurrentPty();

        return weather.getParsedPm10Value() >= WeatherRiskCriteria.PM10_BAD_MIN.getValue() ||
                weather.getParsedPm25Value() >= WeatherRiskCriteria.PM25_BAD_MIN.getValue() ||
                weather.getParsedCurrentTemp() >= WeatherRiskCriteria.TEMP_HEAT_WAVE.getValue() ||
                weather.getParsedCurrentTemp() <= WeatherRiskCriteria.TEMP_COLD_WAVE.getValue() ||
                weather.getParsedUvRisk() >= WeatherRiskCriteria.UV_VERY_HIGH_MIN.getValue() ||
                (pty >= 1 && pty <= 3) ||
                tempChange >= changeLimit;
    }
}
