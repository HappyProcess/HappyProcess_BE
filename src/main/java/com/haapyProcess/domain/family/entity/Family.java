package com.haapyProcess.domain.family.entity;

import com.haapyProcess.domain.member.entity.Member;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "FAMILY")
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Family {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "FAMILY_ID")
    private Long familyId;

    // 가족을 "추가한" 주체 (예: 나)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "USER_ID", nullable = false)
    private Member user;

    // 가족으로 "추가된" 대상 (예: 우리 아빠)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "RELATIVE_ID", nullable = false)
    private Member relative;

    // 기획서 7번: 가족 알림 켜기/끄기 스위치
    @Column(name = "IS_ALERT_ENABLED", nullable = false)
    @Builder.Default
    private boolean isAlertEnabled = true;

    // 알림 토글 비즈니스 로직
    public void toggleAlert(boolean isAlertEnabled) {
        this.isAlertEnabled = isAlertEnabled;
    }
}