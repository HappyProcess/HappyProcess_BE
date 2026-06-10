package com.haapyProcess.domain.analysis.score;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

/**
 * 질환별 날씨 점수 산출 결과.
 * score는 0(최악)~100(최고), factors는 점수를 크게 깎은 주요 날씨 지수 이름(최대 2개, 영향 큰 순).
 */
@Getter
@AllArgsConstructor
public class WeatherScore {
    private final int score;
    private final List<String> factors;
}
