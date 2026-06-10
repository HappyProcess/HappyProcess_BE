package com.haapyProcess.domain.member.entity;

import com.haapyProcess.domain.alert.entity.Alert;
import com.haapyProcess.domain.alert.entity.NotificationHistory;
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

    // 위험 알림 문자 수신용 휴대폰 번호 (하이픈 없이 저장, 선택 입력)
    @Column(name = "PHONE_NUMBER", length = 20)
    private String phoneNumber;

    // 문자 알림 수신 여부. 인앱 알림은 그대로 받되 문자만 끄고 싶을 때 사용.
    // 기존 회원(null)은 수신(true)으로 간주한다.
    @Builder.Default
    @Column(name = "SMS_ENABLED")
    private Boolean smsEnabled = true;

    @Builder.Default
    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Location> locations = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Alert> alerts = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<NotificationHistory> notificationHistories = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<HealthCondition> healthConditions = new ArrayList<>();

    public void updateProfile(String name, LocalDate birth, String phoneNumber, Boolean smsEnabled) {
        if (name != null && !name.isBlank()) {
            this.name = name;
        }
        if (birth != null) {
            this.birth = birth;
        }
        if (phoneNumber != null && !phoneNumber.isBlank()) {
            this.phoneNumber = phoneNumber;
        }
        if (smsEnabled != null) {
            this.smsEnabled = smsEnabled;
        }
    }

    public boolean hasPhoneNumber() {
        return phoneNumber != null && !phoneNumber.isBlank();
    }

    // 기존 회원(null)은 수신으로 간주
    public boolean isSmsEnabled() {
        return smsEnabled == null || smsEnabled;
    }
}
