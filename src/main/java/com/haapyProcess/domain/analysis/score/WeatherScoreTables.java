package com.haapyProcess.domain.analysis.score;

import com.haapyProcess.domain.member.entity.PrecipPreference;

import java.util.Set;

/**
 * 날씨 점수 기능의 구간별 점수표.
 * 각 변수의 측정값을 0~100 사이의 '원점수(클수록 안 좋은 날씨)'로 변환한다.
 * 최종 날씨 점수는 100 - 가중합 원점수로 산출한다 (0점=최악, 100점=최고).
 */
public final class WeatherScoreTables {

    private WeatherScoreTables() {
    }

    // PM10 (㎍/㎥)
    public static int pm10(double v) {
        if (v <= 15) return 0;
        if (v <= 30) return 10;
        if (v <= 50) return 25;
        if (v <= 80) return 40;
        if (v <= 100) return 60;
        if (v <= 150) return 75;
        if (v <= 200) return 90;
        return 100;
    }

    // PM2.5 (㎍/㎥)
    public static int pm25(double v) {
        if (v <= 8) return 0;
        if (v <= 15) return 10;
        if (v <= 25) return 25;
        if (v <= 35) return 40;
        if (v <= 50) return 60;
        if (v <= 75) return 75;
        if (v <= 100) return 90;
        return 100;
    }

    // 자외선지수
    public static int uv(double u) {
        if (u <= 1) return 0;
        if (u <= 2) return 10;
        if (u <= 4) return 20;
        if (u <= 5) return 35;
        if (u <= 6) return 55;
        if (u <= 7) return 65;
        if (u <= 9) return 80;
        if (u <= 10) return 90;
        return 100;
    }

    // 꽃가루지수 (기상청 0~3 단계)
    public static int pollen(double p) {
        if (p <= 0) return 0;
        if (p <= 1) return 30;
        if (p <= 2) return 70;
        return 100;
    }

    // 기온 고온 (℃)
    public static int tempHeat(double t) {
        if (t < 25) return 0;
        if (t < 28) return 10;
        if (t < 30) return 20;
        if (t < 33) return 40;
        if (t < 35) return 70;
        if (t < 37) return 85;
        return 100;
    }

    // 기온 저온 (℃)
    public static int tempCold(double t) {
        if (t >= -3) return 0;
        if (t >= -6) return 10;
        if (t >= -9) return 25;
        if (t >= -12) return 50;
        if (t >= -15) return 75;
        if (t >= -18) return 90;
        return 100;
    }

    // 기온 (고온/저온 중 더 위험한 쪽을 채택)
    public static int temp(double t) {
        return Math.max(tempHeat(t), tempCold(t));
    }

    // 습도 건조 (%) - 낮을수록 점수 높음
    public static int humidityDry(double h) {
        if (h >= 60) return 0;
        if (h >= 50) return 5;
        if (h >= 40) return 15;
        if (h >= 35) return 40;
        if (h >= 30) return 65;
        if (h >= 20) return 85;
        return 100;
    }

    // 습도 습함 (%) - 높을수록 점수 높음
    public static int humidityWet(double h) {
        if (h < 70) return 0;
        if (h < 75) return 5;
        if (h < 80) return 15;
        if (h < 85) return 40;
        if (h < 90) return 65;
        if (h < 95) return 85;
        return 100;
    }

    // 6시간 기온 급변/급감 (℃) - 절대 변화량
    public static int tempChange(double c) {
        double v = Math.abs(c);
        if (v < 2) return 0;
        if (v < 3) return 15;
        if (v < 4) return 30;
        if (v < 5) return 50;
        if (v < 6) return 65;
        if (v < 7) return 80;
        return 100;
    }

    // 강수형태 - 질병 없는 사용자 (비선호 설정 기반)
    public static int precipNormal(int pty, PrecipPreference preference) {
        if (preference == null || preference == PrecipPreference.NONE) return 0;
        if (preference == PrecipPreference.RAIN) {
            return switch (pty) {
                case 1 -> 100; // 비
                case 2 -> 70;  // 비/눈
                case 4 -> 50;  // 소나기
                case 6 -> 50;  // 빗방울/눈날림
                case 5 -> 30;  // 빗방울
                default -> 0;  // 눈(3)/눈날림(7)/없음(0)
            };
        }
        // SNOW
        return switch (pty) {
            case 3 -> 100; // 눈
            case 2 -> 70;  // 비/눈
            case 6 -> 40;  // 빗방울/눈날림
            case 7 -> 40;  // 눈날림
            default -> 0;  // 비(1)/소나기(4)/빗방울(5)/없음(0)
        };
    }

    // 강수형태 - 민감군 (질병 있는 사용자). allowedCodes 외의 코드는 0점 처리.
    public static int precipSensitive(int pty, Set<Integer> allowedCodes) {
        if (!allowedCodes.contains(pty)) return 0;
        return switch (pty) {
            case 3 -> 100; // 눈
            case 2 -> 80;  // 비/눈
            case 1 -> 60;  // 비
            case 7 -> 60;  // 눈날림
            case 6 -> 50;  // 빗방울/눈날림
            case 4 -> 40;  // 소나기
            case 5 -> 30;  // 빗방울
            default -> 0;  // 없음(0)
        };
    }
}
