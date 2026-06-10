package com.haapyProcess.domain.report.service;

import com.haapyProcess.domain.diary.entity.SymptomDiary;
import com.haapyProcess.domain.diary.repository.SymptomDiaryRepository;
import com.haapyProcess.domain.member.entity.Member;
import com.haapyProcess.domain.member.service.MemberService;
import com.haapyProcess.domain.report.dto.WeeklyReportResponse;
import com.haapyProcess.domain.report.entity.WeeklyReport;
import com.haapyProcess.domain.report.repository.WeeklyReportRepository;
import com.haapyProcess.global.exception.CustomException;
import com.haapyProcess.global.exception.ErrorCode;
import com.haapyProcess.global.gemini.GeminiClient;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WeeklyReportServiceTest {

    @Mock SymptomDiaryRepository diaryRepository;
    @Mock WeeklyReportRepository reportRepository;
    @Mock GeminiClient geminiClient;
    @Mock MemberService memberService;

    @InjectMocks WeeklyReportService service;

    private final Member member = Member.builder().build();

    @Test
    @DisplayName("weekStart 파라미터는 해당 주의 월요일로 정규화되어 조회된다")
    void normalizesToMonday() {
        when(memberService.getCurrentMember()).thenReturn(member);
        // 2025-06-04는 수요일 -> 그 주 월요일 2025-06-02
        LocalDate wednesday = LocalDate.of(2025, 6, 4);
        LocalDate expectedMonday = LocalDate.of(2025, 6, 2);
        assertThat(expectedMonday.getDayOfWeek()).isEqualTo(DayOfWeek.MONDAY);

        SymptomDiary diary = SymptomDiary.builder().member(member).entryDate(expectedMonday).build();
        when(reportRepository.findByMemberAndWeekStartDate(member, expectedMonday)).thenReturn(Optional.empty());
        when(diaryRepository.findByMemberAndEntryDateBetween(member, expectedMonday, expectedMonday.plusDays(6)))
                .thenReturn(List.of(diary));
        when(geminiClient.generate(any())).thenReturn("리포트 본문");
        when(reportRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        WeeklyReportResponse res = service.generateWeeklyReport(wednesday);

        assertThat(res.getWeekStartDate()).isEqualTo(expectedMonday);
        assertThat(res.getWeekEndDate()).isEqualTo(expectedMonday.plusDays(6));
        assertThat(res.getContent()).isEqualTo("리포트 본문");
    }

    @Test
    @DisplayName("이미 생성된 주차면 Gemini 재호출 없이 캐시를 반환한다")
    void returnsCacheWithoutCallingGemini() {
        when(memberService.getCurrentMember()).thenReturn(member);
        LocalDate monday = LocalDate.of(2025, 6, 2);
        WeeklyReport cached = WeeklyReport.builder()
                .member(member).weekStartDate(monday).weekEndDate(monday.plusDays(6))
                .content("캐시된 리포트").build();
        when(reportRepository.findByMemberAndWeekStartDate(member, monday)).thenReturn(Optional.of(cached));

        WeeklyReportResponse res = service.generateWeeklyReport(monday);

        assertThat(res.getContent()).isEqualTo("캐시된 리포트");
        verify(geminiClient, never()).generate(any());
        verify(reportRepository, never()).save(any());
    }

    @Test
    @DisplayName("해당 주에 일기가 없으면 NO_DIARY_FOR_REPORT 예외")
    void throwsWhenNoDiary() {
        when(memberService.getCurrentMember()).thenReturn(member);
        LocalDate monday = LocalDate.of(2025, 6, 2);
        when(reportRepository.findByMemberAndWeekStartDate(member, monday)).thenReturn(Optional.empty());
        when(diaryRepository.findByMemberAndEntryDateBetween(eq(member), eq(monday), eq(monday.plusDays(6))))
                .thenReturn(List.of());

        assertThatThrownBy(() -> service.generateWeeklyReport(monday))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(ErrorCode.NO_DIARY_FOR_REPORT);
    }

    @Test
    @DisplayName("프롬프트에 증상·날씨 데이터가 포함된다")
    void promptIncludesDiaryData() {
        when(memberService.getCurrentMember()).thenReturn(member);
        LocalDate monday = LocalDate.of(2025, 6, 2);
        SymptomDiary diary = SymptomDiary.builder()
                .member(member).entryDate(monday).memo("두통이 있었음").build();
        diary.applyWeatherSnapshot("강남구", "25", "60", "맑음",
                "80", "나쁨", "40", "보통", "3", "5");

        when(reportRepository.findByMemberAndWeekStartDate(member, monday)).thenReturn(Optional.empty());
        when(diaryRepository.findByMemberAndEntryDateBetween(member, monday, monday.plusDays(6)))
                .thenReturn(List.of(diary));
        when(geminiClient.generate(any())).thenReturn("결과");
        when(reportRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        service.generateWeeklyReport(monday);

        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(geminiClient).generate(captor.capture());
        String prompt = captor.getValue();
        assertThat(prompt).contains("강남구").contains("두통이 있었음").contains("미세먼지=80");
    }
}
