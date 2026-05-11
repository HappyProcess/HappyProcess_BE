package com.haapyProcess.domain.location.dto;

import com.haapyProcess.domain.location.entity.LocationType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class AddLocationRequest {
    @Schema(description = "위치 유형", example = "HOME")
    @NotNull
    private LocationType locationType;

    @Schema(description = "도시명", example = "서울특별시")
    @NotBlank
    private String city;
}
