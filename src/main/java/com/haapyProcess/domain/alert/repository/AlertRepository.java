package com.haapyProcess.domain.alert.repository;

import com.haapyProcess.domain.alert.entity.Alert;
import com.haapyProcess.domain.location.entity.LocationType;
import com.haapyProcess.domain.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AlertRepository extends JpaRepository<Alert, Long> {
    List<Alert> findAllByAlertTimeAndIsEnableTrue(String alertTime);
    List<Alert> findAllByMemberOrderByAlertTimeAsc(Member member);
    boolean existsByMemberAndAlertTimeAndLocationType(Member member, String alertTime, LocationType locationType);
}