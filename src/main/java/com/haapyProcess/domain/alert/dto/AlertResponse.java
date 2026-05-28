package com.haapyProcess.domain.alert.dto;

import com.haapyProcess.domain.alert.entity.Alert;

public record AlertResponse(Long alertId, String alertTime, boolean isEnable) {
    public static AlertResponse from(Alert alert) {
        return new AlertResponse(alert.getAlertId(), alert.getAlertTime(), alert.isEnable());
    }
}
