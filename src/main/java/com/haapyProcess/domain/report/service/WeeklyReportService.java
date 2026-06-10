package com.haapyProcess.domain.report.service;

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
    public WeeklyReportResponse generateWeeklyReport(LocalDate weekStartParam) {
        Member member = memberService.getCurrentMember();
        LocalDate weekStart = resolveWeekStart(weekStartParam);
        LocalDate weekEnd = weekStart.plusDays(6);

        // 캐시 히트 시 재생성하지 않음 (비용 절약)
        WeeklyReport existing = reportRepository.findByMemberAndWeekStartDate(member, weekStart).orElse(null);
        if (existing != null) {
            return WeeklyReportResponse.from(existing);
        }

        List<SymptomDiary> diaries = diaryRepository.findByMemberAndEntryDateBetween(member, weekStart, weekEnd);
        if (diaries.isEmpty()) {
            throw new CustomException(ErrorCode.NO_DIARY_FOR_REPORT);
        }

        String prompt = buildPrompt(member, weekStart, weekEnd, diaries);
        String content = geminiClient.generate(prompt);

        WeeklyReport report = WeeklyReport.builder()
                .member(member)
                .weekStartDate(weekStart)
                .weekEndDate(weekEnd)
                .content(content)
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

    private String buildPrompt(Member member, LocalDate weekStart, LocalDate weekEnd, List<SymptomDiary> diaries) {
        StringBuilder sb = new StringBuilder();
        sb.append("당신은 만성질환 사용자의 건강 관리를 돕는 의료 코치입니다. ");
        sb.append("아래는 한 사용자가 ").append(weekStart).append("부터 ").append(weekEnd)
                .append("까지 기록한 증상 일기와 해당 시점의 날씨 데이터입니다.\n");
        sb.append("이 데이터를 바탕으로 한국어로 주간 건강 리포트를 작성해주세요. ");
        sb.append("다음을 포함하세요: (1) 이번 주 증상 추세 요약, (2) 날씨(미세먼지·꽃가루·자외선·기온 등)와 증상의 연관성 분석, ");
        sb.append("(3) 다음 주 관리 조언. 의학적 단정은 피하고 부드럽고 실용적인 톤으로, 400자 내외로 작성하세요. ");
        sb.append("반드시 마크다운 기호(**, ##, -, * 등) 없이 일반 평문으로만 작성하고, 항목 구분이 필요하면 줄바꿈만 사용하세요.\n\n");
        sb.append("=== 기록 데이터 ===\n");

        for (SymptomDiary d : diaries) {
            sb.append("[").append(d.getEntryDate()).append("]\n");
            sb.append("  날씨: 지역=").append(nv(d.getRegionName()))
                    .append(", 기온=").append(nv(d.getTemperature()))
                    .append(", 습도=").append(nv(d.getHumidity()))
                    .append(", 상태=").append(nv(d.getWeatherCondition()))
                    .append(", 미세먼지=").append(nv(d.getPm10Value())).append("(").append(nv(d.getPm10Grade())).append(")")
                    .append(", 초미세먼지=").append(nv(d.getPm25Value())).append("(").append(nv(d.getPm25Grade())).append(")")
                    .append(", 꽃가루위험=").append(nv(d.getPollenRiskLevel()))
                    .append(", 자외선=").append(nv(d.getUvRiskLevel())).append("\n");
            sb.append("  증상: ");
            if (d.getItems().isEmpty()) {
                sb.append("기록 없음");
            } else {
                List<String> parts = d.getItems().stream()
                        .map(this::formatSymptom)
                        .toList();
                sb.append(String.join(", ", parts));
            }
            sb.append("\n");
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
