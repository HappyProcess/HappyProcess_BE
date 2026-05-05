package com.haapyProcess.domain.location.repository;

import com.haapyProcess.domain.location.entity.Location;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LocationRepository extends JpaRepository<Location, Long> {
}
