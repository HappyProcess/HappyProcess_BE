package com.haapyProcess.domain.location.repository;

import com.haapyProcess.domain.location.entity.Location;
import com.haapyProcess.domain.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface LocationRepository extends JpaRepository<Location, Long> {
    List<Location> findAllByMember(Member member);
    Optional<Location> findByLocationIdAndMember(Long locationId, Member member);
}
