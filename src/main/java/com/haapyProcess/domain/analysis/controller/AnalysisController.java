package com.haapyProcess.domain.analysis.controller;

import com.haapyProcess.domain.analysis.dto.RiskAnalysisResult;
import com.haapyProcess.domain.analysis.service.RiskAnalysisService;
import com.haapyProcess.domain.location.entity.LocationType;
import com.haapyProcess.domain.member.entity.Member;
import com.haapyProcess.domain.member.service.MemberService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Analysis", description = "질병 위험도 분석 API")
@RestController
@RequestMapping("/api/v1/analysis")
@RequiredArgsConstructor
public class AnalysisController {

    private final RiskAnalysisService riskAnalysisService;
    private final MemberService memberService;

    @Operation(
            summary = "실시간 질병 위험도 분석",
            description = """
                현재 로그인한 유저의 **기본 위치(집 우선, 없으면 직장)**를 기준으로 기상청/에어코리아 실시간 날씨 데이터를 조회한 뒤,
                유저가 등록한 **질환 목록**과 매칭하여 현재 날씨가 위험한지 분석 결과를 반환합니다. 메인 홈 화면 진입 시 호출합니다.
                
                * **동작 흐름**
                  1. 헤더의 토큰을 통해 유저 식별 및 대표 지역 코드 추출
                  2. 외부 API를 통해 해당 지역의 실시간 통합 날씨 조회
                  3. 유저가 보유한 질병별 판별기(Rule)를 가동하여 위험 여부 연산
                  4. 위험에 해당하는 질병이 1개라도 있으면 `isRisk: true` 와 함께 상세 원인과 행동 강령 반환
                
                ---
                
                ## **📋 Response Fields**
                | **키** | **설명** | **타입** | **비고** |
                |---|---|---|---|
                | **isRisk** | 현재 날씨가 유저의 질병에 위험한지 여부 | boolean | 안전하면 false |
                | **riskDetails** | 위험 기준을 초과한 질병별 상세 정보 배열 | List<Object> | isRisk가 false면 오지 않음 |
                | └ diseaseId | 원인 질병 ID | Long | |
                | └ diseaseName | 원인 질병 이름 | String | |
                | └ weatherScore | 질환별 날씨 점수 (0=최악, 100=최고) | int | 위험 요인이 없어도 점수는 제공 |
                | └ scoreFactors | 점수를 크게 깎은 주요 날씨 지수 이름 배열 (예: 초미세먼지) | List<String> | 영향 큰 순, 최대 2개. 양호하면 빈 배열 |
                | └ factorGuides | 날씨 원인 및 맞춤 행동 강령 배열 | List<Object> | 위험 요인이 없으면 빈 배열 |
                | &nbsp;&nbsp;&nbsp;└ factorName | 초과한 날씨 요인 (예: 미세먼지) | String | |
                | &nbsp;&nbsp;&nbsp;└ guide | 행동 추천 가이드 | String | |
                """
    )
    @ApiResponse(responseCode = "200", description = "위험도 분석 성공")
    @ApiResponse(responseCode = "400", description = "유저에게 등록된 위치(지역) 정보가 없어 날씨를 조회할 수 없음")
    @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자 (토큰 만료 또는 없음)")
    @ApiResponse(responseCode = "500", description = "외부 공공데이터 API 연동 중 서버 장애 발생")
    @GetMapping("/risk-status")
    public ResponseEntity<RiskAnalysisResult> getLiveRiskStatus(
            @Parameter(description = "분석 기준 위치 (HOME: 집, WORK: 직장/학교). 생략 시 집 우선 폴백")
            @RequestParam(required = false) LocationType locationType) {
        Member currentMember = memberService.getCurrentMember();

        RiskAnalysisResult result = (locationType != null)
                ? riskAnalysisService.analyzeRiskForMemberAt(currentMember, locationType)
                : riskAnalysisService.analyzeRiskForMember(currentMember);

        return ResponseEntity.ok(result);
    }
}
