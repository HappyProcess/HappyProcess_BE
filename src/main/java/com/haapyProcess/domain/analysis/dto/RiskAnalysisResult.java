package com.haapyProcess.domain.analysis.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL) // null이 아닌 필드만 JSON에 포함
public class RiskAnalysisResult {
    private boolean isRisk;           // 위험 여부 (true/false)
    private List<String> causeDiseaseNames;
    private List<Long> causeDiseaseIds;}
