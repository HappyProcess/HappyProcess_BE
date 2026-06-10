package com.haapyProcess.domain.report.controller;

import com.haapyProcess.domain.report.dto.WeeklyReportResponse;
import com.haapyProcess.domain.report.service.WeeklyReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@Tag(name = "Weekly Report", description = "Gemini 주간 건강 리포트 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/reports")
public class ReportController {

    private final WeeklyReportService reportService;

    @Operation(
            summary = "주간 리포트 생성(온디맨드)",
            description = """
                    지난 주(또는 weekStart가 속한 주)의 증상 일기·날씨를 바탕으로 Gemini가 리포트를 생성합니다.
                    이미 생성된 주차면 캐시된 리포트를 반환합니다. weekStart 생략 시 지난 주가 대상입니다.
                    """
    )
    @ApiResponse(responseCode = "200", description = "생성/캐시 반환 성공")
    @ApiResponse(responseCode = "400", description = "해당 주에 작성된 일기가 없음")
    @ApiResponse(responseCode = "401", description = "인증 실패")
    @PostMapping("/weekly/generate")
    public ResponseEntity<WeeklyReportResponse> generate(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate weekStart,
            @RequestParam(defaultValue = "false") boolean force) {
        return ResponseEntity.ok(reportService.generateWeeklyReport(weekStart, force));
    }

    @Operation(
            summary = "주간 리포트 조회",
            description = "이미 생성된 주차 리포트를 조회합니다. weekStart 생략 시 지난 주가 대상입니다."
    )
    @ApiResponse(responseCode = "200", description = "조회 성공")
    @ApiResponse(responseCode = "404", description = "해당 주차 리포트 없음")
    @GetMapping("/weekly")
    public ResponseEntity<WeeklyReportResponse> getWeekly(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate weekStart) {
        return ResponseEntity.ok(reportService.getWeeklyReport(weekStart));
    }
}
