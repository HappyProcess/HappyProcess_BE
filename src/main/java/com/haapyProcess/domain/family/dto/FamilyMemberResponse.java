package com.haapyProcess.domain.family.dto;

import com.haapyProcess.domain.alert.dto.AlertResponse;
import com.haapyProcess.domain.location.dto.LocationResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import java.util.List;

@Getter
@Builder
public class FamilyMemberResponse {

    @Schema(description = "가족 관계 ID (수정/삭제용)", example = "1")
    private Long familyId;

    @Schema(description = "가족 본인의 유저 ID", example = "5")
    private Long relativeId;

    @Schema(description = "가족 이름", example = "아빠")
    private String name;

    @Schema(description = "가족 나이", example = "51")
    private int age;

    @Schema(description = "가족 알림 활성화 여부", example = "true")
    private boolean isAlertEnabled;

    // --- 건강 및 위험도 정보 ---
    @Schema(description = "가족이 가진 질병 이름 목록", example = "[\"관절염\", \"고혈압\"]")
    private List<String> healthConditionNames;

    @Schema(description = "현재 가족이 사는 동네 날씨 기준 위험 여부", example = "true")
    private boolean isRisk;

    @Schema(description = "위험 기준을 초과한 원인 질병", example = "[\"관절염\"]")
    private List<String> causeDiseaseNames;

    // --- 지역 및 알림 시간 정보 (조회/수정용) ---
    @Schema(description = "가족이 등록한 지역(집/직장) 목록")
    private List<LocationResponse> locations;

    @Schema(description = "가족에게 설정된 알림 시간 목록")
    private List<AlertResponse> alerts;
}
