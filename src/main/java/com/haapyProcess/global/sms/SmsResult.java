package com.haapyProcess.global.sms;

/**
 * SMS 발송 결과. 성공 여부와 공급자 메시지 ID, 실패 시 사유를 담는다.
 */
public record SmsResult(boolean success, String providerMessageId, String errorMessage) {

    public static SmsResult success(String providerMessageId) {
        return new SmsResult(true, providerMessageId, null);
    }

    public static SmsResult failure(String errorMessage) {
        return new SmsResult(false, null, errorMessage);
    }
}
