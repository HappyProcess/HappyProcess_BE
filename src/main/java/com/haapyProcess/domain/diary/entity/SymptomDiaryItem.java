package com.haapyProcess.domain.diary.entity;

import com.haapyProcess.domain.condition.entity.Condition;
import jakarta.persistence.*;
import lombok.*;

/**
 * 증상 일기의 질환별 증상 강도 항목. 보유 질환(Condition) 하나당 강도(1~5)를 기록한다.
 */
@Entity
@Table(name = "DIARY_SYMPTOM")
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class SymptomDiaryItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "DIARY_SYMPTOM_ID")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "DIARY_ID")
    private SymptomDiary diary;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CONDITION_ID")
    private Condition condition;

    // 증상 강도 1~5
    @Column(name = "INTENSITY", nullable = false)
    private int intensity;

    void assignDiary(SymptomDiary diary) {
        this.diary = diary;
    }
}
