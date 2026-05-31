package com.haapyProcess.domain.location.entity;

import com.haapyProcess.domain.member.entity.Member;
import com.haapyProcess.domain.region.entity.Region;
import jakarta.persistence.*;
import lombok.*;

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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "AREA_NO")
    private Region region;

    public void updateRegion(Region region) {
        this.region = region;
    }
}
