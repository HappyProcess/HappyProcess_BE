package com.haapyProcess.domain.analysis.rule.impl;

import com.haapyProcess.domain.analysis.criteria.WeatherRiskCriteria;
import com.haapyProcess.domain.analysis.dto.RiskAnalysisResult.FactorGuide;
import com.haapyProcess.domain.analysis.rule.DiseaseRiskRule;
import com.haapyProcess.domain.weather.dto.WeatherResponseDto;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

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
    public List<FactorGuide> evaluateFactorGuides(WeatherResponseDto weather) {
        List<FactorGuide> guides = new ArrayList<>();

        if (weather.getParsedCurrentTemp() >= WeatherRiskCriteria.TEMP_HEAT_WAVE.getValue()) {
            guides.add(new FactorGuide("폭염", "더운 날씨는 심장에 부담을 줄 수 있어요. 야외활동을 자제하고 시원한 실내에 머무세요. 무리한 활동을 삼가세요."));
        } else if (weather.getParsedCurrentTemp() <= WeatherRiskCriteria.TEMP_COLD_WAVE.getValue()) {
            guides.add(new FactorGuide("한파", "한파 시 혈관이 수축해 심장에 부담이 커요. 갑작스러운 외출을 피하고 목·손·발을 따뜻하게 보온하세요."));
        }

        if (weather.getParsedPm25Value() >= WeatherRiskCriteria.PM25_NORMAL_MIN.getValue()) {
            guides.add(new FactorGuide("초미세먼지", "초미세먼지가 보통 이상이에요. 초미세먼지는 혈관에 직접 영향을 줄 수 있어요. 마스크를 착용하고 외출을 자제하세요."));
        }

        return guides;
    }
}
