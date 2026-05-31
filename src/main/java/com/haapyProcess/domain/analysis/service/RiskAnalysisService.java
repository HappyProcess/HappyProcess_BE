package com.haapyProcess.domain.analysis.service;

import com.haapyProcess.domain.analysis.dto.RiskAnalysisResult;
import com.haapyProcess.domain.analysis.rule.DiseaseRiskRule;
import com.haapyProcess.domain.healthcondition.entity.HealthCondition;
import com.haapyProcess.domain.healthcondition.service.HealthConditionService;
import com.haapyProcess.domain.location.entity.LocationType;
import com.haapyProcess.domain.location.service.LocationService;
import com.haapyProcess.domain.member.entity.Member;
import com.haapyProcess.domain.weather.dto.WeatherResponseDto;
import com.haapyProcess.domain.weather.service.WeatherService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class RiskAnalysisService {

    private final Map<Long, DiseaseRiskRule> ruleMap;
    private final WeatherService weatherService;
    private final HealthConditionService healthConditionService;
    private final LocationService locationService;

    public RiskAnalysisService(List<DiseaseRiskRule> rules,
                               WeatherService weatherService,
                               HealthConditionService healthConditionService,
                               LocationService locationService) {
        this.ruleMap = rules.stream()
                .collect(Collectors.toMap(DiseaseRiskRule::getConditionId, rule -> rule));
        this.weatherService = weatherService;
        this.healthConditionService = healthConditionService;
        this.locationService = locationService;
        log.info("위험도 판별기(Rule) {}개 로드 완료", ruleMap.size());
    }

    public RiskAnalysisResult analyzeRiskForMember(Member member) {
        String targetAreaNo = locationService.getMainAreaNo(member);

        WeatherResponseDto liveWeather = weatherService.getCombinedWeatherData(targetAreaNo);
        logWeatherSnapshot(member, null, targetAreaNo, liveWeather);

        List<HealthCondition> userConditions = healthConditionService.findAllByMember(member);

        RiskAnalysisResult result = analyzeRisk(userConditions, liveWeather);
        result.setRegionName(liveWeather.getRegionName());
        return result;
    }

    public RiskAnalysisResult analyzeRiskForMemberAt(Member member, LocationType locationType) {
        String targetAreaNo = locationService.getAreaNoByType(member, locationType);

        WeatherResponseDto liveWeather = weatherService.getCombinedWeatherData(targetAreaNo);
        logWeatherSnapshot(member, locationType, targetAreaNo, liveWeather);

        List<HealthCondition> userConditions = healthConditionService.findAllByMember(member);

        RiskAnalysisResult result = analyzeRisk(userConditions, liveWeather);
        result.setRegionName(liveWeather.getRegionName());
        return result;
    }

    private void logWeatherSnapshot(Member member, LocationType locationType, String areaNo, WeatherResponseDto w) {
        log.info("[위험도분석] memberId={} locationType={} areaNo={} region={} pm10={} pm25={} pollen={} uv={} temp={} humidity={} condition={}",
                member.getMemberId(),
                locationType,
                areaNo,
                w.getRegionName(),
                w.getParsedPm10Value(),
                w.getParsedPm25Value(),
                w.getParsedPollenRisk(),
                w.getParsedUvRisk(),
                w.getParsedCurrentTemp(),
                w.getParsedHumidity(),
                w.getWeatherCondition());
    }

    private RiskAnalysisResult analyzeRisk(List<HealthCondition> conditions, WeatherResponseDto weather) {
        if (conditions == null || conditions.isEmpty()) {
            DiseaseRiskRule normalRule = ruleMap.get(0L);
            if (normalRule != null) {
                List<RiskAnalysisResult.FactorGuide> factors = normalRule.evaluateFactorGuides(weather);

                if (factors != null && !factors.isEmpty()) {
                    RiskAnalysisResult.RiskDetail detail = new RiskAnalysisResult.RiskDetail(
                            normalRule.getConditionId(),
                            normalRule.getDiseaseName(),
                            factors
                    );
                    return new RiskAnalysisResult(true, List.of(detail));
                }
            }
            return new RiskAnalysisResult(false, null);
        }

        List<RiskAnalysisResult.RiskDetail> riskDetails = new ArrayList<>();

        for (HealthCondition healthCondition : conditions) {
            Long conditionId = healthCondition.getCondition().getConditionId();
            DiseaseRiskRule rule = ruleMap.get(conditionId);

            if (rule == null) continue;

            List<RiskAnalysisResult.FactorGuide> factors = rule.evaluateFactorGuides(weather);

            if (factors != null && !factors.isEmpty()) {
                riskDetails.add(new RiskAnalysisResult.RiskDetail(
                        rule.getConditionId(),
                        rule.getDiseaseName(),
                        factors
                ));
            }
        }

        if (!riskDetails.isEmpty()) {
            return new RiskAnalysisResult(true, riskDetails);
        }

        return new RiskAnalysisResult(false, null);
    }
}
