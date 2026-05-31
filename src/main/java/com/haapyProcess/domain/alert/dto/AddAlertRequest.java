package com.haapyProcess.domain.alert.dto;

import com.haapyProcess.domain.location.entity.LocationType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record AddAlertRequest(
        @Schema(description = "추가할 알림 시간 (HH:mm 형식)", example = "08:00")
        @NotBlank(message = "알림 시간을 입력해주세요.")
        @Pattern(regexp = "^([01]\\d|2[0-3]):([0-5]\\d)$", message = "알림 시간은 HH:mm 형식이어야 합니다.")
        String alertTime,

        @Schema(description = "알림 기준 위치 (HOME: 집, WORK: 직장)", example = "HOME")
        @NotNull(message = "위치 타입을 선택해주세요.")
        LocationType locationType) {}
