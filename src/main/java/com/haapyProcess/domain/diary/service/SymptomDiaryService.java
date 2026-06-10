package com.haapyProcess.domain.diary.service;

import com.haapyProcess.domain.condition.entity.Condition;
import com.haapyProcess.domain.condition.repository.ConditionRepository;
import com.haapyProcess.domain.diary.dto.DiaryRequest;
import com.haapyProcess.domain.diary.dto.DiaryResponse;
import com.haapyProcess.domain.diary.entity.DiaryWeather;
import com.haapyProcess.domain.diary.entity.SymptomDiary;
import com.haapyProcess.domain.diary.entity.SymptomDiaryItem;
import com.haapyProcess.domain.diary.repository.SymptomDiaryRepository;
import com.haapyProcess.domain.location.entity.Location;
import com.haapyProcess.domain.location.repository.LocationRepository;
import com.haapyProcess.domain.member.entity.Member;
import com.haapyProcess.domain.member.service.MemberService;
import com.haapyProcess.domain.weather.dto.WeatherResponseDto;
import com.haapyProcess.domain.weather.service.WeatherService;
import com.haapyProcess.global.exception.CustomException;
import com.haapyProcess.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SymptomDiaryService {

    private final SymptomDiaryRepository diaryRepository;
    private final ConditionRepository conditionRepository;
    private final LocationRepository locationRepository;
    private final WeatherService weatherService;
    private final MemberService memberService;

    /**
     * 증상 일기 작성/수정. entryDate 기준으로 upsert하며, 작성 시점 날씨 스냅샷을 함께 저장한다.
     */
    @Transactional
    public DiaryResponse upsertDiary(DiaryRequest request) {
        Member member = memberService.getCurrentMember();

        SymptomDiary diary = diaryRepository.findByMemberAndEntryDate(member, request.getEntryDate())
                .orElseGet(() -> SymptomDiary.builder()
                        .member(member)
                        .entryDate(request.getEntryDate())
                        .build());

        diary.updateMemo(request.getMemo());
        diary.replaceItems(buildItems(request));
        applyWeatherSnapshot(member, diary);

        SymptomDiary saved = diaryRepository.save(diary);
        return DiaryResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public DiaryResponse getDiary(LocalDate date) {
        Member member = memberService.getCurrentMember();
        SymptomDiary diary = diaryRepository.findByMemberAndEntryDate(member, date)
                .orElseThrow(() -> new CustomException(ErrorCode.DIARY_NOT_FOUND));
        return DiaryResponse.from(diary);
    }

    @Transactional(readOnly = true)
    public List<DiaryResponse> getDiaries(LocalDate from, LocalDate to) {
        Member member = memberService.getCurrentMember();
        return diaryRepository.findByMemberAndEntryDateBetween(member, from, to).stream()
                .map(DiaryResponse::from)
                .toList();
    }

    @Transactional
    public void deleteDiary(LocalDate date) {
        Member member = memberService.getCurrentMember();
        SymptomDiary diary = diaryRepository.findByMemberAndEntryDate(member, date)
                .orElseThrow(() -> new CustomException(ErrorCode.DIARY_NOT_FOUND));
        diaryRepository.delete(diary);
    }

    private List<SymptomDiaryItem> buildItems(DiaryRequest request) {
        return request.getSymptoms().stream()
                .map(s -> {
                    if (s.getIntensity() < 1 || s.getIntensity() > 5) {
                        throw new CustomException(ErrorCode.INVALID_SYMPTOM_INTENSITY);
                    }
                    Condition condition = conditionRepository.findById(s.getConditionId())
                            .orElseThrow(() -> new CustomException(ErrorCode.CONDITION_NOT_FOUND));
                    return SymptomDiaryItem.builder()
                            .condition(condition)
                            .intensity(s.getIntensity())
                            .build();
                })
                .toList();
    }

    /**
     * 사용자의 모든 위치(HOME/WORK)에 대해 날씨를 수집해 일기에 위치별 스냅샷으로 채운다.
     * 위치가 없거나 외부 API 실패 시 해당 위치는 건너뛰고 저장한다.
     */
    private void applyWeatherSnapshot(Member member, SymptomDiary diary) {
        List<Location> locations = locationRepository.findAllByMember(member);
        if (locations.isEmpty()) {
            log.info("날씨 스냅샷 보류: 등록된 위치가 없습니다. (memberId: {})", member.getMemberId());
            diary.replaceWeathers(List.of());
            return;
        }

        List<DiaryWeather> snapshots = new ArrayList<>();
        for (Location loc : locations) {
            if (loc.getRegion() == null) {
                continue;
            }
            try {
                WeatherResponseDto w = weatherService.getCombinedWeatherData(loc.getRegion().getAreaNo());
                snapshots.add(DiaryWeather.builder()
                        .locationType(loc.getLocationType())
                        .regionName(w.getRegionName())
                        .temperature(w.getTemperature())
                        .humidity(w.getHumidity())
                        .weatherCondition(w.getWeatherCondition())
                        .pm10Value(w.getPm10Value())
                        .pm10Grade(w.getPm10Grade())
                        .pm25Value(w.getPm25Value())
                        .pm25Grade(w.getPm25Grade())
                        .pollenRiskLevel(w.getPollenRiskLevel())
                        .uvRiskLevel(w.getUvRiskLevel())
                        .build());
            } catch (Exception e) {
                log.warn("날씨 스냅샷 수집 실패 (memberId: {}, locationType: {}): {}",
                        member.getMemberId(), loc.getLocationType(), e.getMessage());
            }
        }
        diary.replaceWeathers(snapshots);
    }
}
