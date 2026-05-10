package com.haapyProcess.domain.location.controller;

import com.haapyProcess.domain.location.dto.AddLocationRequest;
import com.haapyProcess.domain.location.dto.LocationResponse;
import com.haapyProcess.domain.location.service.LocationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@Tag(name = "Member Location", description = "회원 위치 관련 API")
@RestController
@RequestMapping("/api/v1/members/me/locations")
@RequiredArgsConstructor
public class LocationController {

    private final LocationService locationService;

    @Operation(summary = "내 저장된 위치 목록 조회")
    @ApiResponse(responseCode = "200", description = "조회 성공")
    @GetMapping
    public ResponseEntity<List<LocationResponse>> getMyLocations() {
        return ResponseEntity.ok(locationService.getMyLocations());
    }

    @Operation(summary = "내 위치 추가 등록")
    @ApiResponse(responseCode = "201", description = "등록 성공")
    @PostMapping
    public ResponseEntity<Long> addLocation(@RequestBody @Valid AddLocationRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(locationService.addLocation(request));
    }

    @Operation(summary = "내 특정 위치 삭제")
    @ApiResponse(responseCode = "200", description = "삭제 성공")
    @DeleteMapping
    public ResponseEntity<Void> deleteLocation(@RequestParam Long locationId) {
        locationService.deleteLocation(locationId);
        return ResponseEntity.ok().build();
    }
}
