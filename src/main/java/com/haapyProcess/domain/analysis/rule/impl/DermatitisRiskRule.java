package com.haapyProcess.domain.analysis.rule.impl;

import com.haapyProcess.domain.analysis.criteria.WeatherRiskCriteria;
import com.haapyProcess.domain.analysis.dto.RiskAnalysisResult.FactorGuide;
import com.haapyProcess.domain.analysis.rule.DiseaseRiskRule;
import com.haapyProcess.domain.weather.dto.WeatherResponseDto;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class DermatitisRiskRule implements DiseaseRiskRule {

    @Override
    public Long getConditionId() {
        return 9L;
    }

    @Override
    public String getDiseaseName() {
        return "피부염/아토피";
    }

    @Override
    public List<FactorGuide> evaluateFactorGuides(WeatherResponseDto weather) {
        List<FactorGuide> guides = new ArrayList<>();

        if (weather.getParsedUvRisk() >= WeatherRiskCriteria.UV_HIGH_MIN.getValue()) {
            guides.add(new FactorGuide("자외선", "자외선이 강해요. 자외선 차단제를 꼼꼼히 바르고 피부 노출을 최소화하세요."));
        }

        if (weather.getParsedHumidity() < WeatherRiskCriteria.HUMIDITY_VERY_DRY_MAX.getValue()) {
            guides.add(new FactorGuide("매우 건조", "매우 건조한 날씨예요. 보습제를 자주 바르고 수분 섭취를 늘리세요."));
        } else if (weather.getParsedHumidity() >= WeatherRiskCriteria.HUMIDITY_WET_MIN.getValue()) {
            guides.add(new FactorGuide("매우 습함", "매우 습한 날씨예요. 땀이 나면 바로 닦고 통기성 좋은 옷을 입으세요."));
        }

        return guides;
    }
}
