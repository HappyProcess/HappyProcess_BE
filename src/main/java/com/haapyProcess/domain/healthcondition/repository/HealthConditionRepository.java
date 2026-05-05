package com.haapyProcess.domain.healthcondition.repository;

import com.haapyProcess.domain.healthcondition.entity.HealthCondition;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HealthConditionRepository extends JpaRepository<HealthCondition, Long> {
}
