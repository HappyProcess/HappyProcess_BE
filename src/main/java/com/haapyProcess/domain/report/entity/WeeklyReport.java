package com.haapyProcess.domain.report.entity;

import com.haapyProcess.domain.member.entity.Member;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Gemini가 생성한 주간 리포트. 온디맨드로 생성한 뒤 DB에 캐시한다.
 * member + weekStartDate 조합이 유니크하다.
 */
@Entity
@Table(
        name = "WEEKLY_REPORT",
        uniqueConstraints = @UniqueConstraint(name = "UK_REPORT_MEMBER_WEEK", columnNames = {"MEMBER_ID", "WEEK_START_DATE"})
)
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class WeeklyReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "WEEKLY_REPORT_ID")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MEMBER_ID")
    private Member member;

    @Column(name = "WEEK_START_DATE", nullable = false)
    private LocalDate weekStartDate;

    @Column(name = "WEEK_END_DATE", nullable = false)
    private LocalDate weekEndDate;

    @Column(name = "CONTENT", columnDefinition = "TEXT", nullable = false)
    private String content;

    @Column(name = "CREATED_AT", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    public void updateContent(String content) {
        this.content = content;
        this.createdAt = LocalDateTime.now();
    }
}
