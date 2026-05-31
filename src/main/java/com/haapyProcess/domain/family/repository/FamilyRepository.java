package com.haapyProcess.domain.family.repository;

import com.haapyProcess.domain.family.entity.Family;
import com.haapyProcess.domain.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface FamilyRepository extends JpaRepository<Family, Long> {
    
    // 1. 내가 등록한 모든 가족 목록 가져오기
    List<Family> findAllByUser(Member user);

    // 2. 내 가족 중에 특정 가족(familyId) 한 명 찾기 (보안용)
    Optional<Family> findByFamilyIdAndUser(Long familyId, Member user);

    // 3. 이미 등록한 가족인지 중복 체크용
    boolean existsByUserAndRelative(Member user, Member relative);

    // 4. 특정 회원을 가족으로 등록하고 알림을 켜둔 모든 등록 건 (스케줄러 팬아웃용)
    List<Family> findAllByRelativeAndIsAlertEnabledTrue(Member relative);

    // 5. 리스트 조회용: 가족(relative) + 질환 + 질환명을 한 방에 fetch (N+1 방지)
    //    alerts는 별도 IN 조회로 처리(컬렉션 두 개 동시 fetch 시 MultipleBagFetchException 방지)
    @Query("SELECT DISTINCT f FROM Family f " +
            "JOIN FETCH f.relative r " +
            "LEFT JOIN FETCH r.healthConditions hc " +
            "LEFT JOIN FETCH hc.condition " +
            "WHERE f.user = :user")
    List<Family> findAllByUserWithConditions(@Param("user") Member user);
}