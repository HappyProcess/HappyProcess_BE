package com.haapyProcess.domain.location.entity;

import com.haapyProcess.domain.member.entity.Member;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(name = "LOCATION")
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Location {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "LOCATION_ID")
    private Long locationId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MEMBER_ID")
    private Member member;

    @Enumerated(EnumType.STRING)
    @Column(name = "LOCATION_TYPE", length = 20)
    private LocationType locationType;

    @Column(name = "CITY", length = 20)
    private String city;

    @Column(name = "LAT", precision = 10, scale = 7)
    private BigDecimal lat;

    @Column(name = "LON", precision = 10, scale = 7)
    private BigDecimal lon;
}
