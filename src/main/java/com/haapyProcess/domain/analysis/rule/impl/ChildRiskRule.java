package com.haapyProcess.domain.analysis.rule.impl;

import com.haapyProcess.domain.analysis.criteria.WeatherRiskCriteria;
import com.haapyProcess.domain.analysis.dto.RiskAnalysisResult.FactorGuide;
import com.haapyProcess.domain.analysis.rule.DiseaseRiskRule;
import com.haapyProcess.domain.weather.dto.WeatherResponseDto;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class ChildRiskRule implements DiseaseRiskRule {

    @Override
    public Long getConditionId() {
        return 12L;
    }

    @Override
    public String getDiseaseName() {
        return "어린이";
    }

    @Override
    public List<FactorGuide> evaluateFactorGuides(WeatherResponseDto weather) {
        List<FactorGuide> guides = new ArrayList<>();

        if (weather.getParsedPm10Value() >= WeatherRiskCriteria.PM10_NORMAL_MIN.getValue()) {
            guides.add(new FactorGuide("미세먼지", "미세먼지가 보통 이상이에요. 아이 야외활동을 줄이고 마스크를 착용시켜 주세요."));
        }

        if (weather.getParsedPm25Value() >= WeatherRiskCriteria.PM25_NORMAL_MIN.getValue()) {
            guides.add(new FactorGuide("초미세먼지", "초미세먼지가 보통 이상이에요. 아이 실외활동을 줄이고 귀가 후 손과 얼굴을 씻겨주세요."));
        }

        if (weather.getParsedUvRisk() >= WeatherRiskCriteria.UV_HIGH_MIN.getValue()) {
            guides.add(new FactorGuide("자외선", "자외선이 강해요. 아이 외출 시 모자와 자외선 차단제로 피부를 보호해 주세요."));
        }

        if (weather.getParsedCurrentTemp() >= WeatherRiskCriteria.TEMP_HEAT_WAVE.getValue()) {
            guides.add(new FactorGuide("폭염", "현재 기온이 매우 높아요. 아이가 야외에서 오래 있지 않도록 하고 30분마다 수분을 챙겨주세요."));
        }

        return guides;
    }
}
