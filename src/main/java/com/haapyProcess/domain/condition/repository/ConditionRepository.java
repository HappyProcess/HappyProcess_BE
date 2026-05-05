package com.haapyProcess.domain.condition.repository;

import com.haapyProcess.domain.condition.entity.Condition;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ConditionRepository extends JpaRepository<Condition, Long> {
}
