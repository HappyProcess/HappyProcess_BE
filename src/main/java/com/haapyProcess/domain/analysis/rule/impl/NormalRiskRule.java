package com.haapyProcess.domain.analysis.rule.impl;

import com.haapyProcess.domain.analysis.criteria.WeatherRiskCriteria;
import com.haapyProcess.domain.analysis.dto.RiskAnalysisResult.FactorGuide;
import com.haapyProcess.domain.analysis.rule.DiseaseRiskRule;
import com.haapyProcess.domain.analysis.score.WeatherScoreTables;
import com.haapyProcess.domain.member.entity.PrecipPreference;
import com.haapyProcess.domain.weather.dto.WeatherResponseDto;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class NormalRiskRule implements DiseaseRiskRule {

    @Override
    public Long getConditionId() {
        return 0L;
    }

    @Override
    public String getDiseaseName() {
        return "질병없음";
    }

    @Override
    public List<FactorGuide> evaluateFactorGuides(WeatherResponseDto weather) {
        List<FactorGuide> guides = new ArrayList<>();
        int month = java.time.LocalDate.now().getMonthValue();
        boolean isSummer = (month >= 5 && month <= 10);

        double tempChange = Math.max(weather.getTempDropIn6Hours(), weather.getTempRiseIn6Hours());
        double changeLimit = isSummer ? WeatherRiskCriteria.TEMP_CHANGE_GENERAL_SUMMER.getValue()
                : WeatherRiskCriteria.TEMP_CHANGE_GENERAL_WINTER.getValue();
        int pty = weather.getParsedCurrentPty();

        if (weather.getParsedPm10Value() >= WeatherRiskCriteria.PM10_BAD_MIN.getValue()) {
            guides.add(new FactorGuide("미세먼지 (나쁨)", "미세먼지가 나쁨 수준이에요. 외출 시 마스크를 착용하고 장시간 야외활동을 자제하세요."));
        }

        if (weather.getParsedPm25Value() >= WeatherRiskCriteria.PM25_BAD_MIN.getValue()) {
            guides.add(new FactorGuide("초미세먼지 (나쁨)", "초미세먼지가 나쁨 수준이에요. KF94 마스크를 착용하고 외출을 자제하세요."));
        }

        if (weather.getParsedCurrentTemp() >= WeatherRiskCriteria.TEMP_HEAT_WAVE.getValue()) {
            guides.add(new FactorGuide("폭염", "현재 기온이 매우 높아요. 야외활동을 자제하고 수분섭취에 신경 쓰세요."));
        } else if (weather.getParsedCurrentTemp() <= WeatherRiskCriteria.TEMP_COLD_WAVE.getValue()) {
            guides.add(new FactorGuide("한파", "현재 기온이 매우 낮아요. 야외활동을 자제하고 보온에 신경 쓰세요."));
        }

        if (weather.getParsedUvRisk() >= WeatherRiskCriteria.UV_VERY_HIGH_MIN.getValue()) {
            guides.add(new FactorGuide("자외선 (매우높음)", "자외선이 매우 강해요. 외출 시 자외선 차단제를 꼼꼼히 바르세요."));
        }

        if (pty >= 1 && pty <= 3) {
            guides.add(new FactorGuide("강수 (비/눈)", "비/눈이 예보되어 있어요. 우산을 챙기고 외출 시 미끄럼에 주의하세요."));
        }

        if (tempChange >= changeLimit) {
            guides.add(new FactorGuide("기온 급변", "기온이 급격히 변하고 있어요. 체온 관리에 주의하세요."));
        }

        return guides;
    }

    @Override
    public int evaluateWeatherScore(WeatherResponseDto weather, PrecipPreference precipPreference) {
        // 건강 위험도 (공통)
        double health = WeatherScoreTables.pm10(weather.getParsedPm10Value()) * 0.35
                + WeatherScoreTables.pm25(weather.getParsedPm25Value()) * 0.35
                + WeatherScoreTables.temp(weather.getParsedCurrentTemp()) * 0.20
                + WeatherScoreTables.uv(weather.getParsedUvRisk()) * 0.10;

        // 강수 불편도 (개인 선호 기반 가산점)
        double precip = WeatherScoreTables.precipNormal(weather.getParsedCurrentPty(), precipPreference) * 0.13;

        double raw = Math.min(health + precip, 100.0);
        return 100 - (int) Math.round(raw);
    }
}
