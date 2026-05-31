package com.haapyProcess.domain.alert.entity;

import com.haapyProcess.domain.member.entity.Member;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "ALERT")
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Alert {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ALERT_ID")
    private Long alertId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MEMBER_ID")
    private Member member;

    @Column(name = "ALERT_TIME", length = 5, nullable = false)
    private String alertTime; // 예: "08:00", "18:30" 형식으로 저장

    @Column(name = "IS_ENABLE", nullable = false)
    private boolean isEnable; // 알림 켜짐(true) / 꺼짐(false) 상태

    // 알림 활성화 상태를 변경하는 비즈니스 메서드
    public void toggleEnable(boolean isEnable) {
        this.isEnable = isEnable;
    }

    public void updateAlertTime(String alertTime) {
        this.alertTime = alertTime;
    }
}