package com.haapyProcess.domain.auth.repository;

import com.haapyProcess.domain.auth.entity.RefreshToken;
import com.haapyProcess.domain.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByMember(Member member);

    Optional<RefreshToken> findByToken(String token);

    void deleteByMember(Member member);
}
