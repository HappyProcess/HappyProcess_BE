package com.haapyProcess.domain.member.dto;

import com.haapyProcess.domain.member.entity.Member;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;

public record ProfileResponse(
        @Schema(description = "로그인 아이디", example = "user1234") String loginId,
        @Schema(description = "이름", example = "홍길동") String name,
        @Schema(description = "생년월일", example = "2000-01-01") LocalDate birth,
        @Schema(description = "휴대폰 번호", example = "01012345678") String phoneNumber,
        @Schema(description = "문자 알림 수신 여부", example = "true") boolean smsEnabled
) {
    public static ProfileResponse from(Member member) {
        return new ProfileResponse(
                member.getLoginId(),
                member.getName(),
                member.getBirth(),
                member.getPhoneNumber(),
                member.isSmsEnabled()
        );
    }
}
