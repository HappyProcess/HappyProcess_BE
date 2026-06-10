package com.haapyProcess.integration;

import com.haapyProcess.domain.condition.entity.Condition;
import com.haapyProcess.domain.condition.repository.ConditionRepository;
import com.haapyProcess.domain.diary.dto.DiaryRequest;
import com.haapyProcess.domain.diary.dto.DiaryResponse;
import com.haapyProcess.domain.diary.service.SymptomDiaryService;
import com.haapyProcess.domain.member.entity.Member;
import com.haapyProcess.domain.member.repository.MemberRepository;
import com.haapyProcess.domain.report.dto.WeeklyReportResponse;
import com.haapyProcess.domain.report.service.WeeklyReportService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 일기 작성 -> 주간 리포트 생성 end-to-end 통합 테스트.
 * H2 인메모리 DB + 실제 빈(서비스/리포지토리/GeminiClient)로 동작하며,
 * 실제 Gemini API를 호출하므로 GEMINI_API_KEY 환경변수가 있을 때만 실행된다.
 */
@SpringBootTest
@EnabledIfEnvironmentVariable(named = "GEMINI_API_KEY", matches = ".+")
class DiaryReportIntegrationTest {

    @DynamicPropertySource
    static void props(DynamicPropertyRegistry r) {
        r.add("spring.datasource.url", () -> "jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;MODE=PostgreSQL");
        r.add("spring.datasource.driver-class-name", () -> "org.h2.Driver");
        r.add("spring.datasource.username", () -> "sa");
        r.add("spring.datasource.password", () -> "");
        r.add("spring.jpa.properties.hibernate.dialect", () -> "org.hibernate.dialect.H2Dialect");
        r.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
        r.add("spring.sql.init.mode", () -> "never");
        r.add("gemini.api-key", () -> System.getenv("GEMINI_API_KEY"));
        r.add("gemini.model", () -> "gemini-3.1-flash-lite");
        // WeatherService @Value 들 (기본 application.yml엔 없음). 본 테스트는 날씨 호출을 하지 않으므로 더미.
        r.add("weather.api.key", () -> "dummy");
        r.add("weather.api.urls.pollen", () -> "http://localhost");
        r.add("weather.api.urls.air-pollution", () -> "http://localhost");
        r.add("weather.api.urls.forecast", () -> "http://localhost");
        r.add("weather.api.urls.living", () -> "http://localhost");
    }

    @Autowired MemberRepository memberRepository;
    @Autowired ConditionRepository conditionRepository;
    @Autowired SymptomDiaryService diaryService;
    @Autowired WeeklyReportService reportService;

    @Test
    void diary_to_weekly_report_end_to_end() throws Exception {
        // 1. 회원 + 질환 준비
        Member member = memberRepository.save(Member.builder()
                .loginId("e2euser").pw("pw").name("테스트유저").build());

        var ctor = Condition.class.getDeclaredConstructor();
        ctor.setAccessible(true);
        Condition condition = ctor.newInstance();
        ReflectionTestUtils.setField(condition, "conditionName", "천식");
        condition = conditionRepository.save(condition);

        // 2. 인증 컨텍스트 (getCurrentMember가 loginId로 조회)
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("e2euser", null, List.of()));

        // 3. 지난 주 월~수에 증상 일기 작성 (위치 미등록 -> 날씨 외부 API 호출 없음)
        LocalDate monday = LocalDate.now().minusWeeks(1).with(DayOfWeek.MONDAY);
        diaryService.upsertDiary(req(monday, "아침에 기침이 심했음", condition.getConditionId(), 4));
        diaryService.upsertDiary(req(monday.plusDays(1), "조금 나아짐", condition.getConditionId(), 2));
        DiaryResponse wed = diaryService.upsertDiary(req(monday.plusDays(2), "미세먼지 많은 날 다시 악화", condition.getConditionId(), 5));

        assertThat(wed.getSymptoms()).hasSize(1);
        assertThat(wed.getSymptoms().get(0).getConditionName()).isEqualTo("천식");

        // 4. 실제 Gemini로 주간 리포트 생성
        WeeklyReportResponse report = reportService.generateWeeklyReport(monday);

        System.out.println("\n========= 주간 리포트 (" + report.getWeekStartDate() + " ~ " + report.getWeekEndDate() + ") =========");
        System.out.println("summary: " + report.getSummary());
        System.out.println("patterns: " + report.getPatterns());
        System.out.println("solutions: " + report.getSolutions());
        System.out.println("====================================================\n");

        assertThat(report.getSummary()).isNotBlank();
        assertThat(report.getWeekStartDate()).isEqualTo(monday);

        // 5. 재호출 시 동일 캐시 반환 (재생성 안 함)
        WeeklyReportResponse cached = reportService.generateWeeklyReport(monday);
        assertThat(cached.getReportId()).isEqualTo(report.getReportId());
    }

    private DiaryRequest req(LocalDate date, String memo, Long conditionId, int intensity) {
        DiaryRequest r = new DiaryRequest();
        ReflectionTestUtils.setField(r, "entryDate", date);
        ReflectionTestUtils.setField(r, "memo", memo);
        DiaryRequest.SymptomItem item = new DiaryRequest.SymptomItem();
        ReflectionTestUtils.setField(item, "conditionId", conditionId);
        ReflectionTestUtils.setField(item, "intensity", intensity);
        ReflectionTestUtils.setField(r, "symptoms", List.of(item));
        return r;
    }
}
