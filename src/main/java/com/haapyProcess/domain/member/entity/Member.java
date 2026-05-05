package com.haapyProcess.domain.member.entity;

import com.haapyProcess.domain.alert.entity.Alert;
import com.haapyProcess.domain.healthcondition.entity.HealthCondition;
import com.haapyProcess.domain.location.entity.Location;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "MEMBER")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MEMBER_ID")
    private Long memberId;

    @Column(name = "LOGIN_ID", length = 50, nullable = false, unique = true)
    private String loginId;

    @Column(name = "PW", length = 255, nullable = false)
    private String pw;

    @Column(name = "NAME", length = 50, nullable = false)
    private String name;

    @Column(name = "BIRTH")
    private LocalDate birth;

    @Column(name = "COMMUTE_TIME", length = 5)
    private String commuteTime;

    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL)
    private List<Location> locations = new ArrayList<>();

    @OneToOne(mappedBy = "member", cascade = CascadeType.ALL)
    private Alert alert;

    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL)
    private List<HealthCondition> healthConditions = new ArrayList<>();
}