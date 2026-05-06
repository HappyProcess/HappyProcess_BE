package com.haapyProcess.domain.auth.controller;

import com.haapyProcess.domain.auth.dto.*;
import com.haapyProcess.domain.auth.service.AuthService;
import com.haapyProcess.domain.member.dto.SignUpRequest;
import com.haapyProcess.domain.member.dto.SignUpResponse;
import com.haapyProcess.domain.member.service.MemberService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Auth", description = "인증 관련 API")
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final MemberService memberService;

    @Operation(summary = "회원가입", description = """
            **Request**

            | 키 | 설명 | 타입 | 필수 | Nullable |
            |---|---|---|---|---|
            | loginId | 로그인 아이디 (영문/숫자 4~20자) | String | O | X |
            | password | 비밀번호 (영문/숫자/특수문자 포함 8~20자) | String | O | X |
            | name | 이름 (2~10자) | String | O | X |
            | birth | 생년월일 (yyyy-MM-dd) | LocalDate | O | X |
            | commuteTime | 출퇴근 시간 | String | X | O |
            | locations | 위치 정보 목록 (HOME·WORK 각 1개 필수) | List | O | X |
            | locations[].locationType | 위치 유형 (HOME 또는 WORK) | String(enum) | O | X |
            | locations[].city | 도시명 (CityCoordinate enum 기준) | String | O | X |
            | conditionIds | 질환 ID 목록 | List&lt;Long&gt; | O | X |

            **Response**

            | 키 | 설명 | 타입 | Nullable |
            |---|---|---|---|
            | memberId | 생성된 회원 ID | Long | X |
            """)
    @ApiResponse(responseCode = "201", description = "회원가입 성공")
    @ApiResponse(responseCode = "400", description = "유효성 검사 실패 / 위치 누락 / 지원하지 않는 도시명")
    @ApiResponse(responseCode = "404", description = "존재하지 않는 질환 ID")
    @ApiResponse(responseCode = "409", description = "아이디 중복")
    @PostMapping("/signup")
    public ResponseEntity<SignUpResponse> signUp(@RequestBody @Valid SignUpRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(memberService.signUp(request));
    }

    @Operation(summary = "로그인", description = """
            **Request**

            | 키 | 설명 | 타입 | 필수 | Nullable |
            |---|---|---|---|---|
            | loginId | 로그인 아이디 | String | O | X |
            | password | 비밀번호 | String | O | X |

            **Response**

            | 키 | 설명 | 타입 | Nullable |
            |---|---|---|---|
            | accessToken | 액세스 토큰 | String | X |
            | refreshToken | 리프레시 토큰 | String | X |
            """)
    @ApiResponse(responseCode = "200", description = "로그인 성공")
    @ApiResponse(responseCode = "400", description = "유효성 검사 실패")
    @ApiResponse(responseCode = "401", description = "비밀번호 불일치")
    @ApiResponse(responseCode = "404", description = "존재하지 않는 아이디")
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody @Valid LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @Operation(summary = "로그아웃", description = """
            **Request**

            | 키 | 설명 | 타입 | 필수 | Nullable |
            |---|---|---|---|---|
            | refreshToken | 리프레시 토큰 | String | O | X |

            **Response**: 없음
            """)
    @ApiResponse(responseCode = "200", description = "로그아웃 성공")
    @ApiResponse(responseCode = "400", description = "유효성 검사 실패")
    @ApiResponse(responseCode = "401", description = "유효하지 않은 리프레시 토큰")
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestBody @Valid LogoutRequest request) {
        authService.logout(request);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "토큰 재발급", description = """
            **Request**

            | 키 | 설명 | 타입 | 필수 | Nullable |
            |---|---|---|---|---|
            | refreshToken | 리프레시 토큰 | String | O | X |

            **Response**

            | 키 | 설명 | 타입 | Nullable |
            |---|---|---|---|
            | accessToken | 새 액세스 토큰 | String | X |
            | refreshToken | 새 리프레시 토큰 | String | X |
            """)
    @ApiResponse(responseCode = "200", description = "토큰 재발급 성공")
    @ApiResponse(responseCode = "400", description = "유효성 검사 실패")
    @ApiResponse(responseCode = "401", description = "만료되거나 유효하지 않은 리프레시 토큰")
    @PostMapping("/reissue")
    public ResponseEntity<ReissueResponse> reissue(@RequestBody @Valid ReissueRequest request) {
        return ResponseEntity.ok(authService.reissue(request));
    }
}
