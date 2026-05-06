package com.haapyProcess.domain.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record ReissueResponse(
        @Schema(description = "새 액세스 토큰") String accessToken,
        @Schema(description = "새 리프레시 토큰") String refreshToken
) {
}
