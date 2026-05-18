package com.haapyProcess.domain.location.dto;

import com.haapyProcess.domain.location.entity.Location;
import com.haapyProcess.domain.location.entity.LocationType;
import io.swagger.v3.oas.annotations.media.Schema;

public record LocationResponse(
        @Schema(description = "위치 ID", example = "1") Long locationId,
        @Schema(description = "위치 유형", example = "HOME") LocationType locationType,
        @Schema(description = "행정구역코드", example = "1168010300") String areaNo,
        @Schema(description = "시도", example = "서울특별시") String sido,
        @Schema(description = "시군구", example = "강남구") String sigungu,
        @Schema(description = "동", example = "역삼동") String dong
) {
    public static LocationResponse from(Location location) {
        return new LocationResponse(
                location.getLocationId(),
                location.getLocationType(),
                location.getRegion().getAreaNo(),
                location.getRegion().getSido(),
                location.getRegion().getSigungu(),
                location.getRegion().getDong()
        );
    }
}