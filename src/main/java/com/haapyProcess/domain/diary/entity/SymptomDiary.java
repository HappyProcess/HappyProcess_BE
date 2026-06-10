package com.haapyProcess.domain.diary.entity;

import com.haapyProcess.domain.member.entity.Member;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 증상 일기 (하루 1건). 작성 시점의 날씨를 스냅샷으로 함께 저장한다.
 * member + entryDate 조합이 유니크하며, 같은 날 재작성 시 기존 일기를 갱신(upsert)한다.
 */
@Entity
@Table(
        name = "DIARY",
        uniqueConstraints = @UniqueConstraint(name = "UK_DIARY_MEMBER_DATE", columnNames = {"MEMBER_ID", "ENTRY_DATE"})
)
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class SymptomDiary {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "DIARY_ID")
    private Long diaryId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MEMBER_ID")
    private Member member;

    @Column(name = "ENTRY_DATE", nullable = false)
    private LocalDate entryDate;

    @Column(name = "MEMO", columnDefinition = "TEXT")
    private String memo;

    // --- 작성 시점 날씨 스냅샷 (수집 실패 시 모두 null 가능) ---
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

    @Column(name = "CREATED_AT", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "UPDATED_AT", nullable = false)
    private LocalDateTime updatedAt;

    @Builder.Default
    @OneToMany(mappedBy = "diary", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SymptomDiaryItem> items = new ArrayList<>();

    @PrePersist
    void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    /** 자유 텍스트 메모를 갱신한다. */
    public void updateMemo(String memo) {
        this.memo = memo;
    }

    /** 날씨 스냅샷을 채운다(작성/재작성 시점 기준). */
    public void applyWeatherSnapshot(String regionName, String temperature, String humidity,
                                     String weatherCondition, String pm10Value, String pm10Grade,
                                     String pm25Value, String pm25Grade, String pollenRiskLevel,
                                     String uvRiskLevel) {
        this.regionName = regionName;
        this.temperature = temperature;
        this.humidity = humidity;
        this.weatherCondition = weatherCondition;
        this.pm10Value = pm10Value;
        this.pm10Grade = pm10Grade;
        this.pm25Value = pm25Value;
        this.pm25Grade = pm25Grade;
        this.pollenRiskLevel = pollenRiskLevel;
        this.uvRiskLevel = uvRiskLevel;
    }

    /** 기존 증상 항목을 모두 비우고 새 항목으로 교체한다. */
    public void replaceItems(List<SymptomDiaryItem> newItems) {
        this.items.clear();
        for (SymptomDiaryItem item : newItems) {
            addItem(item);
        }
    }

    public void addItem(SymptomDiaryItem item) {
        item.assignDiary(this);
        this.items.add(item);
    }
}
