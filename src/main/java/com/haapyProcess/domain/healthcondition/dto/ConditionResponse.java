package com.haapyProcess.domain.healthcondition.dto;

import com.haapyProcess.domain.condition.entity.Condition;
import io.swagger.v3.oas.annotations.media.Schema;

public record ConditionResponse(
        @Schema(description = "건강 상태 ID", example = "1",
                requiredMode = Schema.RequiredMode.REQUIRED, nullable = false) Long conditionId,
        @Schema(description = "건강 상태 이름", example = "천식",
                requiredMode = Schema.RequiredMode.REQUIRED, nullable = false) String conditionName
) {
    public static ConditionResponse from(Condition condition) {
        return new ConditionResponse(condition.getConditionId(), condition.getConditionName());
    }
}
