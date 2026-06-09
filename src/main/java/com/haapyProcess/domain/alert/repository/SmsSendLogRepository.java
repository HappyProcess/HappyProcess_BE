package com.haapyProcess.domain.alert.repository;

import com.haapyProcess.domain.alert.entity.SmsSendLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SmsSendLogRepository extends JpaRepository<SmsSendLog, Long> {
}
