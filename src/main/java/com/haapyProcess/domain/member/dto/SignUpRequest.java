package com.haapyProcess.domain.member.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.haapyProcess.domain.location.entity.LocationType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Getter;

import java.time.LocalDate;
import java.util.List;

@Getter
public class SignUpRequest {

    @NotBlank
    @Pattern(regexp = "^[a-zA-Z0-9]{4,20}$", message = "아이디는 영문/숫자 4~20자여야 합니다.")
    private String loginId;

    @NotBlank
    @Pattern(regexp = "^(?=.*[a-zA-Z])(?=.*\\d)(?=.*[!@#$%^&*])[a-zA-Z\\d!@#$%^&*]{8,20}$",
            message = "비밀번호는 영문/숫자/특수문자 포함 8~20자여야 합니다.")
    private String password;

    @NotBlank
    @Size(min = 2, max = 10, message = "이름은 2~10자여야 합니다.")
    private String name;

    @NotNull
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate birth;

    private String commuteTime;

    @NotEmpty(message = "위치 정보를 1개 이상 입력해주세요.")
    @Valid
    private List<LocationRequest> locations;

    private List<Long> conditionIds;

    @Getter
    public static class LocationRequest {

        @NotNull
        private LocationType locationType;

        @NotBlank
        private String city;
    }
}
