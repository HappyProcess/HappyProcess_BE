package com.haapyProcess.domain.alert.dto;

import com.haapyProcess.domain.alert.entity.NotificationHistory;

public record NotificationHistoryResponse(
        Long historyId,
        String diseaseNames,
        String message,
        boolean isRead,
        String createdAt
) {
    public static NotificationHistoryResponse from(NotificationHistory history) {
        String formattedDate = history.getCreatedAt().format(java.time.format.DateTimeFormatter.ofPattern("MM월 dd일 HH:mm"));
        return new NotificationHistoryResponse(
                history.getHistoryId(), history.getDiseaseNames(), history.getMessage(), history.isRead(), formattedDate
        );
    }
}
