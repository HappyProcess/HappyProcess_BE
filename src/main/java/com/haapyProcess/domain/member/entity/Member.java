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

    // 문자 알림 수신 동의 여부 (opt-in). 기본은 미수신이며, 마이페이지에서 명시적으로 켜야 발송된다.
    // 미설정(null)도 미수신으로 간주한다.
    @Builder.Default
    @Column(name = "SMS_ENABLED")
    private Boolean smsEnabled = false;

    // 강수 비선호 설정 (질병 없는 사용자의 강수 불편도 점수에 사용). 미설정(null)은 NONE으로 간주한다.
    @Enumerated(EnumType.STRING)
    @Column(name = "PRECIP_PREFERENCE", length = 10)
    @Builder.Default
    private PrecipPreference precipPreference = PrecipPreference.NONE;

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

    public void updateProfile(String name, LocalDate birth, String phoneNumber, Boolean smsEnabled,
                              PrecipPreference precipPreference) {
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
        if (precipPreference != null) {
            this.precipPreference = precipPreference;
        }
    }

    // 미설정(null)은 NONE으로 간주
    public PrecipPreference getPrecipPreferenceOrDefault() {
        return precipPreference != null ? precipPreference : PrecipPreference.NONE;
    }

    public boolean hasPhoneNumber() {
        return phoneNumber != null && !phoneNumber.isBlank();
    }

    // 미설정(null)은 미수신으로 간주 (opt-in)
    public boolean isSmsEnabled() {
        return smsEnabled != null && smsEnabled;
    }
}
