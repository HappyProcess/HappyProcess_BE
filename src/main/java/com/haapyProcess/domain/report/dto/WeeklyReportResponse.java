package com.haapyProcess.domain.report.dto;

import com.haapyProcess.domain.report.entity.WeeklyReport;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Builder
public class WeeklyReportResponse {

    private Long reportId;
    private LocalDate weekStartDate;
    private LocalDate weekEndDate;
    private String content;
    private LocalDateTime createdAt;

    public static WeeklyReportResponse from(WeeklyReport report) {
        return WeeklyReportResponse.builder()
                .reportId(report.getId())
                .weekStartDate(report.getWeekStartDate())
                .weekEndDate(report.getWeekEndDate())
                .content(report.getContent())
                .createdAt(report.getCreatedAt())
                .build();
    }
}
