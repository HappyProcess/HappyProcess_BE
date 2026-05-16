package com.haapyProcess.domain.healthcondition.service;

import com.haapyProcess.domain.condition.entity.Condition;
import com.haapyProcess.domain.condition.repository.ConditionRepository;
import com.haapyProcess.domain.healthcondition.dto.ConditionResponse;
import com.haapyProcess.domain.healthcondition.dto.MyConditionsResponse;
import com.haapyProcess.domain.healthcondition.dto.UpdateConditionsRequest;
import com.haapyProcess.domain.healthcondition.entity.HealthCondition;
import com.haapyProcess.domain.healthcondition.repository.HealthConditionRepository;
import com.haapyProcess.domain.member.entity.Member;
import com.haapyProcess.domain.member.repository.MemberRepository;
import com.haapyProcess.global.exception.CustomException;
import com.haapyProcess.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class HealthConditionService {

    private final MemberRepository memberRepository;
    private final ConditionRepository conditionRepository;
    private final HealthConditionRepository healthConditionRepository;

    // 전체 건강 상태 목록 조회
    @Transactional(readOnly = true)
    public List<ConditionResponse> getAllConditions() {
        return conditionRepository.findAll()
                .stream()
                .map(ConditionResponse::from)
                .toList();
    }

    // 내 건강 상태 조회
    @Transactional(readOnly = true)
    public MyConditionsResponse getMyConditions() {
        Member member = getCurrentMember();
        List<ConditionResponse> myConditions = healthConditionRepository.findAllByMember(member)
                .stream()
                .map(hc -> ConditionResponse.from(hc.getCondition()))
                .toList();
        return new MyConditionsResponse(myConditions);
    }

    // 내 건강 상태 일괄 수정
    @Transactional
    public void updateMyConditions(UpdateConditionsRequest request) {
        Member member = getCurrentMember();

        // 1. 기존 건강 상태 전체 삭제 (형님 지시사항 반영)
        healthConditionRepository.deleteAllByMember(member);

        // 2. 새로운 건강 상태 조회
        List<Condition> conditions = conditionRepository.findAllById(request.getConditionIds());
        if (conditions.size() != request.getConditionIds().size()) {
            throw new CustomException(ErrorCode.CONDITION_NOT_FOUND);
        }

        // 3. 하나씩 save 하지 않고, 리스트로 만들어서 saveAll로 한 번에 저장 (형님 지시사항 반영!)
        List<HealthCondition> newHealthConditions = conditions.stream()
                .map(condition -> HealthCondition.builder()
                        .member(member)
                        .condition(condition)
                        .build())
                .toList();
        
        healthConditionRepository.saveAll(newHealthConditions);
    }

    private Member getCurrentMember() {
        String loginId = SecurityContextHolder.getContext().getAuthentication().getName();
        return memberRepository.findByLoginId(loginId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));
    }
}
