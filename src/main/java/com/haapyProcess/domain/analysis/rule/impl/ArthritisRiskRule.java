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
import java.util.Set;

@Component
public class ArthritisRiskRule implements DiseaseRiskRule {

    @Override
    public Long getConditionId() {
        return 10L;
    }

    @Override
    public String getDiseaseName() {
        return "관절염";
    }

    @Override
    public List<FactorGuide> evaluateFactorGuides(WeatherResponseDto weather) {
        List<FactorGuide> guides = new ArrayList<>();
        int pty = weather.getParsedCurrentPty();

        if (weather.getTempDropIn6Hours() >= WeatherRiskCriteria.TEMP_DROP_SUDDEN.getValue()) {
            guides.add(new FactorGuide("기온 급감", "기온이 급격히 낮아지고 있어요. 관절을 따뜻하게 보호하고 외출 전 실내에서 스트레칭으로 관절을 충분히 풀어주세요."));
        }

        if (pty >= 1 && pty <= 3) {
            guides.add(new FactorGuide("강수 (비/눈)", "비/눈이 예보되어 있어요. 기압 변화로 관절 통증이 심해질 수 있어요. 무리한 활동을 자제하고 실내 활동을 권장해요."));
        }

        return guides;
    }

    @Override
    public WeatherScore evaluateWeatherScore(WeatherResponseDto weather, PrecipPreference precipPreference) {
        // 6시간 기온 급감 (하강 폭만 사용)
        int tempDrop = WeatherScoreTables.tempChange(weather.getTempDropIn6Hours());
        // 강수형태 코드 1(비), 2(비/눈), 3(눈)만 적용
        int precip = WeatherScoreTables.precipSensitive(weather.getParsedCurrentPty(), Set.of(1, 2, 3));

        return ScoreBuilder.create()
                .add("기온 급강하", tempDrop, 0.60)
                .add("강수", precip, 0.40)
                .build();
    }
}
