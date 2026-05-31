package com.haapyProcess.domain.location.service;

import com.haapyProcess.domain.location.dto.AddLocationRequest;
import com.haapyProcess.domain.location.dto.LocationResponse;
import com.haapyProcess.domain.region.entity.Region;
import com.haapyProcess.domain.region.repository.RegionRepository;
import com.haapyProcess.domain.location.entity.Location;
import com.haapyProcess.domain.location.entity.LocationType;
import com.haapyProcess.domain.location.repository.LocationRepository;
import com.haapyProcess.domain.member.entity.Member;
import com.haapyProcess.domain.member.service.MemberService;
import com.haapyProcess.global.exception.CustomException;
import com.haapyProcess.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
public class LocationService {

    private final LocationRepository locationRepository;
    private final MemberService memberService;
    private final RegionRepository regionRepository;

    @Transactional(readOnly = true)
    public List<LocationResponse> getMyLocations() {
        Member member = memberService.getCurrentMember();
        return locationRepository.findAllByMember(member).stream()
                .map(LocationResponse::from)
                .toList();
    }

    @Transactional
    public Long addLocation(AddLocationRequest request) {
        Member member = memberService.getCurrentMember();

        Region region = regionRepository.findById(request.areaNo())
                .orElseThrow(() -> new CustomException(ErrorCode.INVALID_CITY));

        Location location = Location.builder()
                .member(member)
                .locationType(request.locationType())
                .region(region)
                .build();

        return locationRepository.save(location).getLocationId();
    }

    @Transactional
    public void deleteLocation(Long locationId) {
        Member member = memberService.getCurrentMember();
        Location location = locationRepository.findByLocationIdAndMember(locationId, member)
                .orElseThrow(() -> new CustomException(ErrorCode.LOCATION_NOT_FOUND));
        locationRepository.delete(location);
    }

    /**
     * 메인 화면 날씨 조회를 위한 대표 지역 코드 추출 (집 -> 직장 순)
     */
    @Transactional(readOnly = true)
    public String getMainAreaNo(Member member) {
        List<Location> locations = locationRepository.findAllByMember(member);

        if (locations.isEmpty()) {
            throw new CustomException(ErrorCode.LOCATION_NOT_FOUND);
        }

        String homeAreaNo = locations.stream()
                .filter(loc -> "HOME".equals(loc.getLocationType().name()))
                .map(loc -> loc.getRegion().getAreaNo())
                .findFirst()
                .orElse(null);

        if (homeAreaNo != null) {
            return homeAreaNo;
        }

        return locations.stream()
                .filter(loc -> "WORK".equals(loc.getLocationType().name()))
                .map(loc -> loc.getRegion().getAreaNo())
                .findFirst()
                .orElseGet(() -> locations.get(0).getRegion().getAreaNo());
    }

    /**
     * 특정 위치 타입(HOME/WORK)의 areaNo를 조회. 해당 타입이 없으면 getMainAreaNo로 폴백.
     */
    @Transactional(readOnly = true)
    public String getAreaNoByType(Member member, LocationType locationType) {
        return locationRepository.findAllByMember(member).stream()
                .filter(loc -> loc.getLocationType() == locationType)
                .map(loc -> loc.getRegion().getAreaNo())
                .findFirst()
                .orElseGet(() -> getMainAreaNo(member));
    }
}
