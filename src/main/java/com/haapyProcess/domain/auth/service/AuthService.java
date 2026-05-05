package com.haapyProcess.domain.auth.service;

import com.haapyProcess.domain.auth.dto.*;
import com.haapyProcess.domain.auth.entity.RefreshToken;
import com.haapyProcess.domain.auth.repository.RefreshTokenRepository;
import com.haapyProcess.domain.member.entity.Member;
import com.haapyProcess.domain.member.repository.MemberRepository;
import com.haapyProcess.global.config.JwtProperties;
import com.haapyProcess.global.exception.CustomException;
import com.haapyProcess.global.exception.ErrorCode;
import com.haapyProcess.global.jwt.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final MemberRepository memberRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;
    private final JwtProperties jwtProperties;

    @Transactional
    public LoginResponse login(LoginRequest request) {
        Member member = memberRepository.findByLoginId(request.getLoginId())
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        if (!passwordEncoder.matches(request.getPassword(), member.getPw())) {
            throw new CustomException(ErrorCode.INVALID_PASSWORD);
        }

        String accessToken = jwtTokenProvider.generateAccessToken(member.getLoginId());
        String refreshToken = jwtTokenProvider.generateRefreshToken(member.getLoginId());
        LocalDateTime expiresAt = LocalDateTime.now()
                .plusSeconds(jwtProperties.getRefreshExpiration() / 1000);

        refreshTokenRepository.findByMember(member)
                .ifPresentOrElse(
                        rt -> rt.updateToken(refreshToken, expiresAt),
                        () -> refreshTokenRepository.save(
                                RefreshToken.builder()
                                        .member(member)
                                        .token(refreshToken)
                                        .expiresAt(expiresAt)
                                        .build()
                        )
                );

        return new LoginResponse(accessToken, refreshToken);
    }

    @Transactional
    public void logout(LogoutRequest request) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(request.getRefreshToken())
                .orElseThrow(() -> new CustomException(ErrorCode.INVALID_TOKEN));
        refreshTokenRepository.delete(refreshToken);
    }

    @Transactional
    public ReissueResponse reissue(ReissueRequest request) {
        if (!jwtTokenProvider.validateToken(request.getRefreshToken())) {
            throw new CustomException(ErrorCode.EXPIRED_TOKEN);
        }

        RefreshToken refreshToken = refreshTokenRepository.findByToken(request.getRefreshToken())
                .orElseThrow(() -> new CustomException(ErrorCode.INVALID_TOKEN));

        Member member = refreshToken.getMember();
        String newAccessToken = jwtTokenProvider.generateAccessToken(member.getLoginId());
        String newRefreshToken = jwtTokenProvider.generateRefreshToken(member.getLoginId());
        LocalDateTime expiresAt = LocalDateTime.now()
                .plusSeconds(jwtProperties.getRefreshExpiration() / 1000);

        refreshToken.updateToken(newRefreshToken, expiresAt);

        return new ReissueResponse(newAccessToken, newRefreshToken);
    }
}
