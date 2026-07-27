package com.haapyProcess.domain.analysis.rule;

import com.haapyProcess.domain.analysis.dto.RiskAnalysisResult.FactorGuide;
import com.haapyProcess.domain.analysis.score.WeatherScore;
import com.haapyProcess.domain.member.entity.PrecipPreference;
import com.haapyProcess.domain.weather.dto.WeatherResponseDto;

import java.util.List;

public interface DiseaseRiskRule {

    /**
     * 해당 규칙이 담당하는 질병의 고유 ID (Condition ID)를 반환합니다.
     * 일반인(질병 없음)은 0L, 실제 질병은 1L부터 시작합니다.
     */
    Long getConditionId();
    String getDiseaseName();

    // 날씨 원인별 맞춤 가이드 분석 반환 (비어 있으면 안전)
    List<FactorGuide> evaluateFactorGuides(WeatherResponseDto weather);

    /**
     * 질환별 가중치를 적용한 날씨 점수(0~100)와 점수 하락의 주요 원인 지수를 반환합니다.
     * 0점 = 최악의 날씨, 100점 = 최고의 날씨.
     * precipPreference는 질병 없는 사용자의 강수 불편도 산정에만 사용됩니다.
     */
    WeatherScore evaluateWeatherScore(WeatherResponseDto weather, PrecipPreference precipPreference);

}