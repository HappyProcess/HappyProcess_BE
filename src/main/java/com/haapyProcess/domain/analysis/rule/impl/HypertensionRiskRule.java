package com.haapyProcess.domain.analysis.rule.impl;

import com.haapyProcess.domain.analysis.criteria.WeatherRiskCriteria;
import com.haapyProcess.domain.analysis.dto.RiskAnalysisResult.FactorGuide;
import com.haapyProcess.domain.analysis.rule.DiseaseRiskRule;
import com.haapyProcess.domain.analysis.score.WeatherScoreTables;
import com.haapyProcess.domain.member.entity.PrecipPreference;
import com.haapyProcess.domain.weather.dto.WeatherResponseDto;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

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
    public List<FactorGuide> evaluateFactorGuides(WeatherResponseDto weather) {
        List<FactorGuide> guides = new ArrayList<>();
        int month = java.time.LocalDate.now().getMonthValue();
        boolean isSummer = (month >= 5 && month <= 10);

        double tempChange = Math.max(weather.getTempDropIn6Hours(), weather.getTempRiseIn6Hours());
        double changeLimit = isSummer ? WeatherRiskCriteria.TEMP_CHANGE_HYPER_SUMMER.getValue()
                : WeatherRiskCriteria.TEMP_CHANGE_HYPER_WINTER.getValue();

        if (weather.getParsedCurrentTemp() >= WeatherRiskCriteria.TEMP_HEAT_WAVE.getValue()) {
            guides.add(new FactorGuide("폭염", "더운 날씨에 혈압이 급변할 수 있어요. 낮 12시~오후 5시 외출을 자제하고 혈압을 자주 체크하세요."));
        }

        if (tempChange >= changeLimit) {
            guides.add(new FactorGuide("기온 급변", "기온이 빠르게 변하고 있어요. 혈압 변동에 주의하고 혈압약을 꼭 챙기세요. 외출 시 보온에 신경 쓰세요."));
        }

        return guides;
    }

    @Override
    public int evaluateWeatherScore(WeatherResponseDto weather, PrecipPreference precipPreference) {
        int temp = WeatherScoreTables.temp(weather.getParsedCurrentTemp());
        double changeVal = Math.max(weather.getTempDropIn6Hours(), weather.getTempRiseIn6Hours());
        int tempChange = WeatherScoreTables.tempChange(changeVal);

        double raw = temp * 0.55 + tempChange * 0.45;
        return 100 - (int) Math.round(raw);
    }
}
