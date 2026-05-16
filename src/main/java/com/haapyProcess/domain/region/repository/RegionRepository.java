// src/main/java/com/haapyProcess/domain/region/repository/RegionRepository.java
package com.haapyProcess.domain.region.repository;

import com.haapyProcess.domain.region.entity.Region;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface RegionRepository extends JpaRepository<Region, String> {

    // 1. 모든 '시도' 목록을 중복 없이 가져오기 (시도 선택용)
    @Query("SELECT DISTINCT r.sido FROM Region r WHERE r.sido IS NOT NULL ORDER BY r.sido")
    List<String> findDistinctSido();

    // 2. 특정 '시도'에 속한 '시군구' 목록 중복 없이 가져오기 (시군구 선택용)
    @Query("SELECT DISTINCT r.sigungu FROM Region r WHERE r.sido = :sido AND r.sigungu IS NOT NULL ORDER BY r.sigungu")
    List<String> findDistinctSigunguBySido(@Param("sido") String sido);

    // 3. 특정 '시도'와 '시군구'에 속한 '동' 목록 가져오기 (동 선택용)
    @Query("SELECT r FROM Region r WHERE r.sido = :sido AND r.sigungu = :sigungu AND r.dong IS NOT NULL ORDER BY r.dong")
    List<Region> findDongList(@Param("sido") String sido, @Param("sigungu") String sigungu);
}