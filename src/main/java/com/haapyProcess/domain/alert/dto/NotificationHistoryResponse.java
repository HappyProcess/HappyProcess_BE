package com.haapyProcess.domain.alert.dto;

import com.haapyProcess.domain.alert.entity.NotificationHistory;
import com.haapyProcess.domain.location.entity.LocationType;

public record NotificationHistoryResponse(
        Long historyId,
        String diseaseNames,
        String factorNames,
        boolean isRead,
        String createdAt,
        LocationType locationType
) {
    public static NotificationHistoryResponse from(NotificationHistory history) {
        String formattedDate = history.getCreatedAt().format(java.time.format.DateTimeFormatter.ofPattern("MM월 dd일 HH:mm"));
        return new NotificationHistoryResponse(
                history.getHistoryId(),
                history.getDiseaseNames(),
                history.getFactorNames(),
                history.isRead(),
                formattedDate,
                history.getEffectiveLocationType()
        );
    }
}
