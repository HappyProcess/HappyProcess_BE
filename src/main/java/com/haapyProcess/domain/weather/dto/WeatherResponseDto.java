package com.haapyProcess.domain.weather.dto;

import lombok.Builder;
import lombok.Getter;
import java.util.List;

@Getter
@Builder
public class WeatherResponseDto {

    private String regionName; // 위치 정보

    // 기상청 단기예보
    private String temperature; // 기온
    private String humidity;    // 습도
    private String weatherCondition; // 상태

    // 에어코리아 실시간 미세먼지
    private String pm10Value; // 미세먼지 수치
    private String pm10Grade; // 미세먼지 등급
    private String pm25Value; // 초미세먼지 수치
    private String pm25Grade; // 초미세먼지 등급

    // 기상청 보건/생활지수
    private String pollenRiskLevel; // 꽃가루 위험도
    private String uvRiskLevel;     // 자외선 위험도

    private List<WeatherHourlyDto> hourlyForecasts;

    public double getParsedPollenRisk() {
        try {
            return Double.parseDouble(this.pollenRiskLevel);
        } catch (Exception e) {
            return 0.0;
        }
    }

    public double getParsedUvRisk() {
        try {
            return Double.parseDouble(this.uvRiskLevel);
        } catch (Exception e) {
            return 0.0;
        }
    }

    public double getParsedPm10Value() {
        try {
            return Double.parseDouble(this.pm10Value);
        } catch (Exception e) {
            return 0.0;
        }
    }

    public double getParsedPm25Value() {
        try {
            return Double.parseDouble(this.pm25Value);
        } catch (Exception e) {
            return 0.0;
        }
    }

    public double getParsedHumidity() {
        try {
            return Double.parseDouble(this.humidity);
        } catch (Exception e) {
            return 50.0;
        }
    }

    public double getParsedCurrentTemp() {
        try {
            return Double.parseDouble(this.temperature);
        } catch (Exception e) {
            return 20.0;
        }
    }

    // 6시간 내 기온 급감 계산기 (현재 기온 - 6시간 내 최저 예상 기온)
    public double getTempDropIn6Hours() {
        if (hourlyForecasts == null || hourlyForecasts.isEmpty()) return 0.0;

        double currentTemp = getParsedCurrentTemp();
        double minFutureTemp = hourlyForecasts.stream()
                .mapToDouble(h -> {
                    try {
                        return Double.parseDouble(h.getTemperature());
                    } catch (Exception e) {
                        return currentTemp;
                    }
                })
                .min().orElse(currentTemp);

        return currentTemp - minFutureTemp;
    }

    // 현재 강수 형태(PTY) 숫자 변환기 (0:없음, 1:비, 2:비/눈, 3:눈, 4:소나기)
    public int getParsedCurrentPty() {
        if (hourlyForecasts == null || hourlyForecasts.isEmpty()) return 0;
        try {
            return Integer.parseInt(hourlyForecasts.get(0).getPty());
        } catch (Exception e) {
            return 0;
        }
    }
}