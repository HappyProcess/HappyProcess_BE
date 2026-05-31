package com.haapyProcess.domain.family.service;

import com.haapyProcess.domain.alert.dto.AddAlertRequest;
import com.haapyProcess.domain.alert.dto.AlertResponse;
import com.haapyProcess.domain.alert.dto.UpdateAlertRequest;
import com.haapyProcess.domain.alert.entity.Alert;
import com.haapyProcess.domain.alert.repository.AlertRepository;
import com.haapyProcess.domain.analysis.dto.RiskAnalysisResult;
import com.haapyProcess.domain.analysis.service.RiskAnalysisService;
import com.haapyProcess.domain.condition.entity.Condition;
import com.haapyProcess.domain.condition.repository.ConditionRepository;
import com.haapyProcess.domain.family.dto.AddFamilyRequest;
import com.haapyProcess.domain.family.dto.FamilyListResponse;
import com.haapyProcess.domain.family.dto.FamilyMemberResponse;
import com.haapyProcess.domain.family.entity.Family;
import com.haapyProcess.domain.family.repository.FamilyRepository;
import com.haapyProcess.domain.healthcondition.dto.UpdateConditionsRequest;
import com.haapyProcess.domain.healthcondition.entity.HealthCondition;
import com.haapyProcess.domain.healthcondition.repository.HealthConditionRepository;
import com.haapyProcess.domain.location.dto.AddLocationRequest;
import com.haapyProcess.domain.location.dto.LocationResponse;
import com.haapyProcess.domain.location.entity.Location;
import com.haapyProcess.domain.location.repository.LocationRepository;
import com.haapyProcess.domain.member.entity.Member;
import com.haapyProcess.domain.member.repository.MemberRepository;
import com.haapyProcess.domain.member.service.MemberService;
import com.haapyProcess.domain.region.entity.Region;
import com.haapyProcess.domain.region.repository.RegionRepository;
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
    private final LocationRepository locationRepository;
    private final RegionRepository regionRepository;
    private final AlertRepository alertRepository;

    // 1. 가족 추가
    @Transactional
    public Long addFamily(AddFamilyRequest request) {
        Member me = memberService.getCurrentMember();

        Member relative = memberRepository.findByLoginId(request.getRelativeLoginId())
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        if (me.getMemberId().equals(relative.getMemberId())) {
            throw new IllegalArgumentException("자기 자신은 가족으로 추가할 수 없습니다.");
        }

        if (familyRepository.existsByUserAndRelative(me, relative)) {
            throw new IllegalArgumentException("이미 등록된 가족입니다.");
        }

        Family family = Family.builder()
                .user(me)
                .relative(relative)
                .build(); // isAlertEnabled는 엔티티 기본값 true

        return familyRepository.save(family).getFamilyId();
    }

    // 2-1. 가족 목록 조회 (가벼운 버전: 이름 + 질병 + 알림 시간만)
    @Transactional(readOnly = true)
    public List<FamilyListResponse> getMyFamilies() {
        Member me = memberService.getCurrentMember();
        List<Family> families = familyRepository.findAllByUser(me);

        return families.stream().map(family -> {
            Member relative = family.getRelative();

            List<String> conditionNames = relative.getHealthConditions().stream()
                    .map(hc -> hc.getCondition().getConditionName())
                    .toList();

            List<String> alertTimes = alertRepository.findAllByMemberOrderByAlertTimeAsc(relative).stream()
                    .map(Alert::getAlertTime)
                    .toList();

            return FamilyListResponse.builder()
                    .familyId(family.getFamilyId())
                    .name(relative.getName())
                    .healthConditionNames(conditionNames)
                    .alertTimes(alertTimes)
                    .build();
        }).toList();
    }

    // 2-2. 가족 상세 조회 (위험도 + 지역 + 알림 시간 전부 포함)
    @Transactional(readOnly = true)
    public FamilyMemberResponse getFamilyDetail(Long familyId) {
        Member me = memberService.getCurrentMember();
        Family family = familyRepository.findByFamilyIdAndUser(familyId, me)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));
        Member relative = family.getRelative();

        int age = relative.getBirth() != null
                ? Period.between(relative.getBirth(), LocalDate.now()).getYears()
                : 0;

        List<String> conditionNames = relative.getHealthConditions().stream()
                .map(hc -> hc.getCondition().getConditionName())
                .toList();

        // 날씨 위험도 분석 (가족이 위치 미등록이거나 기상청 에러 시 방어)
        RiskAnalysisResult riskResult;
        try {
            riskResult = riskAnalysisService.analyzeRiskForMember(relative);
        } catch (Exception e) {
            riskResult = new RiskAnalysisResult(false, null);
        }

        List<String> causeDiseaseNames = riskResult.getRiskDetails() == null ? List.of()
                : riskResult.getRiskDetails().stream()
                    .map(RiskAnalysisResult.RiskDetail::getDiseaseName)
                    .toList();

        List<LocationResponse> locations = locationRepository.findAllByMember(relative).stream()
                .map(LocationResponse::from)
                .toList();

        List<AlertResponse> alerts = alertRepository.findAllByMemberOrderByAlertTimeAsc(relative).stream()
                .map(AlertResponse::from)
                .toList();

        return FamilyMemberResponse.builder()
                .familyId(family.getFamilyId())
                .relativeId(relative.getMemberId())
                .name(relative.getName())
                .age(age)
                .isAlertEnabled(family.isAlertEnabled())
                .healthConditionNames(conditionNames)
                .isRisk(riskResult.isRisk())
                .causeDiseaseNames(causeDiseaseNames)
                .locations(locations)
                .alerts(alerts)
                .build();
    }

    // 3. 가족 건강 상태(질환) 수정 (덮어쓰기)
    @Transactional
    public void updateFamilyConditions(Long familyId, UpdateConditionsRequest request) {
        Member relative = getRelativeWithOwnershipCheck(familyId);

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

    // 4. 가족 알림 켜기/끄기 (Family.isAlertEnabled 스위치)
    @Transactional
    public void toggleFamilyAlert(Long familyId, boolean isAlertEnabled) {
        Member me = memberService.getCurrentMember();
        Family family = familyRepository.findByFamilyIdAndUser(familyId, me)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));
        family.toggleAlert(isAlertEnabled);
    }

    // 5. 가족 지역(위치) 등록/수정 — 같은 타입(HOME/WORK)이 있으면 덮어쓰고 없으면 새로 추가
    @Transactional
    public LocationResponse upsertFamilyLocation(Long familyId, AddLocationRequest request) {
        Member relative = getRelativeWithOwnershipCheck(familyId);

        Region region = regionRepository.findById(request.areaNo())
                .orElseThrow(() -> new CustomException(ErrorCode.INVALID_CITY));

        Location existing = locationRepository.findAllByMember(relative).stream()
                .filter(loc -> loc.getLocationType() == request.locationType())
                .findFirst()
                .orElse(null);

        if (existing != null) {
            existing.updateRegion(region);
            return LocationResponse.from(existing);
        }

        Location location = Location.builder()
                .member(relative)
                .locationType(request.locationType())
                .region(region)
                .build();
        return LocationResponse.from(locationRepository.save(location));
    }

    // 6. 가족 알림 시간 추가
    @Transactional
    public AlertResponse addFamilyAlert(Long familyId, AddAlertRequest request) {
        Member relative = getRelativeWithOwnershipCheck(familyId);

        if (alertRepository.existsByMemberAndAlertTimeAndLocationType(relative, request.alertTime(), request.locationType())) {
            throw new CustomException(ErrorCode.DUPLICATE_ALERT_TIME);
        }

        Alert alert = Alert.builder()
                .member(relative)
                .alertTime(request.alertTime())
                .isEnable(true)
                .locationType(request.locationType())
                .build();
        return AlertResponse.from(alertRepository.save(alert));
    }

    // 7. 가족 알림 시간 수정
    @Transactional
    public AlertResponse updateFamilyAlert(Long familyId, Long alertId, UpdateAlertRequest request) {
        Member relative = getRelativeWithOwnershipCheck(familyId);

        Alert alert = alertRepository.findById(alertId)
                .orElseThrow(() -> new CustomException(ErrorCode.ALERT_NOT_FOUND));
        if (!alert.getMember().getMemberId().equals(relative.getMemberId())) {
            throw new CustomException(ErrorCode.UNAUTHORIZED_USER);
        }

        boolean changed = !alert.getAlertTime().equals(request.alertTime())
                || alert.getEffectiveLocationType() != request.locationType();
        if (changed && alertRepository.existsByMemberAndAlertTimeAndLocationType(relative, request.alertTime(), request.locationType())) {
            throw new CustomException(ErrorCode.DUPLICATE_ALERT_TIME);
        }

        alert.updateAlertTime(request.alertTime());
        alert.updateLocationType(request.locationType());
        return AlertResponse.from(alert);
    }

    // 8. 가족 알림 시간 삭제
    @Transactional
    public void deleteFamilyAlert(Long familyId, Long alertId) {
        Member relative = getRelativeWithOwnershipCheck(familyId);

        Alert alert = alertRepository.findById(alertId)
                .orElseThrow(() -> new CustomException(ErrorCode.ALERT_NOT_FOUND));
        if (!alert.getMember().getMemberId().equals(relative.getMemberId())) {
            throw new CustomException(ErrorCode.UNAUTHORIZED_USER);
        }
        alertRepository.delete(alert);
    }

    // 내 가족인지 소유권 검증 후 relative(가족 본인) 반환
    private Member getRelativeWithOwnershipCheck(Long familyId) {
        Member me = memberService.getCurrentMember();
        Family family = familyRepository.findByFamilyIdAndUser(familyId, me)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));
        return family.getRelative();
    }
}
