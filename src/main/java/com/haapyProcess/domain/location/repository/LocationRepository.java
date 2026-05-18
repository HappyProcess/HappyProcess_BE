package com.haapyProcess.domain.location.repository;

import com.haapyProcess.domain.location.entity.Location;
import com.haapyProcess.domain.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface LocationRepository extends JpaRepository<Location, Long> {

    @Query("SELECT l FROM Location l JOIN FETCH l.region WHERE l.member = :member")
    List<Location> findAllByMember(@Param("member") Member member);

    Optional<Location> findByLocationIdAndMember(Long locationId, Member member);
}