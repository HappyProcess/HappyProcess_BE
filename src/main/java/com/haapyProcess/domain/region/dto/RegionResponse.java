package com.haapyProcess.domain.region.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class RegionResponse {

    @Schema(description = "행정구역코드 (PK)", example = "1168010300")
    private String areaNo;

    @Schema(description = "동/읍/면 이름", example = "역삼동")
    private String dong;
}