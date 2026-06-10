package com.haapyProcess.domain.analysis.rule.impl;

import com.haapyProcess.domain.analysis.criteria.WeatherRiskCriteria;
import com.haapyProcess.domain.analysis.dto.RiskAnalysisResult.FactorGuide;
import com.haapyProcess.domain.analysis.rule.DiseaseRiskRule;
import com.haapyProcess.domain.analysis.score.ScoreBuilder;
import com.haapyProcess.domain.analysis.score.WeatherScore;
import com.haapyProcess.domain.analysis.score.WeatherScoreTables;
import com.haapyProcess.domain.member.entity.PrecipPreference;
import com.haapyProcess.domain.weather.dto.WeatherResponseDto;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class DiabetesRiskRule implements DiseaseRiskRule {

    @Override
    public Long getConditionId() {
        return 7L;
    }

    @Override
    public String getDiseaseName() {
        return "당뇨";
    }

    @Override
    public List<FactorGuide> evaluateFactorGuides(WeatherResponseDto weather) {
        List<FactorGuide> guides = new ArrayList<>();

        if (weather.getParsedCurrentTemp() >= WeatherRiskCriteria.TEMP_HEAT_WAVE.getValue()) {
            guides.add(new FactorGuide("폭염", "더운 날씨에 탈수로 혈당이 급변할 수 있어요. 수분을 자주 섭취하고 혈당을 자주 체크하세요. 인슐린 보관에 주의하세요."));
        }

        if (weather.getParsedHumidity() >= WeatherRiskCriteria.HUMIDITY_WET_MIN.getValue()) {
            guides.add(new FactorGuide("매우 습함", "매우 습한 날씨예요. 컨디션 변화에 주의하고 혈당을 자주 확인하세요."));
        }

        return guides;
    }

    @Override
    public WeatherScore evaluateWeatherScore(WeatherResponseDto weather, PrecipPreference precipPreference) {
        // 당뇨는 폭염(탈수)이 핵심이므로 고온 점수를 사용한다.
        return ScoreBuilder.create()
                .add("기온", WeatherScoreTables.tempHeat(weather.getParsedCurrentTemp()), 0.65)
                .add("높은 습도", WeatherScoreTables.humidityWet(weather.getParsedHumidity()), 0.35)
                .build();
    }
}
