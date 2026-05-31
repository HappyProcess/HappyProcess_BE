package com.haapyProcess.domain.analysis.rule.impl;

import com.haapyProcess.domain.analysis.criteria.WeatherRiskCriteria;
import com.haapyProcess.domain.analysis.dto.RiskAnalysisResult.FactorGuide;
import com.haapyProcess.domain.analysis.rule.DiseaseRiskRule;
import com.haapyProcess.domain.weather.dto.WeatherResponseDto;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class SunAllergyRiskRule implements DiseaseRiskRule {

    @Override
    public Long getConditionId() {
        return 4L;
    }

    @Override
    public String getDiseaseName() {
        return "햇빛알러지";
    }

    @Override
    public List<FactorGuide> evaluateFactorGuides(WeatherResponseDto weather) {
        List<FactorGuide> guides = new ArrayList<>();

        if (weather.getParsedUvRisk() >= WeatherRiskCriteria.UV_HIGH_MIN.getValue()) {
            guides.add(new FactorGuide("자외선", "자외선이 강해요. SPF50 이상 자외선 차단제를 바르고 긴소매 옷을 착용하세요. 자외선이 강한 오전 10시~오후 2시 외출을 자제하세요."));
        }

        return guides;
    }
}