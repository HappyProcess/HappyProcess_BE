package com.haapyProcess.domain.analysis.score;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * 날씨 지수별 가중 기여도를 누적해 최종 날씨 점수와 주요 원인 지수를 산출한다.
 * <p>
 * 각 지수의 원점수(클수록 안 좋은 날씨)에 가중치를 곱한 '가중 기여 점수'를 모은 뒤,
 * 가중 기여 점수가 임계값 이상인 지수만 영향이 큰 순서로 최대 2개까지 원인으로 노출한다.
 */
public final class ScoreBuilder {

    /** 이 가중 기여 점수 이상인 지수만 점수 하락의 주요 원인으로 노출한다. */
    private static final double FACTOR_THRESHOLD = 20.0;
    /** 노출할 원인 지수 최대 개수. */
    private static final int MAX_FACTORS = 2;

    private record Contribution(String name, double weighted) {
    }

    private final List<Contribution> contributions = new ArrayList<>();

    private ScoreBuilder() {
    }

    public static ScoreBuilder create() {
        return new ScoreBuilder();
    }

    /**
     * @param factorName 날씨 지수 이름 (예: "초미세먼지")
     * @param rawScore   해당 지수의 원점수 (0~100, 클수록 안 좋음)
     * @param weight     가중치
     */
    public ScoreBuilder add(String factorName, int rawScore, double weight) {
        contributions.add(new Contribution(factorName, rawScore * weight));
        return this;
    }

    public WeatherScore build() {
        double raw = Math.min(
                contributions.stream().mapToDouble(Contribution::weighted).sum(),
                100.0);
        int score = 100 - (int) Math.round(raw);

        List<String> factors = contributions.stream()
                .filter(c -> c.weighted() >= FACTOR_THRESHOLD)
                .sorted(Comparator.comparingDouble(Contribution::weighted).reversed())
                .limit(MAX_FACTORS)
                .map(Contribution::name)
                .toList();

        return new WeatherScore(score, factors);
    }
}
