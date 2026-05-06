package com.haapyProcess.domain.member.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.haapyProcess.domain.location.entity.LocationType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Getter;

import java.time.LocalDate;
import java.util.List;

@Getter
public class SignUpRequest {

    @Schema(description = "로그인 아이디 (영문/숫자 4~20자)", example = "user1234",
            requiredMode = Schema.RequiredMode.REQUIRED, nullable = false)
    @NotBlank
    @Pattern(regexp = "^[a-zA-Z0-9]{4,20}$", message = "아이디는 영문/숫자 4~20자여야 합니다.")
    private String loginId;

    @Schema(description = "비밀번호 (영문/숫자/특수문자 포함 8~20자)", example = "Pass1234!",
            requiredMode = Schema.RequiredMode.REQUIRED, nullable = false)
    @NotBlank
    @Pattern(regexp = "^(?=.*[a-zA-Z])(?=.*\\d)(?=.*[!@#$%^&*])[a-zA-Z\\d!@#$%^&*]{8,20}$",
            message = "비밀번호는 영문/숫자/특수문자 포함 8~20자여야 합니다.")
    private String password;

    @Schema(description = "이름 (2~10자)", example = "홍길동",
            requiredMode = Schema.RequiredMode.REQUIRED, nullable = false)
    @NotBlank
    @Size(min = 2, max = 10, message = "이름은 2~10자여야 합니다.")
    private String name;

    @Schema(description = "생년월일 (yyyy-MM-dd)", example = "2000-01-01",
            requiredMode = Schema.RequiredMode.REQUIRED, nullable = false)
    @NotNull
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate birth;

    @Schema(description = "출퇴근 시간 (선택)", example = "30분",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED, nullable = true)
    private String commuteTime;

    @Schema(description = "위치 정보 (HOME, WORK 각 1개 필수)",
            requiredMode = Schema.RequiredMode.REQUIRED, nullable = false)
    @NotEmpty(message = "위치 정보를 1개 이상 입력해주세요.")
    @Valid
    private List<LocationRequest> locations;

    @Schema(description = "질환 ID 목록",
            requiredMode = Schema.RequiredMode.REQUIRED, nullable = false)
    @NotEmpty(message = "질환 ID 목록을 1개 이상 입력해주세요.")
    private List<Long> conditionIds;

    @Getter
    public static class LocationRequest {

        @Schema(description = "위치 유형 (HOME 또는 WORK)", example = "HOME",
                requiredMode = Schema.RequiredMode.REQUIRED, nullable = false)
        @NotNull
        private LocationType locationType;

        @Schema(description = "도시명 (CityCoordinate enum 기준)", example = "서울특별시",
                requiredMode = Schema.RequiredMode.REQUIRED, nullable = false)
        @NotBlank
        private String city;
    }
}
