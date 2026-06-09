package com.haapyProcess.domain.alert.listener;

import com.haapyProcess.domain.alert.entity.SmsSendLog;
import com.haapyProcess.domain.alert.event.NotificationCreatedEvent;
import com.haapyProcess.domain.alert.repository.SmsSendLogRepository;
import com.haapyProcess.global.sms.SmsResult;
import com.haapyProcess.global.sms.SmsSender;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * 위험 알림 적재 트랜잭션이 커밋된 후, 비동기로 문자를 발송하고 결과를 기록한다.
 * 발송이 실패하거나 느려도 알림 적재/스케줄러에는 영향을 주지 않는다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SmsNotificationListener {

    private final SmsSender smsSender;
    private final SmsSendLogRepository smsSendLogRepository;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(NotificationCreatedEvent event) {
        if (event.phoneNumber() == null || event.phoneNumber().isBlank()) {
            log.debug("문자 발송 스킵 - 수신자 {} 번호 미등록", event.receiverId());
            return;
        }

        SmsResult result = smsSender.send(event.phoneNumber(), event.message());

        SmsSendLog logEntry = SmsSendLog.builder()
                .memberId(event.receiverId())
                .phoneNumber(event.phoneNumber())
                .message(event.message())
                .status(result.success() ? SmsSendLog.SmsSendStatus.SUCCESS : SmsSendLog.SmsSendStatus.FAILED)
                .providerMessageId(result.providerMessageId())
                .errorMessage(result.errorMessage())
                .createdAt(LocalDateTime.now())
                .build();

        smsSendLogRepository.save(logEntry);
    }
}
