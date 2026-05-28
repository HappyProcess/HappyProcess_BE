package com.haapyProcess.domain.analysis.service;

import com.haapyProcess.domain.analysis.dto.RiskAnalysisResult;
import com.haapyProcess.domain.analysis.rule.DiseaseRiskRule;
import com.haapyProcess.domain.healthcondition.entity.HealthCondition;
import com.haapyProcess.domain.healthcondition.service.HealthConditionService;
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

        List<HealthCondition> userConditions = healthConditionService.findAllByMember(member);

        return analyzeRisk(userConditions, liveWeather);
    }

    private RiskAnalysisResult analyzeRisk(List<HealthCondition> conditions, WeatherResponseDto weather) {
        if (conditions == null || conditions.isEmpty()) {
            DiseaseRiskRule normalRule = ruleMap.get(0L);
            if (normalRule != null && normalRule.isAtRisk(weather)) {
                return new RiskAnalysisResult(true, List.of(normalRule.getDiseaseName()), List.of(normalRule.getConditionId()));
            }
            return new RiskAnalysisResult(false, null, null);
        }

        List<String> riskDiseaseNames = new ArrayList<>();
        List<Long> riskDiseaseIds = new ArrayList<>();

        for (HealthCondition healthCondition : conditions) {
            Long conditionId = healthCondition.getCondition().getConditionId();
            DiseaseRiskRule rule = ruleMap.get(conditionId);

            if (rule == null) continue;

            if (rule.isAtRisk(weather)) {
                riskDiseaseNames.add(rule.getDiseaseName());
                riskDiseaseIds.add(rule.getConditionId());
            }
        }

        if (!riskDiseaseNames.isEmpty()) {
            return new RiskAnalysisResult(true, riskDiseaseNames, riskDiseaseIds);
        }

        return new RiskAnalysisResult(false, null, null);
    }
}
