package com.haapyProcess.global.sms;

/**
 * 문자 발송 추상화. 구현체(CoolSMS, Mock 등)는 이 인터페이스만 따른다.
 */
public interface SmsSender {

    /**
     * 단건 문자 발송.
     *
     * @param to   수신 번호 (하이픈 없이)
     * @param text 메시지 본문
     * @return 발송 결과 (예외를 던지지 않고 결과로 성공/실패를 표현한다)
     */
    SmsResult send(String to, String text);
}
