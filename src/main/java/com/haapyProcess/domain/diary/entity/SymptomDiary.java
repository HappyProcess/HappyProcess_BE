package com.haapyProcess.domain.diary.entity;

import com.haapyProcess.domain.member.entity.Member;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 증상 일기 (하루 1건). 작성 시점의 날씨를 위치(HOME/WORK)별로 스냅샷 저장한다.
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

    @Column(name = "CREATED_AT", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "UPDATED_AT", nullable = false)
    private LocalDateTime updatedAt;

    @Builder.Default
    @OneToMany(mappedBy = "diary", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SymptomDiaryItem> items = new ArrayList<>();

    // 위치(HOME/WORK)별 날씨 스냅샷
    @Builder.Default
    @OneToMany(mappedBy = "diary", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DiaryWeather> weathers = new ArrayList<>();

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

    /** 기존 증상 항목을 모두 비우고 새 항목으로 교체한다. */
    public void replaceItems(List<SymptomDiaryItem> newItems) {
        this.items.clear();
        for (SymptomDiaryItem item : newItems) {
            item.assignDiary(this);
            this.items.add(item);
        }
    }

    /** 기존 날씨 스냅샷을 모두 비우고 새 스냅샷으로 교체한다. */
    public void replaceWeathers(List<DiaryWeather> newWeathers) {
        this.weathers.clear();
        for (DiaryWeather w : newWeathers) {
            w.assignDiary(this);
            this.weathers.add(w);
        }
    }
}
