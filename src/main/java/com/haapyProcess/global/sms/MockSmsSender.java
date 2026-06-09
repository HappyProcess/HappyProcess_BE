package com.haapyProcess.global.sms;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * 실제 발송 없이 로그만 남기는 Mock 구현체.
 * coolsms.enabled 가 false 이거나 미설정이면 이 빈이 활성화된다.
 * (CoolSMS 키가 없는 로컬/개발 환경에서도 앱이 정상 동작하도록 보장)
 */
@Slf4j
@Component
@ConditionalOnProperty(prefix = "coolsms", name = "enabled", havingValue = "false", matchIfMissing = true)
public class MockSmsSender implements SmsSender {

    @Override
    public SmsResult send(String to, String text) {
        String fakeId = "MOCK-" + UUID.randomUUID();
        log.info("[MockSmsSender] 문자 발송(가짜) → to: {}, text: {}", to, text);
        return SmsResult.success(fakeId);
    }
}
