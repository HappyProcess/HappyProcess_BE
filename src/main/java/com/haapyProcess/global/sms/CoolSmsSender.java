package com.haapyProcess.global.sms;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import net.nurigo.sdk.NurigoApp;
import net.nurigo.sdk.message.model.Message;
import net.nurigo.sdk.message.request.SingleMessageSendingRequest;
import net.nurigo.sdk.message.response.SingleMessageSentResponse;
import net.nurigo.sdk.message.service.DefaultMessageService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * CoolSMS(SOLAPI) 기반 실제 문자 발송 구현체.
 * coolsms.enabled=true 일 때만 빈으로 등록된다.
 */
@Slf4j
@Component
@ConditionalOnProperty(prefix = "coolsms", name = "enabled", havingValue = "true")
public class CoolSmsSender implements SmsSender {

    private static final String SOLAPI_BASE_URL = "https://api.solapi.com";

    @Value("${coolsms.api-key}")
    private String apiKey;

    @Value("${coolsms.api-secret}")
    private String apiSecret;

    @Value("${coolsms.sender-number}")
    private String senderNumber;

    private DefaultMessageService messageService;

    @PostConstruct
    public void init() {
        this.messageService = NurigoApp.INSTANCE.initialize(apiKey, apiSecret, SOLAPI_BASE_URL);
        log.info("CoolSmsSender 초기화 완료 (발신번호: {})", senderNumber);
    }

    @Override
    public SmsResult send(String to, String text) {
        try {
            Message message = new Message();
            message.setFrom(senderNumber);
            message.setTo(to);
            message.setText(text);

            SingleMessageSentResponse response = messageService.sendOne(new SingleMessageSendingRequest(message));

            if (response == null) {
                return SmsResult.failure("CoolSMS 응답이 비어있습니다.");
            }
            log.info("문자 발송 성공 (to: {}, messageId: {})", to, response.getMessageId());
            return SmsResult.success(response.getMessageId());

        } catch (Exception e) {
            log.warn("문자 발송 실패 (to: {}): {}", to, e.getMessage());
            return SmsResult.failure(e.getMessage());
        }
    }
}
