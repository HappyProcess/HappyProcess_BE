package com.haapyProcess.domain.report.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.haapyProcess.domain.report.entity.WeeklyReport;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 주간 리포트 응답. content(JSON)를 구조화 필드로 파싱해서 내려준다.
 * - summary: 한 주 요약
 * - patterns: 어떤 날씨에 어떤 증상이 나타났는지(날씨→증상→관찰)
 * - solutions: 증상별 솔루션(대상→조치)
 */
@Slf4j
@Getter
@Builder
public class WeeklyReportResponse {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private Long reportId;
    private LocalDate weekStartDate;
    private LocalDate weekEndDate;
    private String summary;
    private List<Pattern> patterns;
    private List<Solution> solutions;
    private LocalDateTime createdAt;

    @Getter
    @Setter
    @NoArgsConstructor
    public static class Pattern {
        private String weather;     // 예: "미세먼지 나쁨 (PM10 80↑)"
        private String symptom;     // 예: "천식"
        private String observation; // 예: "강도 4~5로 악화"
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class Solution {
        private String target; // 대상 증상/상황, 예: "천식"
        private String action; // 구체적 조치, 예: "미세먼지 나쁜 날 외출 자제, KF94 착용"
    }

    public static WeeklyReportResponse from(WeeklyReport report) {
        ReportContent content = parse(report.getContent());
        return WeeklyReportResponse.builder()
                .reportId(report.getId())
                .weekStartDate(report.getWeekStartDate())
                .weekEndDate(report.getWeekEndDate())
                .summary(content.summary)
                .patterns(content.patterns)
                .solutions(content.solutions)
                .createdAt(report.getCreatedAt())
                .build();
    }

    private static ReportContent parse(String json) {
        ReportContent content = new ReportContent();
        if (json == null || json.isBlank()) {
            content.summary = "";
            content.patterns = List.of();
            content.solutions = List.of();
            return content;
        }
        try {
            ReportContent parsed = OBJECT_MAPPER.readValue(json, ReportContent.class);
            content.summary = parsed.summary != null ? parsed.summary : "";
            content.patterns = parsed.patterns != null ? parsed.patterns : List.of();
            content.solutions = parsed.solutions != null ? parsed.solutions : List.of();
        } catch (Exception e) {
            // 구버전(평문) 또는 파싱 실패 시 원문을 summary로 폴백
            log.warn("리포트 content JSON 파싱 실패, 원문을 summary로 반환합니다: {}", e.getMessage());
            content.summary = json;
            content.patterns = List.of();
            content.solutions = List.of();
        }
        return content;
    }

    // content JSON 역직렬화용 내부 DTO
    private static class ReportContent {
        public String summary;
        public List<Pattern> patterns;
        public List<Solution> solutions;
    }
}
