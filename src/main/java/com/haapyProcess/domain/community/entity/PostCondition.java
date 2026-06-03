package com.haapyProcess.domain.community.entity;

import com.haapyProcess.domain.condition.entity.Condition;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "POST_CONDITION")
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class PostCondition {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "POST_CONDITION_ID")
    private Long postConditionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "POST_ID", nullable = false)
    private Post post;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CONDITION_ID", nullable = false)
    private Condition condition;
}