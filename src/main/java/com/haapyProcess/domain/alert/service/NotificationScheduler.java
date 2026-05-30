package com.haapyProcess.domain.alert.service;

import com.haapyProcess.domain.alert.entity.Alert;
import com.haapyProcess.domain.alert.entity.NotificationHistory;
import com.haapyProcess.domain.alert.repository.AlertRepository;
import com.haapyProcess.domain.alert.repository.NotificationHistoryRepository;
import com.haapyProcess.domain.analysis.dto.RiskAnalysisResult;
import com.haapyProcess.domain.analysis.service.RiskAnalysisService;
import com.haapyProcess.domain.family.entity.Family;
import com.haapyProcess.domain.family.repository.FamilyRepository;
import com.haapyProcess.domain.member.entity.Member;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.time.ZoneId;
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
    // 🚀 추가됨: 가족 스위치를 확인하기 위한 창고지기
    private final FamilyRepository familyRepository; 

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
                // 1. 유저 본인의 위험도 분석 및 알림 기록 (기존 로직)
                RiskAnalysisResult myResult = riskAnalysisService.analyzeRiskForMember(targetMember);
                if (myResult.isRisk()) {
                    saveNotificationHistory(targetMember, myResult, "본인");
                }

                // 2. 🚀 추가됨: 기획서 8, 9번 충족을 위한 가족 알림 분석
                // 스위치(isAlertEnabled)가 켜져 있는 가족들만 쏙쏙 뽑아옵니다.
                List<Family> activeFamilies = familyRepository.findAllByUser(targetMember).stream()
                        .filter(Family::isAlertEnabled)
                        .toList();

                for (Family family : activeFamilies) {
                    Member relative = family.getRelative();
                    try {
                        // 가족이 사는 동네의 날씨와 질병으로 위험도를 계산합니다.
                        RiskAnalysisResult familyResult = riskAnalysisService.analyzeRiskForMember(relative);
                        
                        if (familyResult.isRisk()) {
                            // 💡 기획서 9번: 가족의 위험 정보를 '나(targetMember)'의 알림 기록장에 저장!
                            saveNotificationHistory(targetMember, familyResult, relative.getName());
                        }
                    } catch (Exception e) {
                        log.error("가족 ID {} 알림 분석 중 오류: {}", relative.getMemberId(), e.getMessage());
                    }
                }

            } catch (Exception e) {
                log.error("회원 ID {} 알림 발송 중 오류 발생: {}", targetMember.getMemberId(), e.getMessage());
            }
        }
    }

    /**
     * 알림 기록 저장 헬퍼 메서드 (targetName 파라미터 추가됨)
     */
    private void saveNotificationHistory(Member receiver, RiskAnalysisResult result, String targetName) {
        String diseaseNamesStr = String.join(", ", result.getCauseDiseaseNames());
        String diseaseIdsStr = result.getCauseDiseaseIds().stream()
                .map(String::valueOf)
                .collect(Collectors.joining(","));

        // 메시지 조립: 본인이면 일반 메시지, 가족이면 [가족 - 아빠] 처럼 머리말을 붙여줍니다.
        String prefix = targetName.equals("본인") ? "" : "[가족 - " + targetName + "] ";
        String message = String.format("%s[%s] 현재 날씨가 위험 기준을 초과했습니다. 외출 시 주의하세요!", prefix, diseaseNamesStr);

        NotificationHistory history = NotificationHistory.builder()
                .member(receiver) // 알림의 주인은 무조건 '나'
                .diseaseIds(diseaseIdsStr)
                .diseaseNames(diseaseNamesStr)
                .message(message)
                .isRead(false)
                .build();

        historyRepository.save(history);
        log.info("회원 ID {}에게 위험 알림 발송 완료 (대상: {}): {}", receiver.getMemberId(), targetName, diseaseNamesStr);
    }
}