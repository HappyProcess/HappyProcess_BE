package com.haapyProcess.domain.healthcondition.repository;

import com.haapyProcess.domain.healthcondition.entity.HealthCondition;
import com.haapyProcess.domain.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface HealthConditionRepository extends JpaRepository<HealthCondition, Long> {

    // condition을 함께 fetch join 하여 N+1 방지 (조회 시 행마다 추가 쿼리 발생 제거)
    @Query("SELECT hc FROM HealthCondition hc JOIN FETCH hc.condition WHERE hc.member = :member")
    List<HealthCondition> findAllByMember(@Param("member") Member member);

    void deleteAllByMember(Member member);
}
