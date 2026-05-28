package com.haapyProcess.domain.alert.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record AddAlertRequest(
        @Schema(description = "추가할 알림 시간 (HH:mm 형식)", example = "08:00")
        @NotBlank(message = "알림 시간을 입력해주세요.")
        @Pattern(regexp = "^([01]\\d|2[0-3]):([0-5]\\d)$", message = "알림 시간은 HH:mm 형식이어야 합니다.")
        String alertTime) {}
