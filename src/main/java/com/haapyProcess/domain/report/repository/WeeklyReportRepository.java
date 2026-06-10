package com.haapyProcess.domain.report.repository;

import com.haapyProcess.domain.member.entity.Member;
import com.haapyProcess.domain.report.entity.WeeklyReport;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;

public interface WeeklyReportRepository extends JpaRepository<WeeklyReport, Long> {

    Optional<WeeklyReport> findByMemberAndWeekStartDate(Member member, LocalDate weekStartDate);
}
