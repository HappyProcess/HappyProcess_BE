package com.haapyProcess.domain.member.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record SignUpResponse(
        @Schema(description = "생성된 회원 ID") Long memberId
) {
}
