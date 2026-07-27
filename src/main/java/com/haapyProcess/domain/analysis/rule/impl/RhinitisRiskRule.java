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
public class RhinitisRiskRule implements DiseaseRiskRule {

    @Override
    public Long getConditionId() {
        return 6L;
    }

    @Override
    public String getDiseaseName() {
        return "비염";
    }

    @Override
    public List<FactorGuide> evaluateFactorGuides(WeatherResponseDto weather) {
        List<FactorGuide> guides = new ArrayList<>();

        if (weather.getParsedPollenRisk() >= WeatherRiskCriteria.POLLEN_WARNING_MIN.getValue()) {
            guides.add(new FactorGuide("꽃가루", "꽃가루가 날리고 있어요. 마스크를 착용하고 귀가 후 코 세척을 권장해요."));
        }

        if (weather.getParsedPm10Value() >= WeatherRiskCriteria.PM10_NORMAL_MIN.getValue()) {
            guides.add(new FactorGuide("미세먼지", "미세먼지가 보통 이상이에요. 마스크를 착용하고 실내에서는 공기청정기를 사용하세요."));
        }

        if (weather.getParsedPm25Value() >= WeatherRiskCriteria.PM25_NORMAL_MIN.getValue()) {
            guides.add(new FactorGuide("초미세먼지", "초미세먼지가 보통 이상이에요. KF94 마스크를 착용하고 실외활동을 줄이세요."));
        }

        if (weather.getParsedHumidity() < WeatherRiskCriteria.HUMIDITY_DRY_MAX.getValue()) {
            guides.add(new FactorGuide("건조", "건조한 날씨예요. 실내 가습기를 사용하고 물을 자주 마셔 코 점막을 보호하세요."));
        }

        return guides;
    }

    @Override
    public WeatherScore evaluateWeatherScore(WeatherResponseDto weather, PrecipPreference precipPreference) {
        return ScoreBuilder.create()
                .add("꽃가루", WeatherScoreTables.pollen(weather.getParsedPollenRisk()), 0.45)
                .add("미세먼지", WeatherScoreTables.pm10(weather.getParsedPm10Value()), 0.20)
                .add("초미세먼지", WeatherScoreTables.pm25(weather.getParsedPm25Value()), 0.20)
                .add("건조", WeatherScoreTables.humidityDry(weather.getParsedHumidity()), 0.15)
                .build();
    }
}