package com.haapyProcess.domain.condition.entity;

import com.haapyProcess.domain.healthcondition.entity.HealthCondition;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "CONDITIONS")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Condition {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "CONDITION_ID")
    private Long conditionId;

    @Column(name = "CONDITION_NAME", length = 50, nullable = false)
    private String conditionName;

    @OneToMany(mappedBy = "condition", cascade = CascadeType.ALL)
    private List<HealthCondition> healthConditions = new ArrayList<>();
}