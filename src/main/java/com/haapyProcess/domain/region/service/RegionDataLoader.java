// src/main/java/com/haapyProcess/domain/region/service/RegionDataLoader.java
package com.haapyProcess.domain.region.service;

import com.haapyProcess.domain.region.entity.Region;
import com.haapyProcess.domain.region.repository.RegionRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class RegionDataLoader {

    private final RegionRepository regionRepository;

    @PostConstruct
    public void init() {
        // 이미 데이터가 존재하면 로딩 생략 (서버 재시작 시 중복 방지)
        if (regionRepository.count() > 0) {
            log.info("✅ 지역 데이터가 이미 존재합니다. (총 {}건)", regionRepository.count());
            return;
        }

        try {
            ClassPathResource resource = new ClassPathResource("regions.csv");
            // 한글 깨짐을 방지하기 위해 UTF-8 혹은 EUC-KR(윈도우 엑셀 기본값)로 인코딩을 맞춥니다.
            // 만약 글자가 깨진다면 StandardCharsets.UTF_8 대신 "EUC-KR" 문자열을 사용하세요.
            BufferedReader br = new BufferedReader(new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8));

            String line;
            boolean isFirstLine = true;
            List<Region> regionList = new ArrayList<>();

            while ((line = br.readLine()) != null) {
                if (isFirstLine) {
                    isFirstLine = false; // 첫 줄(헤더) 건너뛰기
                    continue;
                }

                // 💥 핵심: split(",", -1)을 써야 뒤쪽의 stationName 같은 빈칸 열이 무시되지 않고 파싱됩니다.
                String[] data = line.split(",", -1);

                // 데이터 길이가 비정상적이면 건너뜀 (안전 장치)
                if (data.length < 8) continue;

                Region region = Region.builder()
                        .areaNo(data[0].trim())
                        .sido(data[1].trim())
                        .sigungu(data[2].trim().isEmpty() ? null : data[2].trim())
                        .dong(data[3].trim().isEmpty() ? null : data[3].trim())
                        .nx(data[4].trim())
                        .ny(data[5].trim())
                        .sidoName(data[6].trim())
                        .stationName(data[7].trim().isEmpty() ? null : data[7].trim())
                        .build();

                regionList.add(region);
            }

            // 성능을 위해 한 번에 통째로 저장 (Batch Insert)
            regionRepository.saveAll(regionList);
            log.info("✅ 지역 데이터 CSV 로딩 완료! 총 {}건", regionList.size());

        } catch (Exception e) {
            log.error("❌ 지역 데이터 로딩 중 에러 발생: ", e);
        }
    }
}