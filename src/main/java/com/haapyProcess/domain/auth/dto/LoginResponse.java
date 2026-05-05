package com.haapyProcess.domain.auth.dto;

public record LoginResponse(String accessToken, String refreshToken) {
}
