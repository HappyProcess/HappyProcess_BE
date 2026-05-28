package com.haapyProcess.domain.alert.service;

import com.haapyProcess.domain.alert.entity.Alert;
import com.haapyProcess.domain.alert.entity.NotificationHistory;
import com.haapyProcess.domain.alert.repository.AlertRepository;
import com.haapyProcess.domain.alert.repository.NotificationHistoryRepository;
import com.haapyProcess.domain.analysis.dto.RiskAnalysisResult;
import com.haapyProcess.domain.analysis.service.RiskAnalysisService;
import com.haapyProcess.domain.member.entity.Member;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.ZoneId;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationScheduler {

    private final AlertRepository alertRepository;
    private final NotificationHistoryRepository historyRepository;
    private final RiskAnalysisService riskAnalysisService;

    /**
     * 매 1분마다(초가 00일 때) 실행되는 스케줄러
     * cron = "초 분 시 일 월 요일"
     */
    @Scheduled(cron = "0 * * * * *")
    @Transactional
    public void processScheduledAlerts() {
        String currentTime = LocalTime.now(ZoneId.of("Asia/Seoul")).format(DateTimeFormatter.ofPattern("HH:mm"));

        List<Alert> activeAlerts = alertRepository.findAllByAlertTimeAndIsEnableTrue(currentTime);

        if (activeAlerts.isEmpty()) {
            return;
        }

        log.info("[스케줄러] {} 알림 발송 대상자 {}명 탐색 완료", currentTime, activeAlerts.size());

        for (Alert alert : activeAlerts) {
            Member targetMember = alert.getMember();

            try {
                RiskAnalysisResult result = riskAnalysisService.analyzeRiskForMember(targetMember);

                if (result.isRisk()) {
                    saveNotificationHistory(targetMember, result);
                    // TODO: 실제 푸시 알림(FCM, SMS 등)을 쏘는 로직이 있다면 이 부분에 추가
                }

            } catch (Exception e) {
                log.error("회원 ID {} 알림 발송 중 오류 발생: {}", targetMember.getMemberId(), e.getMessage());
            }
        }
    }

    /**
     * 알림 기록을 예쁘게 포장해서 DB에 저장하는 헬퍼 메서드
     */
    private void saveNotificationHistory(Member member, RiskAnalysisResult result) {
        String diseaseNamesStr = String.join(", ", result.getCauseDiseaseNames());
        String diseaseIdsStr = result.getCauseDiseaseIds().stream()
                .map(String::valueOf)
                .collect(Collectors.joining(","));

        String message = String.format("[%s] 현재 날씨가 위험 기준을 초과했습니다. 외출 시 주의하세요!", diseaseNamesStr);

        NotificationHistory history = NotificationHistory.builder()
                .member(member)
                .diseaseIds(diseaseIdsStr)
                .diseaseNames(diseaseNamesStr)
                .message(message)
                .isRead(false)
                .build();

        historyRepository.save(history);
        log.info("회원 ID {}에게 위험 알림 발송 완료: {}", member.getMemberId(), diseaseNamesStr);
    }
}