package com.haapyProcess.domain.family.service;

import com.haapyProcess.domain.analysis.dto.RiskAnalysisResult;
import com.haapyProcess.domain.analysis.service.RiskAnalysisService;
import com.haapyProcess.domain.condition.entity.Condition;
import com.haapyProcess.domain.condition.repository.ConditionRepository;
import com.haapyProcess.domain.family.dto.AddFamilyRequest;
import com.haapyProcess.domain.family.dto.FamilyMemberResponse;
import com.haapyProcess.domain.family.entity.Family;
import com.haapyProcess.domain.family.repository.FamilyRepository;
import com.haapyProcess.domain.healthcondition.dto.UpdateConditionsRequest;
import com.haapyProcess.domain.healthcondition.entity.HealthCondition;
import com.haapyProcess.domain.healthcondition.repository.HealthConditionRepository;
import com.haapyProcess.domain.member.entity.Member;
import com.haapyProcess.domain.member.repository.MemberRepository;
import com.haapyProcess.domain.member.service.MemberService;
import com.haapyProcess.global.exception.CustomException;
import com.haapyProcess.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.Period;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FamilyService {

    private final FamilyRepository familyRepository;
    private final MemberRepository memberRepository;
    private final MemberService memberService; // 현재 로그인한 내 정보 가져오기용
    private final RiskAnalysisService riskAnalysisService;
    private final HealthConditionRepository healthConditionRepository;
    private final ConditionRepository conditionRepository;

    // 1. 가족 추가 로직
    @Transactional
    public Long addFamily(AddFamilyRequest request) {
        // 1) 나는 누구인가?
        Member me = memberService.getCurrentMember();

        // 2) 추가하려는 가족이 우리 회원(DB)에 존재하는가?
        Member relative = memberRepository.findByLoginId(request.getRelativeLoginId())
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        // 3) 나 자신을 가족으로 추가하려고 하면 컷!
        if (me.getMemberId().equals(relative.getMemberId())) {
            throw new IllegalArgumentException("자기 자신은 가족으로 추가할 수 없습니다.");
        }

        // 4) 이미 등록된 가족이면 컷!
        if (familyRepository.existsByUserAndRelative(me, relative)) {
            throw new IllegalArgumentException("이미 등록된 가족입니다.");
        }

        // 5) 문제없으면 가족 족보(Entity) 생성해서 DB에 저장
        Family family = Family.builder()
                .user(me)
                .relative(relative)
                .build(); // isAlertEnabled는 엔티티에서 기본값 true로 설정됨

        return familyRepository.save(family).getFamilyId();
    }

    // 2. 가족 목록 조회 (위험도 분석 포함)
    @Transactional(readOnly = true)
    public List<FamilyMemberResponse> getMyFamilies() {
        Member me = memberService.getCurrentMember();
        List<Family> families = familyRepository.findAllByUser(me);

        return families.stream().map(family -> {
            Member relative = family.getRelative();
            
            // 나이 계산
            int age = Period.between(relative.getBirth(), LocalDate.now()).getYears();

            // 가족의 질병 이름 리스트
            List<String> conditionNames = relative.getHealthConditions().stream()
                    .map(hc -> hc.getCondition().getConditionName())
                    .toList();

            // 날씨 위험도 분석
            RiskAnalysisResult riskResult;
            try {
                riskResult = riskAnalysisService.analyzeRiskForMember(relative);
            } catch (Exception e) {
                // 가족이 위치(Location)를 아직 등록 안 했거나 기상청 서버 에러 시 방어
                riskResult = new RiskAnalysisResult(false, null, null);
            }

            return FamilyMemberResponse.builder()
                    .familyId(family.getFamilyId())
                    .relativeId(relative.getMemberId())
                    .name(relative.getName())
                    .age(age)
                    .isAlertEnabled(family.isAlertEnabled())
                    .healthConditionNames(conditionNames)
                    .isRisk(riskResult.isRisk())
                    .causeDiseaseNames(riskResult.getCauseDiseaseNames())
                    .build();
        }).toList();
    }

    // 3. 가족 구성원의 건강 상태(질환) 수정
    @Transactional
    public void updateFamilyConditions(Long familyId, UpdateConditionsRequest request) {
        Member me = memberService.getCurrentMember();

        Family family = familyRepository.findByFamilyIdAndUser(familyId, me)
                .orElseThrow(() -> new IllegalArgumentException("접근 권한이 없거나 존재하지 않는 가족입니다."));

        Member relative = family.getRelative();

        // 기존 건강 상태 삭제 후 덮어쓰기
        healthConditionRepository.deleteAllByMember(relative);

        List<Condition> conditions = conditionRepository.findAllById(request.getConditionIds());
        if (conditions.size() != request.getConditionIds().size()) {
            throw new CustomException(ErrorCode.CONDITION_NOT_FOUND);
        }

        List<HealthCondition> newConditions = conditions.stream()
                .map(condition -> HealthCondition.builder()
                        .member(relative)
                        .condition(condition)
                        .build())
                .toList();
        
        healthConditionRepository.saveAll(newConditions);
    }

    // 4. 가족 알림 켜기/끄기 (Toggle)
    @Transactional
    public void toggleFamilyAlert(Long familyId, boolean isAlertEnabled) {
        Member me = memberService.getCurrentMember();

        Family family = familyRepository.findByFamilyIdAndUser(familyId, me)
                .orElseThrow(() -> new IllegalArgumentException("접근 권한이 없거나 존재하지 않는 가족입니다."));

        family.toggleAlert(isAlertEnabled);
    }
}