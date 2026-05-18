package com.haapyProcess.domain.region.service;

import com.haapyProcess.domain.region.entity.Region;
import com.haapyProcess.domain.region.repository.RegionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
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
public class RegionDataLoader implements ApplicationRunner {

    private final RegionRepository regionRepository;

    @Override
    public void run(ApplicationArguments args) {
        if (regionRepository.count() > 0) {
            log.info("✅ 지역 데이터가 이미 존재합니다. (총 {}건)", regionRepository.count());
            return;
        }

        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(new ClassPathResource("regions.csv").getInputStream(), StandardCharsets.UTF_8))) {

            String line;
            boolean isFirstLine = true;
            List<Region> regionList = new ArrayList<>();

            while ((line = br.readLine()) != null) {
                if (isFirstLine) {
                    isFirstLine = false;
                    continue;
                }

                String[] data = line.split(",", -1);
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

            regionRepository.saveAll(regionList);
            log.info("✅ 지역 데이터 CSV 로딩 완료! 총 {}건", regionList.size());

        } catch (Exception e) {
            log.error("❌ 지역 데이터 로딩 중 에러 발생: ", e);
        }
    }
}