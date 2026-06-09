package com.haapyProcess.domain.alert.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 위험 알림 문자 발송 이력. 성공/실패와 공급자 메시지 ID를 기록한다.
 */
@Entity
@Table(name = "SMS_SEND_LOG")
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class SmsSendLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "SMS_SEND_LOG_ID")
    private Long smsSendLogId;

    @Column(name = "MEMBER_ID")
    private Long memberId;

    @Column(name = "PHONE_NUMBER", length = 20)
    private String phoneNumber;

    @Column(name = "MESSAGE", length = 500)
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(name = "STATUS", length = 20, nullable = false)
    private SmsSendStatus status;

    @Column(name = "PROVIDER_MESSAGE_ID", length = 100)
    private String providerMessageId;

    @Column(name = "ERROR_MESSAGE", length = 500)
    private String errorMessage;

    @Column(name = "CREATED_AT", nullable = false)
    private LocalDateTime createdAt;

    public enum SmsSendStatus {
        SUCCESS, FAILED
    }
}
