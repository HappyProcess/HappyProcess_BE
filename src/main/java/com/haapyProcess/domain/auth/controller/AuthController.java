package com.haapyProcess.domain.auth.controller;

import com.haapyProcess.domain.auth.dto.*;
import com.haapyProcess.domain.auth.service.AuthService;
import com.haapyProcess.domain.member.dto.SignUpRequest;
import com.haapyProcess.domain.member.dto.SignUpResponse;
import com.haapyProcess.domain.member.service.MemberService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
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

    @Operation(
        summary = "회원가입",
        description = """
            ## **📋 Request Fields**

            | **키** | **설명** | **타입** | **필수** |
            |---|---|---|:---:|
            | **loginId** | 로그인 아이디 (영문/숫자 4~20자) | String | ✅ |
            | **password** | 비밀번호 (영문/숫자/특수문자 포함 8~20자) | String | ✅ |
            | **name** | 이름 (2~10자) | String | ✅ |
            | **birth** | 생년월일 (yyyy-MM-dd) | LocalDate | ✅ |
            | **commuteTime** | 출퇴근 시간 | String | ❌ |
            | **locations** | 위치 정보 목록 | List | ✅ |
            | **locations[].locationType** | 위치 유형 | String(enum) | ✅ |
            | **locations[].city** | 도시명 (CityCoordinate enum 기준) | String | ✅ |
            | **conditionIds** | 질환 ID 목록 | List&lt;Long&gt; | ✅ |

            > ⚠️ **locations에는 반드시 `HOME`과 `WORK`를 각각 1개씩 포함해야 합니다.**

            ---

            ## **📋 Response Fields**

            | **키** | **설명** | **타입** | **필수** |
            |---|---|---|:---:|
            | **memberId** | 생성된 회원 ID | Long | ✅ |
            """
    )
    @RequestBody(content = @Content(examples = @ExampleObject(value = """
            {
              "loginId": "user1234",
              "password": "Pass1234!",
              "name": "홍길동",
              "birth": "2000-01-01",
              "commuteTime": "30분",
              "locations": [
                {
                  "locationType": "HOME",
                  "city": "서울특별시"
                },
                {
                  "locationType": "WORK",
                  "city": "부산광역시"
                }
              ],
              "conditionIds": [1, 2]
            }
            """)))
    @ApiResponse(responseCode = "201", description = "회원가입 성공")
    @ApiResponse(responseCode = "400", description = "유효성 검사 실패 / 위치 누락 / 지원하지 않는 도시명")
    @ApiResponse(responseCode = "404", description = "존재하지 않는 질환 ID")
    @ApiResponse(responseCode = "409", description = "아이디 중복")
    @PostMapping("/signup")
    public ResponseEntity<SignUpResponse> signUp(@org.springframework.web.bind.annotation.RequestBody @Valid SignUpRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(memberService.signUp(request));
    }

    @Operation(
        summary = "로그인",
        description = """
            ## **📋 Request Fields**

            | **키** | **설명** | **타입** | **필수** |
            |---|---|---|:---:|
            | **loginId** | 로그인 아이디 | String | ✅ |
            | **password** | 비밀번호 | String | ✅ |

            ---

            ## **📋 Response Fields**

            | **키** | **설명** | **타입** | **필수** |
            |---|---|---|:---:|
            | **accessToken** | 액세스 토큰 | String | ✅ |
            | **refreshToken** | 리프레시 토큰 | String | ✅ |
            """
    )
    @ApiResponse(responseCode = "200", description = "로그인 성공")
    @ApiResponse(responseCode = "400", description = "유효성 검사 실패")
    @ApiResponse(responseCode = "401", description = "비밀번호 불일치")
    @ApiResponse(responseCode = "404", description = "존재하지 않는 아이디")
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@org.springframework.web.bind.annotation.RequestBody @Valid LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @Operation(
        summary = "로그아웃",
        description = """
            ## **📋 Request Fields**

            | **키** | **설명** | **타입** | **필수** |
            |---|---|---|:---:|
            | **refreshToken** | 리프레시 토큰 | String | ✅ |

            ---

            **Response**: 없음
            """
    )
    @ApiResponse(responseCode = "200", description = "로그아웃 성공")
    @ApiResponse(responseCode = "400", description = "유효성 검사 실패")
    @ApiResponse(responseCode = "401", description = "유효하지 않은 리프레시 토큰")
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@org.springframework.web.bind.annotation.RequestBody @Valid LogoutRequest request) {
        authService.logout(request);
        return ResponseEntity.ok().build();
    }

    @Operation(
        summary = "토큰 재발급",
        description = """
            ## **📋 Request Fields**

            | **키** | **설명** | **타입** | **필수** |
            |---|---|---|:---:|
            | **refreshToken** | 리프레시 토큰 | String | ✅ |

            ---

            ## **📋 Response Fields**

            | **키** | **설명** | **타입** | **필수** |
            |---|---|---|:---:|
            | **accessToken** | 새 액세스 토큰 | String | ✅ |
            | **refreshToken** | 새 리프레시 토큰 | String | ✅ |
            """
    )
    @ApiResponse(responseCode = "200", description = "토큰 재발급 성공")
    @ApiResponse(responseCode = "400", description = "유효성 검사 실패")
    @ApiResponse(responseCode = "401", description = "만료되거나 유효하지 않은 리프레시 토큰")
    @PostMapping("/reissue")
    public ResponseEntity<ReissueResponse> reissue(@org.springframework.web.bind.annotation.RequestBody @Valid ReissueRequest request) {
        return ResponseEntity.ok(authService.reissue(request));
    }
}
