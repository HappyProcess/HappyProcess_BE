package com.haapyProcess.domain.report.service;

import com.haapyProcess.domain.diary.entity.DiaryWeather;
import com.haapyProcess.domain.diary.entity.SymptomDiary;
import com.haapyProcess.domain.diary.entity.SymptomDiaryItem;
import com.haapyProcess.domain.diary.repository.SymptomDiaryRepository;
import com.haapyProcess.domain.member.entity.Member;
import com.haapyProcess.domain.member.service.MemberService;
import com.haapyProcess.domain.report.dto.WeeklyReportResponse;
import com.haapyProcess.domain.report.entity.WeeklyReport;
import com.haapyProcess.domain.report.repository.WeeklyReportRepository;
import com.haapyProcess.global.exception.CustomException;
import com.haapyProcess.global.exception.ErrorCode;
import com.haapyProcess.global.gemini.GeminiClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class WeeklyReportService {

    private final SymptomDiaryRepository diaryRepository;
    private final WeeklyReportRepository reportRepository;
    private final GeminiClient geminiClient;
    private final MemberService memberService;

    /**
     * 주간 리포트 온디맨드 생성. 이미 생성된 주차면 캐시를 반환한다.
     * @param weekStartParam 주차 내 임의의 날짜(월요일로 정규화). null이면 지난 주.
     */
    @Transactional
    public WeeklyReportResponse generateWeeklyReport(LocalDate weekStartParam, boolean force) {
        Member member = memberService.getCurrentMember();
        LocalDate weekStart = resolveWeekStart(weekStartParam);
        LocalDate weekEnd = weekStart.plusDays(6);

        WeeklyReport existing = reportRepository.findByMemberAndWeekStartDate(member, weekStart).orElse(null);
        // force가 아니면 캐시 반환. force면 기존 리포트를 새로 생성해 덮어쓴다(구버전 포맷 교체용).
        if (existing != null && !force) {
            return WeeklyReportResponse.from(existing);
        }

        List<SymptomDiary> diaries = diaryRepository.findByMemberAndEntryDateBetween(member, weekStart, weekEnd);
        if (diaries.isEmpty()) {
            throw new CustomException(ErrorCode.NO_DIARY_FOR_REPORT);
        }

        String prompt = buildPrompt(weekStart, weekEnd, diaries);
        String contentJson = geminiClient.generateJson(prompt);

        if (existing != null) {
            existing.updateContent(contentJson);
            return WeeklyReportResponse.from(reportRepository.save(existing));
        }

        WeeklyReport report = WeeklyReport.builder()
                .member(member)
                .weekStartDate(weekStart)
                .weekEndDate(weekEnd)
                .content(contentJson)
                .build();

        return WeeklyReportResponse.from(reportRepository.save(report));
    }

    @Transactional(readOnly = true)
    public WeeklyReportResponse getWeeklyReport(LocalDate weekStartParam) {
        Member member = memberService.getCurrentMember();
        LocalDate weekStart = resolveWeekStart(weekStartParam);
        WeeklyReport report = reportRepository.findByMemberAndWeekStartDate(member, weekStart)
                .orElseThrow(() -> new CustomException(ErrorCode.REPORT_NOT_FOUND));
        return WeeklyReportResponse.from(report);
    }

    /** 파라미터가 없으면 지난 주, 있으면 해당 날짜가 속한 주의 월요일로 정규화한다. */
    private LocalDate resolveWeekStart(LocalDate weekStartParam) {
        LocalDate base = (weekStartParam != null) ? weekStartParam : LocalDate.now().minusWeeks(1);
        return base.with(DayOfWeek.MONDAY);
    }

    private String buildPrompt(LocalDate weekStart, LocalDate weekEnd, List<SymptomDiary> diaries) {
        StringBuilder sb = new StringBuilder();
        sb.append("당신은 만성질환 사용자의 건강 데이터를 분석하는 의료 코치입니다.\n");
        sb.append("아래는 한 사용자가 ").append(weekStart).append("부터 ").append(weekEnd)
                .append("까지 기록한 증상 일기와, 집(HOME)/직장·학교(WORK) 위치별 날씨 데이터입니다.\n");
        sb.append("이 데이터를 분석해 '어떤 날씨 조건일 때 어떤 증상이 유독 심해지는지'를 구체적으로 찾아내고, 그에 대한 실천 가능한 솔루션을 제시하세요.\n\n");

        sb.append("반드시 아래 JSON 스키마로만 응답하세요. 코드블록(```)이나 다른 설명 텍스트 없이 순수 JSON만 출력합니다.\n");
        sb.append("{\n");
        sb.append("  \"summary\": \"이번 주 전반 추세를 1~2문장으로 요약\",\n");
        sb.append("  \"patterns\": [ {\"weather\": \"구체적 날씨 조건(가능하면 수치 포함, 예: 미세먼지 나쁨 PM10 80)\", \"symptom\": \"증상 또는 질환명\", \"observation\": \"그 조건에서 증상이 어떻게 나타났는지 강도 등 구체적으로\"} ],\n");
        sb.append("  \"solutions\": [ {\"target\": \"증상 또는 질환명\", \"action\": \"구체적이고 실천 가능한 조치\"} ]\n");
        sb.append("}\n");
        sb.append("규칙: patterns는 데이터에서 실제로 관찰되는 연관만 적고 막연한 일반론은 금지합니다. ");
        sb.append("데이터가 부족하거나 날씨 변화가 거의 없어 연관을 찾기 어려우면 patterns를 빈 배열로 두고 summary에 그 사실을 적으세요. 모든 문자열은 한국어로 작성합니다.\n\n");

        sb.append("=== 기록 데이터 ===\n");
        for (SymptomDiary d : diaries) {
            sb.append("[").append(d.getEntryDate()).append("]\n");
            sb.append("  증상: ");
            if (d.getItems().isEmpty()) {
                sb.append("기록 없음");
            } else {
                sb.append(String.join(", ", d.getItems().stream().map(this::formatSymptom).toList()));
            }
            sb.append("\n");
            if (d.getWeathers().isEmpty()) {
                sb.append("  날씨: 없음\n");
            } else {
                for (DiaryWeather w : d.getWeathers()) {
                    sb.append("  날씨(").append(w.getLocationType()).append(", ").append(nv(w.getRegionName())).append("): ")
                            .append("기온=").append(nv(w.getTemperature()))
                            .append(", 습도=").append(nv(w.getHumidity()))
                            .append(", 상태=").append(nv(w.getWeatherCondition()))
                            .append(", 미세먼지=").append(nv(w.getPm10Value())).append("(").append(nv(w.getPm10Grade())).append(")")
                            .append(", 초미세먼지=").append(nv(w.getPm25Value())).append("(").append(nv(w.getPm25Grade())).append(")")
                            .append(", 꽃가루위험=").append(nv(w.getPollenRiskLevel()))
                            .append(", 자외선=").append(nv(w.getUvRiskLevel())).append("\n");
                }
            }
            if (d.getMemo() != null && !d.getMemo().isBlank()) {
                sb.append("  메모: ").append(d.getMemo()).append("\n");
            }
        }
        return sb.toString();
    }

    private String formatSymptom(SymptomDiaryItem item) {
        return item.getCondition().getConditionName() + " 강도" + item.getIntensity();
    }

    private String nv(String v) {
        return (v == null || v.isBlank()) ? "-" : v;
    }
}
