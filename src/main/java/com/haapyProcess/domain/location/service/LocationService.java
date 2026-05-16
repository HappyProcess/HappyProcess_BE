package com.haapyProcess.domain.location.service;

import com.haapyProcess.domain.location.dto.AddLocationRequest;
import com.haapyProcess.domain.location.dto.LocationResponse;
import com.haapyProcess.domain.location.entity.CityCoordinate;
import com.haapyProcess.domain.location.entity.Location;
import com.haapyProcess.domain.location.repository.LocationRepository;
import com.haapyProcess.domain.member.entity.Member;
import com.haapyProcess.domain.member.service.MemberService;
import com.haapyProcess.global.exception.CustomException;
import com.haapyProcess.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class LocationService {

    private final LocationRepository locationRepository;
    private final MemberService memberService;

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

        CityCoordinate coord;
        try {
            coord = CityCoordinate.valueOf(request.getCity());
        } catch (IllegalArgumentException e) {
            throw new CustomException(ErrorCode.INVALID_CITY);
        }

        Location location = Location.builder()
                .member(member)
                .locationType(request.getLocationType())
                .city(request.getCity())
                .lat(BigDecimal.valueOf(coord.getLat()))
                .lon(BigDecimal.valueOf(coord.getLon()))
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
}
