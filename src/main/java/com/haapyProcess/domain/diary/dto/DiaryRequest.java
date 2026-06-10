package com.haapyProcess.domain.diary.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Getter
@NoArgsConstructor
public class DiaryRequest {

    @NotNull(message = "기록 날짜(entryDate)는 필수입니다.")
    private LocalDate entryDate;

    // 자유 텍스트 메모 (선택)
    private String memo;

    @Valid
    private List<SymptomItem> symptoms = new ArrayList<>();

    @Getter
    @NoArgsConstructor
    public static class SymptomItem {

        @NotNull(message = "conditionId는 필수입니다.")
        private Long conditionId;

        @Min(value = 1, message = "증상 강도는 1~5 사이여야 합니다.")
        @Max(value = 5, message = "증상 강도는 1~5 사이여야 합니다.")
        private int intensity;
    }
}
