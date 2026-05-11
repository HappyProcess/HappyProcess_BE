package com.haapyProcess.domain.healthcondition.repository;

import com.haapyProcess.domain.healthcondition.entity.HealthCondition;
import com.haapyProcess.domain.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface HealthConditionRepository extends JpaRepository<HealthCondition, Long> {

    List<HealthCondition> findAllByMember(Member member);

    void deleteAllByMember(Member member);
}
