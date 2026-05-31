package com.haapyProcess.domain.analysis.rule.impl;

import com.haapyProcess.domain.analysis.criteria.WeatherRiskCriteria;
import com.haapyProcess.domain.analysis.dto.RiskAnalysisResult.FactorGuide;
import com.haapyProcess.domain.analysis.rule.DiseaseRiskRule;
import com.haapyProcess.domain.weather.dto.WeatherResponseDto;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class AsthmaRiskRule implements DiseaseRiskRule {

    @Override
    public Long getConditionId() {
        return 1L;
    }

    @Override
    public String getDiseaseName() {
        return "천식";
    }

    @Override
    public List<FactorGuide> evaluateFactorGuides(WeatherResponseDto weather) {
        List<FactorGuide> guides = new ArrayList<>();

        if (weather.getParsedPm10Value() >= WeatherRiskCriteria.PM10_NORMAL_MIN.getValue()) {
            guides.add(new FactorGuide("미세먼지", "미세먼지 농도가 보통 이상이에요. KF94 마스크를 착용하고 흡입기를 반드시 챙기세요."));
        }

        if (weather.getParsedPm25Value() >= WeatherRiskCriteria.PM25_NORMAL_MIN.getValue()) {
            guides.add(new FactorGuide("초미세먼지", "초미세먼지가 보통 이상이에요. 실외활동을 줄이고 흡입기를 소지하세요. 증상 악화 시 병원을 방문하세요."));
        }

        if (weather.getParsedPollenRisk() >= WeatherRiskCriteria.POLLEN_WARNING_MIN.getValue()) {
            guides.add(new FactorGuide("꽃가루", "꽃가루가 날리고 있어요. 마스크를 착용하고 귀가 후 손과 얼굴을 씻으세요. 흡입기를 챙기세요."));
        }

        return guides;
    }
}
