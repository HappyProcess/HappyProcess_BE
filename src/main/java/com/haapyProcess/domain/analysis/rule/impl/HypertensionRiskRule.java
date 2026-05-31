package com.haapyProcess.domain.analysis.rule.impl;

import com.haapyProcess.domain.analysis.rule.DiseaseRiskRule;
import com.haapyProcess.domain.analysis.criteria.WeatherRiskCriteria;
import com.haapyProcess.domain.weather.dto.WeatherResponseDto;
import org.springframework.stereotype.Component;

@Component
public class HypertensionRiskRule implements DiseaseRiskRule {

    @Override
    public Long getConditionId() {
        return 2L;
    }

    @Override
    public String getDiseaseName() {
        return "고혈압";
    }

    @Override
    public boolean isAtRisk(WeatherResponseDto weather) {
        int month = java.time.LocalDate.now().getMonthValue();
        boolean isSummer = (month >= 5 && month <= 10);

        double tempChange = Math.max(weather.getTempDropIn6Hours(), weather.getTempRiseIn6Hours());
        double changeLimit = isSummer ? WeatherRiskCriteria.TEMP_CHANGE_HYPER_SUMMER.getValue()
                : WeatherRiskCriteria.TEMP_CHANGE_HYPER_WINTER.getValue();

        return weather.getParsedCurrentTemp() >= WeatherRiskCriteria.TEMP_HEAT_WAVE.getValue() ||
                tempChange >= changeLimit;
    }
}
