package com.haapyProcess.domain.alert.controller;

import com.haapyProcess.domain.alert.dto.*;
import com.haapyProcess.domain.alert.service.AlertService;
import com.haapyProcess.domain.member.entity.Member;
import com.haapyProcess.domain.member.service.MemberService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Alert", description = "알림 설정 및 발송 내역 API")
@RestController
@RequestMapping("/api/v1/alerts")
@RequiredArgsConstructor
public class AlertController {

    private final AlertService alertService;
    private final MemberService memberService;

    // 알림 시간 추가
    @Operation(
            summary = "알림 시간 추가",
            description = """
                새로운 알림 수신 시간을 추가합니다. 추가된 알림은 기본적으로 켜짐(활성화) 상태가 됩니다.
                
                ## **📋 Request Fields**
                | **키** | **설명** | **타입** | **필수** |
                |---|---|---|:---:|
                | **alertTime** | 추가할 알림 시간 (반드시 HH:mm 형식) | String | ✅ |
                """
    )
    @ApiResponse(responseCode = "200", description = "알림 추가 성공")
    @ApiResponse(responseCode = "400", description = "잘못된 시간 형식 (예: 25:99)")
    @PostMapping
    public ResponseEntity<AlertResponse> addAlert(@RequestBody @Valid AddAlertRequest request) {
        Member currentMember = memberService.getCurrentMember();
        return ResponseEntity.ok(alertService.addAlert(currentMember, request.alertTime()));
    }

    // 내 알림 설정 목록 조회
    @Operation(
            summary = "내 알림 설정 목록 조회",
            description = "현재 로그인한 유저가 설정한 모든 알림 시간 목록을 시간순(오름차순)으로 조회합니다."
    )
    @ApiResponse(responseCode = "200", description = "조회 성공")
    @GetMapping
    public ResponseEntity<List<AlertResponse>> getMyAlerts() {
        Member currentMember = memberService.getCurrentMember();
        return ResponseEntity.ok(alertService.getMyAlerts(currentMember));
    }

    // 알림 시간 수정
    @Operation(
            summary = "알림 시간 수정",
            description = """
                특정 알림의 시간을 변경합니다.

                ## **📋 Request Fields**
                | **키** | **설명** | **타입** | **필수** |
                |---|---|---|:---:|
                | **alertTime** | 변경할 알림 시간 (반드시 HH:mm 형식) | String | ✅ |
                """
    )
    @ApiResponse(responseCode = "200", description = "수정 성공")
    @ApiResponse(responseCode = "400", description = "잘못된 시간 형식 (예: 25:99)")
    @ApiResponse(responseCode = "403", description = "본인의 알림이 아님 (권한 없음)")
    @ApiResponse(responseCode = "404", description = "존재하지 않는 알림 ID")
    @ApiResponse(responseCode = "409", description = "이미 동일한 시간의 알림이 존재함")
    @PatchMapping("/{alertId}")
    public ResponseEntity<AlertResponse> updateAlert(
            @Parameter(description = "수정할 알림 ID", example = "1") @PathVariable Long alertId,
            @RequestBody @Valid UpdateAlertRequest request) {
        Member currentMember = memberService.getCurrentMember();
        return ResponseEntity.ok(alertService.updateAlert(currentMember, alertId, request.alertTime()));
    }

    // 알림 켜기/끄기 토글
    @Operation(
            summary = "특정 알림 켜기/끄기",
            description = """
                특정 알림의 활성화/비활성화 상태를 변경합니다.
                
                ## **📋 Request Parameters**
                | **이름** | **설명** | **타입** | **필수** |
                |---|---|---|:---:|
                | **isEnable** | 변경할 상태 (true: 켜기, false: 끄기) | boolean | ✅ |
                """
    )
    @ApiResponse(responseCode = "200", description = "상태 변경 성공")
    @ApiResponse(responseCode = "403", description = "본인의 알림이 아님 (권한 없음)")
    @ApiResponse(responseCode = "404", description = "존재하지 않는 알림 ID")
    @PatchMapping("/{alertId}/toggle")
    public ResponseEntity<Void> toggleAlert(
            @Parameter(description = "알림 ID", example = "1") @PathVariable Long alertId,
            @RequestParam boolean isEnable) {
        Member currentMember = memberService.getCurrentMember();
        alertService.toggleAlert(currentMember, alertId, isEnable);
        return ResponseEntity.ok().build();
    }

    // 내 알림 기록 조회
    @Operation(
            summary = "내 알림 발송 기록 조회",
            description = "스케줄러에 의해 실제로 발송된 알림 내역을 최신순(내림차순)으로 조회합니다. 화면의 알림 목록창에 사용됩니다."
    )
    @ApiResponse(responseCode = "200", description = "조회 성공")
    @GetMapping("/history")
    public ResponseEntity<List<NotificationHistoryResponse>> getMyHistories() {
        Member currentMember = memberService.getCurrentMember();
        return ResponseEntity.ok(alertService.getMyHistories(currentMember));
    }

    // 특정 알림 기록 읽음 처리
    @Operation(
            summary = "특정 알림 기록 읽음 처리",
            description = "유저가 알림 내역을 클릭하여 확인했을 때, 해당 알림을 '읽음(isRead = true)' 상태로 변경합니다."
    )
    @ApiResponse(responseCode = "200", description = "읽음 처리 성공")
    @ApiResponse(responseCode = "403", description = "본인의 기록이 아님 (권한 없음)")
    @PatchMapping("/history/{historyId}/read")
    public ResponseEntity<Void> readHistory(
            @Parameter(description = "알림 발송 기록 ID", example = "1") @PathVariable Long historyId) {
        Member currentMember = memberService.getCurrentMember();
        alertService.readHistory(currentMember, historyId);
        return ResponseEntity.ok().build();
    }


    // 특정 알림 삭제
    @Operation(
            summary = "특정 알림 설정 삭제",
            description = "유저가 등록해둔 특정 알림 시간을 완전히 삭제합니다."
    )
    @ApiResponse(responseCode = "204", description = "삭제 성공 (No Content)")
    @ApiResponse(responseCode = "403", description = "본인의 알림이 아님 (권한 없음)")
    @DeleteMapping("/{alertId}")
    public ResponseEntity<Void> deleteAlert(
            @Parameter(description = "삭제할 알림 ID", example = "1") @PathVariable Long alertId) {
        Member currentMember = memberService.getCurrentMember();
        alertService.deleteAlert(currentMember, alertId);
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "특정 알림 발송 기록 삭제",
            description = "유저가 받은 특정 알림 내역을 개별적으로 완전히 삭제합니다."
    )
    @ApiResponse(responseCode = "204", description = "삭제 성공 (No Content)")
    @ApiResponse(responseCode = "403", description = "본인의 기록이 아님 (권한 없음)")
    @DeleteMapping("/history/{historyId}")
    public ResponseEntity<Void> deleteHistory(
            @Parameter(description = "삭제할 알림 발송 기록 ID", example = "1") @PathVariable Long historyId) {
        Member currentMember = memberService.getCurrentMember();
        alertService.deleteHistory(currentMember, historyId);
        return ResponseEntity.noContent().build();
    }
}