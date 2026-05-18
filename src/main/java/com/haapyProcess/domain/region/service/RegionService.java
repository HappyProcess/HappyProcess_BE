package com.haapyProcess.domain.region.service;

import com.haapyProcess.domain.region.dto.RegionResponse;
import com.haapyProcess.domain.region.repository.RegionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RegionService {

    private final RegionRepository regionRepository;

    public List<String> getSidoList() {
        return regionRepository.findDistinctSido();
    }

    public List<String> getSigunguList(String sido) {
        return regionRepository.findDistinctSigunguBySido(sido);
    }

    public List<RegionResponse> getDongList(String sido, String sigungu) {
        return regionRepository.findDongList(sido, sigungu);
    }
}