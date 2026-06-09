package com.haapyProcess.domain.member.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import java.time.LocalDate;

@Getter
public class UpdateProfileRequest {
    @Schema(description = "이름 (2~10자)", example = "김철수")
    @Size(min = 2, max = 10, message = "이름은 2~10자여야 합니다.")
    private String name;

    @Schema(description = "생년월일 (yyyy-MM-dd)", example = "1995-05-05")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate birth;

    @Schema(description = "휴대폰 번호 (하이픈 없이 숫자만)", example = "01012345678")
    @Pattern(regexp = "^$|^01[016789]\\d{7,8}$", message = "휴대폰 번호는 하이픈 없이 숫자만 입력해주세요. (예: 01012345678)")
    private String phoneNumber;
}
