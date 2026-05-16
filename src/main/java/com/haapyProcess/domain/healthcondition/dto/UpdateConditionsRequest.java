package com.haapyProcess.domain.healthcondition.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

import java.util.List;

@Getter
public class UpdateConditionsRequest {

    @Schema(description = "건강 상태 ID 목록", example = "[1, 2, 3]",
            requiredMode = Schema.RequiredMode.REQUIRED, nullable = false)
    @NotNull
    private List<Long> conditionIds;
}
