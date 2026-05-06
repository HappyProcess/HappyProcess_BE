package com.haapyProcess.domain.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class LoginRequest {

    @Schema(description = "로그인 아이디", example = "user1234")
    @NotBlank
    private String loginId;

    @Schema(description = "비밀번호", example = "Pass1234!")
    @NotBlank
    private String password;
}
