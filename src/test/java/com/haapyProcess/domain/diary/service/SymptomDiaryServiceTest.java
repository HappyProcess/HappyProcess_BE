package com.haapyProcess.domain.diary.service;

import com.haapyProcess.domain.condition.entity.Condition;
import com.haapyProcess.domain.condition.repository.ConditionRepository;
import com.haapyProcess.domain.diary.dto.DiaryRequest;
import com.haapyProcess.domain.diary.dto.DiaryResponse;
import com.haapyProcess.domain.diary.entity.SymptomDiary;
import com.haapyProcess.domain.diary.repository.SymptomDiaryRepository;
import com.haapyProcess.domain.location.entity.Location;
import com.haapyProcess.domain.location.entity.LocationType;
import com.haapyProcess.domain.location.repository.LocationRepository;
import com.haapyProcess.domain.member.entity.Member;
import com.haapyProcess.domain.member.service.MemberService;
import com.haapyProcess.domain.region.entity.Region;
import com.haapyProcess.domain.weather.dto.WeatherResponseDto;
import com.haapyProcess.domain.weather.service.WeatherService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SymptomDiaryServiceTest {

    @Mock SymptomDiaryRepository diaryRepository;
    @Mock ConditionRepository conditionRepository;
    @Mock LocationRepository locationRepository;
    @Mock WeatherService weatherService;
    @Mock MemberService memberService;

    @InjectMocks SymptomDiaryService service;

    private final Member member = Member.builder().build();

    private DiaryRequest request(LocalDate date, String memo, Long conditionId, int intensity) {
        DiaryRequest req = new DiaryRequest();
        ReflectionTestUtils.setField(req, "entryDate", date);
        ReflectionTestUtils.setField(req, "memo", memo);
        DiaryRequest.SymptomItem item = new DiaryRequest.SymptomItem();
        ReflectionTestUtils.setField(item, "conditionId", conditionId);
        ReflectionTestUtils.setField(item, "intensity", intensity);
        ReflectionTestUtils.setField(req, "symptoms", List.of(item));
        return req;
    }

    private Condition condition(Long id, String name) {
        Condition c = mock(Condition.class);
        lenient().when(c.getConditionId()).thenReturn(id);
        lenient().when(c.getConditionName()).thenReturn(name);
        return c;
    }

    @Test
    @DisplayName("같은 날 재작성 시 기존 일기를 갱신(upsert)하고 날씨 스냅샷을 채운다")
    void upsertExistingDiaryWithWeather() {
        LocalDate date = LocalDate.of(2025, 6, 2);
        when(memberService.getCurrentMember()).thenReturn(member);

        SymptomDiary existing = SymptomDiary.builder().member(member).entryDate(date).memo("이전 메모").build();
        when(diaryRepository.findByMemberAndEntryDate(member, date)).thenReturn(Optional.of(existing));
        Condition asthma = condition(1L, "천식");
        when(conditionRepository.findById(1L)).thenReturn(Optional.of(asthma));

        Region region = Region.builder().areaNo("1168010100").dong("역삼동").build();
        Location home = Location.builder().member(member).locationType(LocationType.HOME).region(region).build();
        when(locationRepository.findAllByMember(member)).thenReturn(List.of(home));
        when(weatherService.getCombinedWeatherData("1168010100")).thenReturn(
                WeatherResponseDto.builder().regionName("역삼동").temperature("25").pm10Value("80").build());
        when(diaryRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        DiaryResponse res = service.upsertDiary(request(date, "새 메모", 1L, 3));

        assertThat(res.getMemo()).isEqualTo("새 메모");
        assertThat(res.getSymptoms()).hasSize(1);
        assertThat(res.getSymptoms().get(0).getConditionName()).isEqualTo("천식");
        assertThat(res.getSymptoms().get(0).getIntensity()).isEqualTo(3);
        assertThat(res.getWeather().getRegionName()).isEqualTo("역삼동");
        assertThat(res.getWeather().getPm10Value()).isEqualTo("80");
        verify(weatherService).getCombinedWeatherData("1168010100");
    }

    @Test
    @DisplayName("등록된 위치가 없으면 날씨 없이 저장한다")
    void savesWithoutWeatherWhenNoLocation() {
        LocalDate date = LocalDate.of(2025, 6, 2);
        when(memberService.getCurrentMember()).thenReturn(member);
        when(diaryRepository.findByMemberAndEntryDate(member, date)).thenReturn(Optional.empty());
        Condition rhinitis = condition(1L, "비염");
        when(conditionRepository.findById(1L)).thenReturn(Optional.of(rhinitis));
        when(locationRepository.findAllByMember(member)).thenReturn(List.of());
        when(diaryRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        DiaryResponse res = service.upsertDiary(request(date, null, 1L, 2));

        assertThat(res.getWeather().getRegionName()).isNull();
        verify(weatherService, never()).getCombinedWeatherData(any());
    }
}
