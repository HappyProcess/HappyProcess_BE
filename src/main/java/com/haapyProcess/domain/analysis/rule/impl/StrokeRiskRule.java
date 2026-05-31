package com.haapyProcess.domain.analysis.rule.impl;

import com.haapyProcess.domain.analysis.criteria.WeatherRiskCriteria;
import com.haapyProcess.domain.analysis.dto.RiskAnalysisResult.FactorGuide;
import com.haapyProcess.domain.analysis.rule.DiseaseRiskRule;
import com.haapyProcess.domain.weather.dto.WeatherResponseDto;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class StrokeRiskRule implements DiseaseRiskRule {

    @Override
    public Long getConditionId() {
        return 11L;
    }

    @Override
    public String getDiseaseName() {
        return "뇌졸중";
    }

    @Override
    public List<FactorGuide> evaluateFactorGuides(WeatherResponseDto weather) {
        List<FactorGuide> guides = new ArrayList<>();

        if (weather.getTempDropIn6Hours() >= WeatherRiskCriteria.TEMP_DROP_SUDDEN.getValue()) {
            guides.add(new FactorGuide("기온 급감", "기온이 급격히 낮아지고 있어요. 갑작스러운 외출을 피하고 외출 전 실내에서 몸을 충분히 따뜻하게 하세요. 준비운동 후 활동하세요."));
        }

        return guides;
    }
}