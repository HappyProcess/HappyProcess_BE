package com.haapyProcess.domain.alert.service;

import com.haapyProcess.domain.alert.dto.*;
import com.haapyProcess.domain.alert.entity.Alert;
import com.haapyProcess.domain.alert.entity.NotificationHistory;
import com.haapyProcess.domain.alert.repository.AlertRepository;
import com.haapyProcess.domain.alert.repository.NotificationHistoryRepository;
import com.haapyProcess.domain.member.entity.Member;
import com.haapyProcess.global.exception.CustomException;
import com.haapyProcess.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AlertService {

    private final AlertRepository alertRepository;
    private final NotificationHistoryRepository historyRepository;

    // 1. 알림 설정 추가
    @Transactional
    public AlertResponse addAlert(Member member, String alertTime) {
        if (alertRepository.existsByMemberAndAlertTime(member, alertTime)) {
            throw new CustomException(ErrorCode.DUPLICATE_ALERT_TIME);
        }
        Alert alert = Alert.builder()
                .member(member)
                .alertTime(alertTime)
                .isEnable(true)
                .build();
        return AlertResponse.from(alertRepository.save(alert));
    }

    // 1-2. 알림 시간 수정
    @Transactional
    public AlertResponse updateAlert(Member member, Long alertId, String alertTime) {
        Alert alert = alertRepository.findById(alertId)
                .orElseThrow(() -> new CustomException(ErrorCode.ALERT_NOT_FOUND));

        if (!alert.getMember().getMemberId().equals(member.getMemberId())) {
            throw new CustomException(ErrorCode.UNAUTHORIZED_USER);
        }

        if (!alert.getAlertTime().equals(alertTime)
                && alertRepository.existsByMemberAndAlertTime(member, alertTime)) {
            throw new CustomException(ErrorCode.DUPLICATE_ALERT_TIME);
        }

        alert.updateAlertTime(alertTime);
        return AlertResponse.from(alert);
    }

    // 2. 내 알림 설정 목록 조회
    @Transactional(readOnly = true)
    public List<AlertResponse> getMyAlerts(Member member) {
        return alertRepository.findAllByMemberOrderByAlertTimeAsc(member)
                .stream().map(AlertResponse::from).toList();
    }

    // 3. 특정 알림 켜기/끄기 (Toggle)
    @Transactional
    public void toggleAlert(Member member, Long alertId, boolean isEnable) {
        Alert alert = alertRepository.findById(alertId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 알림입니다."));

        if (!alert.getMember().getMemberId().equals(member.getMemberId())) {
            throw new CustomException(ErrorCode.UNAUTHORIZED_USER);
        }

        alert.toggleEnable(isEnable);
    }

    // 4. 알림 발송 내역 조회
    @Transactional(readOnly = true)
    public List<NotificationHistoryResponse> getMyHistories(Member member) {
        return historyRepository.findAllByMemberOrderByCreatedAtDesc(member)
                .stream().map(NotificationHistoryResponse::from).toList();
    }

    // 5. 특정 알림 내역 읽음 처리
    @Transactional
    public void readHistory(Member member, Long historyId) {
        NotificationHistory history = historyRepository.findById(historyId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 알림 기록입니다."));

        if (!history.getMember().getMemberId().equals(member.getMemberId())) {
            throw new CustomException(ErrorCode.UNAUTHORIZED_USER);
        }

        history.markAsRead();
    }

    // 6. 알림 설정 시간 삭제
    @Transactional
    public void deleteAlert(Member member, Long alertId) {
        Alert alert = alertRepository.findById(alertId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 알림입니다."));

        if (!alert.getMember().getMemberId().equals(member.getMemberId())) {
            throw new CustomException(ErrorCode.UNAUTHORIZED_USER);
        }
        alertRepository.delete(alert);
    }

    // 7. 알림 발송 내역(기록) 삭제
    @Transactional
    public void deleteHistory(Member member, Long historyId) {
        NotificationHistory history = historyRepository.findById(historyId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 알림 기록입니다."));

        if (!history.getMember().getMemberId().equals(member.getMemberId())) {
            throw new CustomException(ErrorCode.UNAUTHORIZED_USER);
        }

        historyRepository.delete(history);
    }
}