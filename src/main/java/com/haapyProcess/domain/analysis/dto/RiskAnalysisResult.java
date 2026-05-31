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
    private List<RiskDetail> riskDetails; // 질병별 상세 원인과 가이드 묶음

    @Getter
    @AllArgsConstructor
    public static class RiskDetail {
        private Long diseaseId;
        private String diseaseName;
        private List<FactorGuide> factorGuides;
    }

    @Getter
    @AllArgsConstructor
    public static class FactorGuide {
        private String factorName; // 예: "미세먼지", "꽃가루 (높음)"
        private String guide;      // 예: "마스크를 착용하세요."
    }

}
