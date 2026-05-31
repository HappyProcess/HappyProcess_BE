package com.haapyProcess.domain.alert.entity;

import com.haapyProcess.domain.member.entity.Member;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "NOTIFICATION_HISTORY")
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class NotificationHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "HISTORY_ID")
    private Long historyId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MEMBER_ID")
    private Member member;

    // 어떤 질병 때문에 이 알림이 울렸는지 원인 추적용
    @Column(name = "DISEASE_IDS", length = 50)
    private String diseaseIds; // 예: "1, 3" (추후 조회용)

    @Column(name = "DISEASE_NAMES", length = 100)
    private String diseaseNames; // 예: "천식, 고혈압"

    @Column(name = "MESSAGE", length = 255, nullable = false)
    private String message; // 예: "[천식] 현재 위험도가 높습니다. 외출 시 주의하세요."

    @Column(name = "IS_READ", nullable = false)
    private boolean isRead; // 유저가 알림을 읽었는지 여부 (새 알림 뱃지 표시용)

    @Column(name = "CREATED_AT", updatable = false)
    private LocalDateTime createdAt; // 알림이 발송된 정확한 날짜와 시간

    // createdAt이 명시되지 않은 경우에만 현재 시각으로 세팅 (스케줄러는 알람 시각을 직접 주입)
    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
    }

    // 알림 읽음 처리 비즈니스 메서드
    public void markAsRead() {
        this.isRead = true;
    }
}