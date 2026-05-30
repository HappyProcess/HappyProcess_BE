package com.haapyProcess.domain.family.controller;

import com.haapyProcess.domain.family.dto.AddFamilyRequest;
import com.haapyProcess.domain.family.dto.FamilyMemberResponse;
import com.haapyProcess.domain.family.service.FamilyService;
import com.haapyProcess.domain.healthcondition.dto.UpdateConditionsRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Family", description = "가족 그룹 관리 API")
@RestController
@RequestMapping("/api/v1/families")
@RequiredArgsConstructor
public class FamilyController {

    private final FamilyService familyService;

    @Operation(summary = "가족 구성원 추가", description = "다른 유저의 로그인 아이디를 입력하여 내 가족으로 등록합니다.")
    @ApiResponse(responseCode = "201", description = "가족 추가 성공")
    @PostMapping
    public ResponseEntity<Long> addFamily(@RequestBody @Valid AddFamilyRequest request) {
        Long familyId = familyService.addFamily(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(familyId);
    }

    @Operation(
        summary = "가족 목록 및 위험도 조회", 
        description = "내 가족 목록과 함께, 각 가족이 거주하는 지역의 실시간 날씨를 분석한 위험도(isRisk)를 반환합니다."
    )
    @ApiResponse(responseCode = "200", description = "조회 성공")
    @GetMapping
    public ResponseEntity<List<FamilyMemberResponse>> getMyFamilies() {
        return ResponseEntity.ok(familyService.getMyFamilies());
    }

    @Operation(
            summary = "가족 프로필(건강 상태) 수정", 
            description = "특정 가족 구성원이 앓고 있는 질환 목록을 덮어씌웁니다. 내 프로필 수정과 동일한 DTO를 사용합니다."
    )
    @ApiResponse(responseCode = "200", description = "수정 성공")
    @ApiResponse(responseCode = "404", description = "권한이 없거나 존재하지 않는 질환 ID")
    @PutMapping("/{familyId}/conditions")
    public ResponseEntity<Void> updateFamilyConditions(
            @PathVariable Long familyId, 
            @RequestBody @Valid UpdateConditionsRequest request) {
        
        familyService.updateFamilyConditions(familyId, request);
        return ResponseEntity.ok().build();
    }

    @Operation(
            summary = "특정 가족 알림 켜기/끄기 (Toggle)",
            description = """
                특정 가족 구성원의 위험도 알림 수신 여부를 변경합니다.
                
                ## **📋 Request Parameters**
                | **이름** | **설명** | **타입** | **필수** |
                |---|---|---|:---:|
                | **isAlertEnabled** | 변경할 상태 (true: 켜기, false: 끄기) | boolean | ✅ |
                """
    )
    @ApiResponse(responseCode = "200", description = "상태 변경 성공")
    @ApiResponse(responseCode = "404", description = "권한이 없거나 존재하지 않는 가족 ID")
    @PatchMapping("/{familyId}/alert/toggle")
    public ResponseEntity<Void> toggleFamilyAlert(
            @PathVariable Long familyId,
            @RequestParam boolean isAlertEnabled) {
        
        familyService.toggleFamilyAlert(familyId, isAlertEnabled);
        return ResponseEntity.ok().build();
    }
}