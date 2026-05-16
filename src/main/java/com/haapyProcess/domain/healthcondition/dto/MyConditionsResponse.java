package com.haapyProcess.domain.healthcondition.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

public record MyConditionsResponse(
        @Schema(description = "내 건강 상태 목록",
                requiredMode = Schema.RequiredMode.REQUIRED, nullable = false)
        List<ConditionResponse> myConditions
) {
}
