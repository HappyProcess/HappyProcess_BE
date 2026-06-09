package com.haapyProcess.domain.alert.event;

/**
 * 위험 알림이 적재될 때 발행되는 이벤트.
 * 트랜잭션 커밋 후 리스너가 받아 문자 발송을 처리한다.
 */
public record NotificationCreatedEvent(
        Long receiverId,
        String phoneNumber,
        String message
) {
}
