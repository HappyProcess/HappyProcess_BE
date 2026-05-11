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
    INVALID_CITY(HttpStatus.BAD_REQUEST, "지원하지 않는 도시명입니다."),
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "유효하지 않은 토큰입니다."),
    EXPIRED_TOKEN(HttpStatus.UNAUTHORIZED, "만료된 토큰입니다."),
    CONDITION_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 질환 ID가 포함되어 있습니다."),
    INVALID_LOCATION(HttpStatus.BAD_REQUEST, "HOME과 WORK 위치를 모두 입력해주세요."),
    LOCATION_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 위치 정보를 찾을 수 없습니다.");

    private final HttpStatus status;
    private final String message;
}
