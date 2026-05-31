package com.haapyProcess.domain.family.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import java.util.List;

@Getter
@Builder
public class FamilyListResponse {

    @Schema(description = "가족 관계 ID (상세 조회/수정용)", example = "1")
    private Long familyId;

    @Schema(description = "가족 이름", example = "아빠")
    private String name;

    @Schema(description = "가족이 가진 질병 이름 목록", example = "[\"관절염\", \"고혈압\"]")
    private List<String> healthConditionNames;

    @Schema(description = "가족에게 설정된 알림 시간 목록 (HH:mm)", example = "[\"08:00\", \"18:30\"]")
    private List<String> alertTimes;
}
