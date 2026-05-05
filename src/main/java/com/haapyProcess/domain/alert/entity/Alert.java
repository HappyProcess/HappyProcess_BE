package com.haapyProcess.domain.alert.entity;

import com.haapyProcess.domain.member.entity.Member;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "ALERT")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Alert {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ALERT_ID")
    private Long alertId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MEMBER_ID")
    private Member member;

    @Column(name = "ENABLE")
    private Boolean enable;

    @Column(name = "ALERT_TIME", length = 5)
    private String alertTime;

    @Column(name = "DUST")
    private Boolean dust;

    @Column(name = "HUMIDITY")
    private Boolean humidity;

    @Column(name = "UV")
    private Boolean uv;

    @Column(name = "TEMP")
    private Boolean temp;
}