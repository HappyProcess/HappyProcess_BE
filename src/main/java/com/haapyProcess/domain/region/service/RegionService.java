package com.haapyProcess.domain.region.service;

import com.haapyProcess.domain.region.dto.RegionResponse;
import com.haapyProcess.domain.region.repository.RegionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RegionService {

    private final RegionRepository regionRepository;

    // 1단계: 시도 목록 (서울특별시, 부산광역시 등)
    public List<String> getSidoList() {
        return regionRepository.findDistinctSido();
    }

    // 2단계: 특정 시도의 시군구 목록 (강남구, 서초구 등)
    public List<String> getSigunguList(String sido) {
        return regionRepository.findDistinctSigunguBySido(sido);
    }

    // 3단계: 특정 시도+시군구의 동 목록과 코드 (역삼동-1168010300 등)
    public List<RegionResponse> getDongList(String sido, String sigungu) {
        return regionRepository.findDongList(sido, sigungu).stream()
                .map(region -> new RegionResponse(region.getAreaNo(), region.getDong()))
                .collect(Collectors.toList());
    }
}
