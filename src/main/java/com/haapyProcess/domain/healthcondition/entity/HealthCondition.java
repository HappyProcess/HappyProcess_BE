package com.haapyProcess.domain.healthcondition.entity;

import com.haapyProcess.domain.member.entity.Member;
import com.haapyProcess.domain.condition.entity.Condition;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "HEALTH_CONDITION")
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class HealthCondition {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "HEALTH_CONDITION_ID")
    private Long healthConditionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MEMBER_ID")
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CONDITION_ID")
    private Condition condition;
}
