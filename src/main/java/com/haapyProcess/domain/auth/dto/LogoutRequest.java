package com.haapyProcess.domain.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class LogoutRequest {

    @Schema(description = "리프레시 토큰", example = "eyJhbGciOiJIUzI1NiJ9...",
            requiredMode = Schema.RequiredMode.REQUIRED, nullable = false)
    @NotBlank
    private String refreshToken;
}
