package com.haapyProcess.domain.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record SignUpResponse(
        @Schema(description = "생성된 회원 ID", example = "1",
                requiredMode = Schema.RequiredMode.REQUIRED, nullable = false) Long memberId
) {
}
