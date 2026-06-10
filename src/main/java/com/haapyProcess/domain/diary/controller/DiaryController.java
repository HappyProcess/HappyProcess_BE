package com.haapyProcess.domain.diary.controller;

import com.haapyProcess.domain.diary.dto.DiaryRequest;
import com.haapyProcess.domain.diary.dto.DiaryResponse;
import com.haapyProcess.domain.diary.service.SymptomDiaryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@Tag(name = "Symptom Diary", description = "증상 일기 API (보유 질환별 강도 + 자유 텍스트 + 날씨 스냅샷)")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/diaries")
public class DiaryController {

    private final SymptomDiaryService diaryService;

    @Operation(
            summary = "증상 일기 작성/수정",
            description = """
                    entryDate 기준으로 하루 1건을 작성하거나 수정합니다(upsert).
                    작성 시점에 사용자의 HOME 위치 기준 날씨를 스냅샷으로 함께 저장합니다.

                    ## **📋 Request Fields**

                    | **키** | **설명** | **타입** | **필수** |
                    |---|---|---|:---:|
                    | **entryDate** | 기록 날짜 (YYYY-MM-DD) | LocalDate | ✅ |
                    | **memo** | 자유 텍스트 메모 | String | |
                    | **symptoms** | 질환별 증상 강도 목록 | List | |
                    | **symptoms[].conditionId** | 질환 ID | Long | ✅ |
                    | **symptoms[].intensity** | 증상 강도 (1~5) | int | ✅ |
                    """
    )
    @ApiResponse(responseCode = "200", description = "작성/수정 성공")
    @ApiResponse(responseCode = "401", description = "인증 실패")
    @ApiResponse(responseCode = "404", description = "존재하지 않는 질환 ID")
    @PostMapping
    public ResponseEntity<DiaryResponse> upsertDiary(@RequestBody @Valid DiaryRequest request) {
        return ResponseEntity.ok(diaryService.upsertDiary(request));
    }

    @Operation(summary = "기간별 증상 일기 목록 조회")
    @ApiResponse(responseCode = "200", description = "조회 성공")
    @GetMapping
    public ResponseEntity<List<DiaryResponse>> getDiaries(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ResponseEntity.ok(diaryService.getDiaries(from, to));
    }

    @Operation(summary = "특정 날짜 증상 일기 조회")
    @ApiResponse(responseCode = "200", description = "조회 성공")
    @ApiResponse(responseCode = "404", description = "해당 날짜의 일기 없음")
    @GetMapping("/{date}")
    public ResponseEntity<DiaryResponse> getDiary(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(diaryService.getDiary(date));
    }

    @Operation(summary = "특정 날짜 증상 일기 삭제")
    @ApiResponse(responseCode = "204", description = "삭제 성공")
    @ApiResponse(responseCode = "404", description = "해당 날짜의 일기 없음")
    @DeleteMapping("/{date}")
    public ResponseEntity<Void> deleteDiary(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        diaryService.deleteDiary(date);
        return ResponseEntity.noContent().build();
    }
}
