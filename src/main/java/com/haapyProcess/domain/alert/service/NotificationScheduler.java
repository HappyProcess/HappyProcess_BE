package com.haapyProcess.domain.alert.service;

import com.haapyProcess.domain.alert.entity.Alert;
import com.haapyProcess.domain.alert.entity.NotificationHistory;
import com.haapyProcess.domain.alert.repository.AlertRepository;
import com.haapyProcess.domain.alert.repository.NotificationHistoryRepository;
import com.haapyProcess.domain.analysis.dto.RiskAnalysisResult;
import com.haapyProcess.domain.analysis.service.RiskAnalysisService;
import com.haapyProcess.domain.location.entity.LocationType;
import com.haapyProcess.domain.member.entity.Member;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
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
     * 매 분 50초에 실행되는 스케줄러 (알람 시각 10초 전 선처리)
     * cron = "초 분 시 일 월 요일"
     */
    @Scheduled(cron = "50 * * * * *")
    @Transactional
    public void processScheduledAlerts() {
        ZonedDateTime targetZdt = ZonedDateTime.now(ZoneId.of("Asia/Seoul")).plusSeconds(10).withSecond(0).withNano(0);
        String targetTime = targetZdt.format(DateTimeFormatter.ofPattern("HH:mm"));
        LocalDateTime alertMoment = targetZdt.toLocalDateTime();

        log.info("[스케줄러] tick (target {})", targetTime);

        List<Alert> activeAlerts = alertRepository.findAllByAlertTimeAndIsEnableTrue(targetTime);

        if (activeAlerts.isEmpty()) {
            log.info("[스케줄러] {} 발송 대상 없음", targetTime);
            return;
        }

        log.info("[스케줄러] {} 알림 발송 대상자 {}명 탐색 완료", targetTime, activeAlerts.size());
        for (Alert alert : activeAlerts) {
            Member targetMember = alert.getMember();
            LocationType locationType = alert.getEffectiveLocationType();

            try {
                RiskAnalysisResult result = riskAnalysisService.analyzeRiskForMemberAt(targetMember, locationType);

                if (result.isRisk()) {
                    saveNotificationHistory(targetMember, result, alertMoment, locationType);
                }

            } catch (Exception e) {
                log.error("회원 ID {} 알림 발송 중 오류 발생: {}", targetMember.getMemberId(), e.getMessage());
            }
        }
    }

    /**
     * 알림 기록을 예쁘게 포장해서 DB에 저장하는 헬퍼 메서드.
     * createdAt은 실제 적재 시각이 아닌 알람이 울려야 할 시각(alertMoment)으로 세팅.
     */
    private void saveNotificationHistory(Member member, RiskAnalysisResult result, LocalDateTime alertMoment, LocationType locationType) {

        String diseaseNamesStr = result.getRiskDetails().stream()
                .map(RiskAnalysisResult.RiskDetail::getDiseaseName)
                .collect(Collectors.joining(", "));

        String diseaseIdsStr = result.getRiskDetails().stream()
                .map(detail -> String.valueOf(detail.getDiseaseId()))
                .collect(Collectors.joining(","));

        // 위험 판정된 모든 질환의 원인 요인(미세먼지/초미세먼지 등)을 중복 없이 모은다.
        String factorNamesStr = result.getRiskDetails().stream()
                .flatMap(detail -> detail.getFactorGuides().stream())
                .map(RiskAnalysisResult.FactorGuide::getFactorName)
                .distinct()
                .collect(Collectors.joining(", "));

        String message = String.format("[%s] %s 수치가 위험 기준을 초과했어요.", diseaseNamesStr, factorNamesStr);

        NotificationHistory history = NotificationHistory.builder()
                .member(member)
                .diseaseIds(diseaseIdsStr)
                .diseaseNames(diseaseNamesStr)
                .message(message)
                .isRead(false)
                .createdAt(alertMoment)
                .locationType(locationType)
                .build();

        historyRepository.save(history);
        log.info("회원 ID {}에게 위험 알림 발송 완료: {}", member.getMemberId(), diseaseNamesStr);
    }
}