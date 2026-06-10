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

    @Override
    public WeatherScore evaluateWeatherScore(WeatherResponseDto weather, PrecipPreference precipPreference) {
        int pm25 = WeatherScoreTables.pm25(weather.getParsedPm25Value());
        int pm10 = WeatherScoreTables.pm10(weather.getParsedPm10Value());
        int temp = WeatherScoreTables.temp(weather.getParsedCurrentTemp());
        double tempChangeVal = Math.max(weather.getTempDropIn6Hours(), weather.getTempRiseIn6Hours());
        int tempChange = WeatherScoreTables.tempChange(tempChangeVal);

        int month = java.time.LocalDate.now().getMonthValue();
        boolean pollenOffSeason = (month == 7 || month == 11 || month == 12 || month == 1 || month == 2);

        if (pollenOffSeason) {
            // 꽃가루 계수(0.35) 제외 후 나머지 변수를 재정규화 (÷0.65)
            return ScoreBuilder.create()
                    .add("초미세먼지", pm25, 0.30 / 0.65)
                    .add("미세먼지", pm10, 0.20 / 0.65)
                    .add("기온", temp, 0.10 / 0.65)
                    .add("기온 급변", tempChange, 0.05 / 0.65)
                    .build();
        }
        int pollen = WeatherScoreTables.pollen(weather.getParsedPollenRisk());
        return ScoreBuilder.create()
                .add("꽃가루", pollen, 0.35)
                .add("초미세먼지", pm25, 0.30)
                .add("미세먼지", pm10, 0.20)
                .add("기온", temp, 0.10)
                .add("기온 급변", tempChange, 0.05)
                .build();
    }
}
