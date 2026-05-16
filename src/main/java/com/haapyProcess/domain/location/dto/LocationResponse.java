package com.haapyProcess.domain.location.dto;

import com.haapyProcess.domain.location.entity.Location;
import com.haapyProcess.domain.location.entity.LocationType;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;

public record LocationResponse(
    @Schema(description = "위치 ID") Long locationId,
    @Schema(description = "위치 유형") LocationType locationType,
    @Schema(description = "도시명") String city,
    @Schema(description = "위도") BigDecimal lat,
    @Schema(description = "경도") BigDecimal lon
) {
    public static LocationResponse from(Location location) {
        return new LocationResponse(
            location.getLocationId(),
            location.getLocationType(),
            location.getCity(),
            location.getLat(),
            location.getLon()
        );
    }
}
