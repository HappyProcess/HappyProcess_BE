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
import java.util.Set;

@Component
public class ElderlyRiskRule implements DiseaseRiskRule {

    @Override
    public Long getConditionId() {
        return 13L;
    }

    @Override
    public String getDiseaseName() {
        return "고령";
    }

    @Override
    public List<FactorGuide> evaluateFactorGuides(WeatherResponseDto weather) {
        List<FactorGuide> guides = new ArrayList<>();
        int pty = weather.getParsedCurrentPty();

        if (weather.getParsedCurrentTemp() >= WeatherRiskCriteria.TEMP_HEAT_WAVE.getValue()) {
            guides.add(new FactorGuide("폭염", "현재 기온이 매우 높아요. 어르신은 폭염에 특히 취약해요. 낮 시간 외출을 삼가고 무더위쉼터를 이용하세요."));
        } else if (weather.getParsedCurrentTemp() <= WeatherRiskCriteria.TEMP_COLD_WAVE.getValue()) {
            guides.add(new FactorGuide("한파", "매우 추운 날씨예요. 어르신은 낙상과 심뇌혈관 위험이 높아요. 외출을 자제하고 꼭 외출해야 한다면 미끄럼 방지 신발을 신으세요."));
        }

        if (weather.getParsedPm10Value() >= WeatherRiskCriteria.PM10_NORMAL_MIN.getValue()) {
            guides.add(new FactorGuide("미세먼지", "미세먼지가 보통 이상이에요. 어르신은 호흡기가 약하니 외출을 자제하고 마스크를 착용하세요."));
        }

        if (pty == 2 || pty == 3) {
            guides.add(new FactorGuide("강수 (비/눈)", "눈/비가 예보되어 있어요. 빙판길 낙상에 주의하고 미끄럼 방지 신발을 신으세요. 가급적 외출을 자제하세요."));
        }

        return guides;
    }

    @Override
    public int evaluateWeatherScore(WeatherResponseDto weather, PrecipPreference precipPreference) {
        double changeVal = Math.max(weather.getTempDropIn6Hours(), weather.getTempRiseIn6Hours());
        // 고령 강수형태는 빙판길 낙상 위험 중심으로 코드 2(비/눈), 3(눈)만 적용
        double raw = WeatherScoreTables.temp(weather.getParsedCurrentTemp()) * 0.30
                + WeatherScoreTables.tempChange(changeVal) * 0.30
                + WeatherScoreTables.pm10(weather.getParsedPm10Value()) * 0.20
                + WeatherScoreTables.precipSensitive(weather.getParsedCurrentPty(), Set.of(2, 3)) * 0.20;
        return 100 - (int) Math.round(raw);
    }
}
