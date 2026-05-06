package com.haapyProcess.domain.member.service;

import com.haapyProcess.domain.condition.entity.Condition;
import com.haapyProcess.domain.condition.repository.ConditionRepository;
import com.haapyProcess.domain.healthcondition.entity.HealthCondition;
import com.haapyProcess.domain.healthcondition.repository.HealthConditionRepository;
import com.haapyProcess.domain.location.entity.CityCoordinate;
import com.haapyProcess.domain.location.entity.Location;
import com.haapyProcess.domain.location.entity.LocationType;
import com.haapyProcess.domain.location.repository.LocationRepository;
import com.haapyProcess.domain.member.dto.SignUpRequest;
import com.haapyProcess.domain.member.dto.SignUpResponse;
import com.haapyProcess.domain.member.entity.Member;
import com.haapyProcess.domain.member.repository.MemberRepository;
import com.haapyProcess.global.exception.CustomException;
import com.haapyProcess.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;
    private final ConditionRepository conditionRepository;
    private final LocationRepository locationRepository;
    private final HealthConditionRepository healthConditionRepository;
    private final PasswordEncoder passwordEncoder;

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

        if (request.getConditionIds() != null && !request.getConditionIds().isEmpty()) {
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
        }

        return new SignUpResponse(member.getMemberId());
    }
}
