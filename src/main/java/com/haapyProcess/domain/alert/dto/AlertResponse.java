package com.haapyProcess.domain.alert.dto;

import com.haapyProcess.domain.alert.entity.Alert;
import com.haapyProcess.domain.location.entity.LocationType;

public record AlertResponse(Long alertId, String alertTime, boolean isEnable, LocationType locationType) {
    public static AlertResponse from(Alert alert) {
        return new AlertResponse(alert.getAlertId(), alert.getAlertTime(), alert.isEnable(), alert.getEffectiveLocationType());
    }
}
