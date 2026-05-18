package com.haapyProcess.domain.region.repository;

import com.haapyProcess.domain.region.dto.RegionResponse; // 👈 DTO import 추가
import com.haapyProcess.domain.region.entity.Region;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface RegionRepository extends JpaRepository<Region, String> {

    @Query("SELECT DISTINCT r.sido FROM Region r WHERE r.sido IS NOT NULL ORDER BY r.sido")
    List<String> findDistinctSido();

    @Query("SELECT DISTINCT r.sigungu FROM Region r WHERE r.sido = :sido AND r.sigungu IS NOT NULL ORDER BY r.sigungu")
    List<String> findDistinctSigunguBySido(@Param("sido") String sido);

    @Query("SELECT new com.haapyProcess.domain.region.dto.RegionResponse(r.areaNo, r.dong) " +
            "FROM Region r WHERE r.sido = :sido AND r.sigungu = :sigungu AND r.dong IS NOT NULL ORDER BY r.dong")
    List<RegionResponse> findDongList(@Param("sido") String sido, @Param("sigungu") String sigungu);
}