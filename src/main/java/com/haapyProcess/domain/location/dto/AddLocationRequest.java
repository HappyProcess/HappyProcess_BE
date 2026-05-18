package com.haapyProcess.domain.location.dto;

import com.haapyProcess.domain.location.entity.LocationType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AddLocationRequest(

        @Schema(description = "위치 유형 (HOME, WORK 등)", example = "HOME")
        @NotNull(message = "위치 유형은 필수입니다.")
        LocationType locationType,

        @Schema(description = "행정구역코드", example = "1168010300")
        @NotBlank(message = "행정구역코드는 필수입니다.")
        String areaNo
) {}