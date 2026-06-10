package com.haapyProcess.global.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    DUPLICATE_LOGIN_ID(HttpStatus.CONFLICT, "이미 사용 중인 아이디입니다."),
    MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 회원입니다."),
    INVALID_PASSWORD(HttpStatus.UNAUTHORIZED, "비밀번호가 일치하지 않습니다."),
    INVALID_CITY(HttpStatus.BAD_REQUEST, "지원하지 않는 지역 코드입니다."),
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "유효하지 않은 토큰입니다."),
    EXPIRED_TOKEN(HttpStatus.UNAUTHORIZED, "만료된 토큰입니다."),
    CONDITION_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 질환 ID가 포함되어 있습니다."),
    INVALID_LOCATION(HttpStatus.BAD_REQUEST, "HOME과 WORK 위치를 모두 입력해주세요."),
    LOCATION_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 위치 정보를 찾을 수 없습니다."),
    EXTERNAL_API_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "외부 API 호출 중 오류가 발생했습니다."),
    UNAUTHORIZED_USER(HttpStatus.UNAUTHORIZED, "로그인이 필요한 서비스입니다."),
    ALERT_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 알림입니다."),
    DUPLICATE_ALERT_TIME(HttpStatus.CONFLICT, "이미 동일한 시간의 알림이 등록되어 있습니다."),
    DIARY_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 날짜의 증상 일기를 찾을 수 없습니다."),
    INVALID_SYMPTOM_INTENSITY(HttpStatus.BAD_REQUEST, "증상 강도는 1~5 사이여야 합니다."),
    REPORT_GENERATION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "주간 리포트 생성에 실패했습니다."),
    REPORT_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 주차의 리포트를 찾을 수 없습니다."),
    NO_DIARY_FOR_REPORT(HttpStatus.BAD_REQUEST, "해당 주에 작성된 증상 일기가 없어 리포트를 생성할 수 없습니다.");

    private final HttpStatus status;
    private final String message;
}
