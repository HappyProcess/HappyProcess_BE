package com.haapyProcess.domain.member.entity;

import com.haapyProcess.domain.alert.entity.Alert;
import com.haapyProcess.domain.healthcondition.entity.HealthCondition;
import com.haapyProcess.domain.location.entity.Location;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "MEMBER")
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
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

    @Builder.Default
    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Location> locations = new ArrayList<>();

    @OneToOne(mappedBy = "member", cascade = CascadeType.ALL)
    private Alert alert;

    @Builder.Default
    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<HealthCondition> healthConditions = new ArrayList<>();

    // 프로필 수정을 위한 비즈니스 로직
    public void updateProfile(String name, LocalDate birth, String commuteTime) {
        if (name != null && !name.isBlank()) {
            this.name = name;
        }
        if (birth != null) {
            this.birth = birth;
        }
        if (commuteTime != null) {
            this.commuteTime = commuteTime;
        }
    }
}
