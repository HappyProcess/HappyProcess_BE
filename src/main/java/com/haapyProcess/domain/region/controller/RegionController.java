package com.haapyProcess.domain.region.controller;

import com.haapyProcess.domain.region.dto.RegionResponse;
import com.haapyProcess.domain.region.service.RegionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Region", description = "지역(동네) 선택 API")
@RestController
@RequestMapping("/api/v1/regions")
@RequiredArgsConstructor
public class RegionController {

    private final RegionService regionService;

    @Operation(summary = "1단계: 시도 목록 조회", description = "대한민국의 전체 시/도 목록을 반환합니다.")
    @GetMapping("/sido")
    public ResponseEntity<List<String>> getSidoList() {
        return ResponseEntity.ok(regionService.getSidoList());
    }

    @Operation(summary = "2단계: 시군구 목록 조회", description = "선택한 시/도에 속한 시/군/구 목록을 반환합니다.")
    @GetMapping("/sigungu")
    public ResponseEntity<List<String>> getSigunguList(@RequestParam String sido) {
        return ResponseEntity.ok(regionService.getSigunguList(sido));
    }

    @Operation(summary = "3단계: 동 목록 조회 (최종)", description = "선택한 시/도와 시/군/구에 속한 동 목록과 고유 코드를 반환합니다.")
    @GetMapping("/dong")
    public ResponseEntity<List<RegionResponse>> getDongList(
            @RequestParam String sido,
            @RequestParam String sigungu) {
        return ResponseEntity.ok(regionService.getDongList(sido, sigungu));
    }
}