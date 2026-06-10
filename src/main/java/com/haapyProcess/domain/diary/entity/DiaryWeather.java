package com.haapyProcess.domain.diary.entity;

import com.haapyProcess.domain.location.entity.LocationType;
import jakarta.persistence.*;
import lombok.*;

/**
 * 증상 일기에 위치(HOME/WORK)별로 저장되는 날씨 스냅샷.
 * 한 일기에 집/직장(학교) 두 위치의 날씨가 각각 기록된다.
 */
@Entity
@Table(name = "DIARY_WEATHER", indexes = @Index(name = "idx_diary_weather_diary", columnList = "DIARY_ID"))
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class DiaryWeather {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "DIARY_WEATHER_ID")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "DIARY_ID")
    private SymptomDiary diary;

    @Enumerated(EnumType.STRING)
    @Column(name = "LOCATION_TYPE", length = 20)
    private LocationType locationType;

    @Column(name = "REGION_NAME", length = 50)
    private String regionName;

    @Column(name = "TEMPERATURE", length = 20)
    private String temperature;

    @Column(name = "HUMIDITY", length = 20)
    private String humidity;

    @Column(name = "WEATHER_CONDITION", length = 30)
    private String weatherCondition;

    @Column(name = "PM10_VALUE", length = 20)
    private String pm10Value;

    @Column(name = "PM10_GRADE", length = 20)
    private String pm10Grade;

    @Column(name = "PM25_VALUE", length = 20)
    private String pm25Value;

    @Column(name = "PM25_GRADE", length = 20)
    private String pm25Grade;

    @Column(name = "POLLEN_RISK_LEVEL", length = 20)
    private String pollenRiskLevel;

    @Column(name = "UV_RISK_LEVEL", length = 20)
    private String uvRiskLevel;

    void assignDiary(SymptomDiary diary) {
        this.diary = diary;
    }
}
