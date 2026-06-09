package com.haapyProcess.domain.alert.service;

import com.haapyProcess.domain.alert.entity.Alert;
import com.haapyProcess.domain.alert.entity.NotificationHistory;
import com.haapyProcess.domain.alert.repository.AlertRepository;
import com.haapyProcess.domain.alert.repository.NotificationHistoryRepository;
import com.haapyProcess.domain.analysis.dto.RiskAnalysisResult;
import com.haapyProcess.domain.analysis.service.RiskAnalysisService;
import com.haapyProcess.domain.family.entity.Family;
import com.haapyProcess.domain.family.repository.FamilyRepository;
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
    private final FamilyRepository familyRepository;
    private final org.springframework.context.ApplicationEventPublisher eventPublisher;

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
                // 1. 알림 주인 본인의 위험도 분석 → 본인 알림함에 저장
                RiskAnalysisResult result = riskAnalysisService.analyzeRiskForMemberAt(targetMember, locationType);
                if (result.isRisk()) {
                    saveNotificationHistory(targetMember, result, alertMoment, locationType, null);
                }

                // 2. 이 알림 주인(targetMember)을 가족으로 등록하고 알림을 켜둔 사람들에게도 전파.
                //    가족 알림은 가족(relative) 본인의 알림 시각·지역 설정을 그대로 따른다.
                fanOutToFamilyRegistrants(targetMember, result, alertMoment, locationType);

            } catch (Exception e) {
                log.error("회원 ID {} 알림 발송 중 오류 발생: {}", targetMember.getMemberId(), e.getMessage());
            }
        }
    }

    /**
     * targetMember(가족 본인)의 위험 분석 결과를, 그를 가족으로 등록하고 알림을 켜둔
     * 모든 사용자(registrant)의 알림함에 [가족 - 이름] 형태로 적재한다.
     */
    private void fanOutToFamilyRegistrants(Member relative, RiskAnalysisResult result,
                                           LocalDateTime alertMoment, LocationType locationType) {
        if (!result.isRisk()) {
            return;
        }
        List<Family> registrations = familyRepository.findAllByRelativeAndIsAlertEnabledTrue(relative);
        for (Family family : registrations) {
            try {
                saveNotificationHistory(family.getUser(), result, alertMoment, locationType, relative.getName());
            } catch (Exception e) {
                log.error("가족 알림 전파 실패 (수신자 {} / 가족 {}): {}",
                        family.getUser().getMemberId(), relative.getMemberId(), e.getMessage());
            }
        }
    }

    /**
     * 알림 기록을 DB에 저장하는 헬퍼 메서드.
     * createdAt은 실제 적재 시각이 아닌 알람이 울려야 할 시각(alertMoment)으로 세팅.
     * relativeName이 null이면 본인 알림, 값이 있으면 해당 가족 기준 알림이다.
     */
    private void saveNotificationHistory(Member receiver, RiskAnalysisResult result, LocalDateTime alertMoment,
                                         LocationType locationType, String relativeName) {

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

        String prefix = (relativeName != null && !relativeName.isBlank()) ? "[가족 - " + relativeName + "] " : "";
        String message = String.format("%s[%s] %s 수치가 위험 기준을 초과했어요.", prefix, diseaseNamesStr, factorNamesStr);

        NotificationHistory history = NotificationHistory.builder()
                .member(receiver)
                .diseaseIds(diseaseIdsStr)
                .diseaseNames(diseaseNamesStr)
                .factorNames(factorNamesStr)
                .message(message)
                .isRead(false)
                .createdAt(alertMoment)
                .locationType(locationType)
                .relativeName(relativeName)
                .build();

        historyRepository.save(history);
        log.info("회원 ID {}에게 위험 알림 발송 완료 (대상: {}): {}",
                receiver.getMemberId(), relativeName == null ? "본인" : relativeName, diseaseNamesStr);

        // 트랜잭션 커밋 후 비동기로 문자 발송 (수신자 본인/가족 모두)
        eventPublisher.publishEvent(new com.haapyProcess.domain.alert.event.NotificationCreatedEvent(
                receiver.getMemberId(), receiver.getPhoneNumber(), message));
    }
}
