package com.haapyProcess.domain.diary.dto;

import com.haapyProcess.domain.diary.entity.SymptomDiary;
import com.haapyProcess.domain.diary.entity.SymptomDiaryItem;
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
    private WeatherSnapshot weather;

    public static DiaryResponse from(SymptomDiary diary) {
        List<SymptomResponse> symptoms = diary.getItems().stream()
                .map(SymptomResponse::from)
                .toList();

        return DiaryResponse.builder()
                .diaryId(diary.getDiaryId())
                .entryDate(diary.getEntryDate())
                .memo(diary.getMemo())
                .symptoms(symptoms)
                .weather(WeatherSnapshot.from(diary))
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

        static WeatherSnapshot from(SymptomDiary diary) {
            return WeatherSnapshot.builder()
                    .regionName(diary.getRegionName())
                    .temperature(diary.getTemperature())
                    .humidity(diary.getHumidity())
                    .weatherCondition(diary.getWeatherCondition())
                    .pm10Value(diary.getPm10Value())
                    .pm10Grade(diary.getPm10Grade())
                    .pm25Value(diary.getPm25Value())
                    .pm25Grade(diary.getPm25Grade())
                    .pollenRiskLevel(diary.getPollenRiskLevel())
                    .uvRiskLevel(diary.getUvRiskLevel())
                    .build();
        }
    }
}
