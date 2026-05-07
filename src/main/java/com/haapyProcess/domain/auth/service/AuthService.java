package com.haapyProcess.domain.auth.service;

import com.haapyProcess.domain.auth.dto.*;
import com.haapyProcess.domain.auth.entity.RefreshToken;
import com.haapyProcess.domain.auth.repository.RefreshTokenRepository;
import com.haapyProcess.domain.condition.entity.Condition;
import com.haapyProcess.domain.condition.repository.ConditionRepository;
import com.haapyProcess.domain.healthcondition.entity.HealthCondition;
import com.haapyProcess.domain.healthcondition.repository.HealthConditionRepository;
import com.haapyProcess.domain.location.entity.CityCoordinate;
import com.haapyProcess.domain.location.entity.Location;
import com.haapyProcess.domain.location.entity.LocationType;
import com.haapyProcess.domain.location.repository.LocationRepository;
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

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final MemberRepository memberRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final ConditionRepository conditionRepository;
    private final LocationRepository locationRepository;
    private final HealthConditionRepository healthConditionRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;
    private final JwtProperties jwtProperties;

    @Transactional
    public SignUpResponse signUp(SignUpRequest request) {
        if (memberRepository.existsByLoginId(request.getLoginId())) {
            throw new CustomException(ErrorCode.DUPLICATE_LOGIN_ID);
        }

        boolean hasHome = request.getLocations().stream()
                .anyMatch(l -> l.getLocationType() == LocationType.HOME);
        boolean hasWork = request.getLocations().stream()
                .anyMatch(l -> l.getLocationType() == LocationType.WORK);
        if (!hasHome || !hasWork) {
            throw new CustomException(ErrorCode.INVALID_LOCATION);
        }

        Member member = Member.builder()
                .loginId(request.getLoginId())
                .pw(passwordEncoder.encode(request.getPassword()))
                .name(request.getName())
                .birth(request.getBirth())
                .commuteTime(request.getCommuteTime())
                .build();
        memberRepository.save(member);

        for (SignUpRequest.LocationRequest loc : request.getLocations()) {
            CityCoordinate coord;
            try {
                coord = CityCoordinate.valueOf(loc.getCity());
            } catch (IllegalArgumentException e) {
                throw new CustomException(ErrorCode.INVALID_CITY);
            }
            Location location = Location.builder()
                    .member(member)
                    .locationType(loc.getLocationType())
                    .city(loc.getCity())
                    .lat(BigDecimal.valueOf(coord.getLat()))
                    .lon(BigDecimal.valueOf(coord.getLon()))
                    .build();
            locationRepository.save(location);
        }

        List<Condition> conditions = conditionRepository.findAllById(request.getConditionIds());
        if (conditions.size() != request.getConditionIds().size()) {
            throw new CustomException(ErrorCode.CONDITION_NOT_FOUND);
        }
        for (Condition condition : conditions) {
            HealthCondition hc = HealthCondition.builder()
                    .member(member)
                    .condition(condition)
                    .build();
            healthConditionRepository.save(hc);
        }

        return new SignUpResponse(member.getMemberId());
    }

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
