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
public class DryEyeRiskRule implements DiseaseRiskRule {

    @Override
    public Long getConditionId() {
        return 3L;
    }

    @Override
    public String getDiseaseName() {
        return "안구건조증";
    }

    @Override
    public List<FactorGuide> evaluateFactorGuides(WeatherResponseDto weather) {
        List<FactorGuide> guides = new ArrayList<>();

        if (weather.getParsedHumidity() < WeatherRiskCriteria.HUMIDITY_DRY_MAX.getValue()) {
            guides.add(new FactorGuide("건조", "공기가 건조해요. 인공눈물을 자주 넣고 렌즈 착용을 자제하세요. 실내 가습기를 사용하세요."));
        }

        if (weather.getParsedUvRisk() >= WeatherRiskCriteria.UV_HIGH_MIN.getValue()) {
            guides.add(new FactorGuide("자외선", "자외선이 강해요. 외출 시 선글라스를 착용해 눈을 보호하세요."));
        }

        return guides;
    }

    @Override
    public WeatherScore evaluateWeatherScore(WeatherResponseDto weather, PrecipPreference precipPreference) {
        return ScoreBuilder.create()
                .add("건조", WeatherScoreTables.humidityDry(weather.getParsedHumidity()), 0.75)
                .add("자외선", WeatherScoreTables.uv(weather.getParsedUvRisk()), 0.25)
                .build();
    }
}
