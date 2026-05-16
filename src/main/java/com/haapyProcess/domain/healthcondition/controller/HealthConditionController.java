package com.haapyProcess.domain.healthcondition.controller;

import com.haapyProcess.domain.healthcondition.dto.ConditionResponse;
import com.haapyProcess.domain.healthcondition.dto.MyConditionsResponse;
import com.haapyProcess.domain.healthcondition.dto.UpdateConditionsRequest;
import com.haapyProcess.domain.healthcondition.service.HealthConditionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Member Health", description = "건강 상태 관련 API")
@RestController
@RequiredArgsConstructor
public class HealthConditionController {

    private final HealthConditionService healthConditionService;

    @Operation(
        summary = "전체 건강 상태 목록 조회",
        description = """
            ## **📋 Response Fields**

            | **키** | **설명** | **타입** |
            |---|---|---|
            | **conditions** | 전체 건강 상태 목록 | List |
            | **conditions[].conditionId** | 건강 상태 ID | Long |
            | **conditions[].conditionName** | 건강 상태 이름 | String |
            """
    )
    @ApiResponse(responseCode = "200", description = "조회 성공")
    @GetMapping("/api/v1/conditions")
    public ResponseEntity<List<ConditionResponse>> getAllConditions() {
        return ResponseEntity.ok(healthConditionService.getAllConditions());
    }

    @Operation(
        summary = "내 건강 상태 조회",
        description = """
            ## **📋 Response Fields**

            | **키** | **설명** | **타입** |
            |---|---|---|
            | **myConditions** | 내 건강 상태 목록 | List |
            | **myConditions[].conditionId** | 건강 상태 ID | Long |
            | **myConditions[].conditionName** | 건강 상태 이름 | String |
            """
    )
    @ApiResponse(responseCode = "200", description = "조회 성공")
    @ApiResponse(responseCode = "401", description = "인증 실패")
    @GetMapping("/api/v1/members/me/conditions")
    public ResponseEntity<MyConditionsResponse> getMyConditions() {
        return ResponseEntity.ok(healthConditionService.getMyConditions());
    }

    @Operation(
        summary = "내 건강 상태 일괄 수정",
        description = """
            ## **📋 Request Fields**

            | **키** | **설명** | **타입** | **필수** |
            |---|---|---|:---:|
            | **conditionIds** | 건강 상태 ID 목록 | List&lt;Long&gt; | ✅ |

            ---

            **Response**: 없음
            """
    )
    @ApiResponse(responseCode = "200", description = "수정 성공")
    @ApiResponse(responseCode = "401", description = "인증 실패")
    @ApiResponse(responseCode = "404", description = "존재하지 않는 건강 상태 ID")
    @PutMapping("/api/v1/members/me/conditions")
    public ResponseEntity<Void> updateMyConditions(@RequestBody @Valid UpdateConditionsRequest request) {
        healthConditionService.updateMyConditions(request);
        return ResponseEntity.ok().build();
    }
}
