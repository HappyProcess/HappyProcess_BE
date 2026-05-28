package com.haapyProcess.domain.alert.repository;

import com.haapyProcess.domain.alert.entity.NotificationHistory;
import com.haapyProcess.domain.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationHistoryRepository extends JpaRepository<NotificationHistory, Long> {
    List<NotificationHistory> findAllByMemberOrderByCreatedAtDesc(Member member);
}