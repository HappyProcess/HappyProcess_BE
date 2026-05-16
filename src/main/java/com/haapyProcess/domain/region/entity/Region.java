// src/main/java/com/haapyProcess/domain/region/entity/Region.java
package com.haapyProcess.domain.region.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

@Entity
@Table(name = "REGION")
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Region {

    @Id
    @Column(name = "AREA_NO", length = 10)
    private String areaNo;      // PK: 행정구역코드 (A열)

    @Column(name = "SIDO", length = 20)
    private String sido;        // 시도 (B열)

    @Column(name = "SIGUNGU", length = 20)
    private String sigungu;     // 시군구 (C열)

    @Column(name = "DONG", length = 20)
    private String dong;        // 동 (D열)

    @Column(name = "NX", length = 10)
    private String nx;          // 날씨 x좌표 (E열)

    @Column(name = "NY", length = 10)
    private String ny;          // 날씨 y좌표 (F열)

    @Column(name = "SIDO_NAME", length = 20)
    private String sidoName;    // 시도약어 (G열)

    @Column(name = "STATION_NAME", length = 20)
    private String stationName; // 미세먼지 측정소명 (H열)
}