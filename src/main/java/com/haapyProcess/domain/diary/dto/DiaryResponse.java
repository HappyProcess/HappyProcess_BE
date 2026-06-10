package com.haapyProcess.domain.diary.dto;

import com.haapyProcess.domain.diary.entity.DiaryWeather;
import com.haapyProcess.domain.diary.entity.SymptomDiary;
import com.haapyProcess.domain.diary.entity.SymptomDiaryItem;
import com.haapyProcess.domain.location.entity.LocationType;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Builder
public class DiaryResponse {

    private Long diaryId;
    private LocalDate entryDate;
    private String memo;
    private List<SymptomResponse> symptoms;
    // 위치(HOME/WORK)별 날씨 스냅샷
    private List<WeatherSnapshot> weathers;

    public static DiaryResponse from(SymptomDiary diary) {
        List<SymptomResponse> symptoms = diary.getItems().stream()
                .map(SymptomResponse::from)
                .toList();

        List<WeatherSnapshot> weathers = diary.getWeathers().stream()
                .map(WeatherSnapshot::from)
                .toList();

        return DiaryResponse.builder()
                .diaryId(diary.getDiaryId())
                .entryDate(diary.getEntryDate())
                .memo(diary.getMemo())
                .symptoms(symptoms)
                .weathers(weathers)
                .build();
    }

    @Getter
    @Builder
    public static class SymptomResponse {
        private Long conditionId;
        private String conditionName;
        private int intensity;

        static SymptomResponse from(SymptomDiaryItem item) {
            return SymptomResponse.builder()
                    .conditionId(item.getCondition().getConditionId())
                    .conditionName(item.getCondition().getConditionName())
                    .intensity(item.getIntensity())
                    .build();
        }
    }

    @Getter
    @Builder
    public static class WeatherSnapshot {
        private LocationType locationType;
        private String regionName;
        private String temperature;
        private String humidity;
        private String weatherCondition;
        private String pm10Value;
        private String pm10Grade;
        private String pm25Value;
        private String pm25Grade;
        private String pollenRiskLevel;
        private String uvRiskLevel;

        static WeatherSnapshot from(DiaryWeather w) {
            return WeatherSnapshot.builder()
                    .locationType(w.getLocationType())
                    .regionName(w.getRegionName())
                    .temperature(w.getTemperature())
                    .humidity(w.getHumidity())
                    .weatherCondition(w.getWeatherCondition())
                    .pm10Value(w.getPm10Value())
                    .pm10Grade(w.getPm10Grade())
                    .pm25Value(w.getPm25Value())
                    .pm25Grade(w.getPm25Grade())
                    .pollenRiskLevel(w.getPollenRiskLevel())
                    .uvRiskLevel(w.getUvRiskLevel())
                    .build();
        }
    }
}
