package com.haapyProcess.domain.analysis.rule.impl;

import com.haapyProcess.domain.analysis.criteria.WeatherRiskCriteria;
import com.haapyProcess.domain.analysis.dto.RiskAnalysisResult.FactorGuide;
import com.haapyProcess.domain.analysis.rule.DiseaseRiskRule;
import com.haapyProcess.domain.weather.dto.WeatherResponseDto;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class PollenAllergyRiskRule implements DiseaseRiskRule {

    @Override
    public Long getConditionId() {
        return 5L;
    }

    @Override
    public String getDiseaseName() {
        return "꽃가루알러지";
    }

    @Override
    public List<FactorGuide> evaluateFactorGuides(WeatherResponseDto weather) {
        List<FactorGuide> guides = new ArrayList<>();
        double pollen = weather.getParsedPollenRisk();

        if (pollen >= 3.0) {
            // 3단계 (매우높음)
            guides.add(new FactorGuide("꽃가루 (매우높음)", "꽃가루 농도가 매우 높아요. 외출을 삼가고 항히스타민제를 준비하세요. 증상 심화 시 병원을 방문하세요."));

        } else if (pollen >= 2.0) {
            // 2단계 (높음)
            guides.add(new FactorGuide("꽃가루 (높음)", "꽃가루 농도가 높아요. 외출을 자제하고 귀가 후 바로 샤워하세요. 창문을 꼭 닫아두세요."));

        } else if (pollen >= WeatherRiskCriteria.POLLEN_WARNING_MIN.getValue()) {
            // 1단계 (보통)
            guides.add(new FactorGuide("꽃가루 (보통)", "꽃가루가 날리고 있어요. 외출 시 마스크와 선글라스를 착용하고 창문을 닫아두세요."));
        }

        return guides;
    }
}