package com.haapyProcess.domain.member.controller;

import com.haapyProcess.domain.member.dto.ProfileResponse;
import com.haapyProcess.domain.member.dto.UpdateProfileRequest;
import com.haapyProcess.domain.member.service.MemberService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Member Profile", description = "회원 프로필 관련 API")
@RestController
@RequestMapping("/api/v1/members")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    @Operation(summary = "내 기본 프로필 조회")
    @ApiResponse(responseCode = "200", description = "조회 성공")
    @GetMapping("/me")
    public ResponseEntity<ProfileResponse> getMyProfile() {
        return ResponseEntity.ok(memberService.getMyProfile());
    }

    @Operation(summary = "내 기본 프로필 수정")
    @ApiResponse(responseCode = "200", description = "수정 성공")
    @PatchMapping("/me")
    public ResponseEntity<Void> updateMyProfile(@RequestBody @Valid UpdateProfileRequest request) {
        memberService.updateMyProfile(request);
        return ResponseEntity.ok().build();
    }
}
