package com.haapyProcess.domain.family.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class AddFamilyRequest {

    @Schema(description = "추가할 가족의 로그인 아이디", example = "papa123")
    @NotBlank(message = "가족의 아이디를 입력해주세요.")
    private String relativeLoginId;
}