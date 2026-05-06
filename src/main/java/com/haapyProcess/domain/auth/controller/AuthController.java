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

    @Operation(
        summary = "회원가입",
        description = """
            **호출 주체**: 신규 사용자
            **인증 필요 여부**: 불필요 (public endpoint)

            **비즈니스 로직**
            1. 요청 필드 유효성 검사 (loginId 영문/숫자 4~20자, password 영문/숫자/특수문자 포함 8~20자)
            2. loginId 중복 여부 확인 → 중복 시 409 반환
            3. locations에 HOME, WORK 둘 다 포함 여부 확인 → 미포함 시 400 반환
            4. 비밀번호 BCrypt 암호화 후 회원 저장
            5. 각 location의 도시명 유효성 확인 (CityCoordinate enum 기준) → 미존재 시 400 반환
            6. 위치 정보 저장 (도시명으로 위도/경도 자동 세팅)
            7. conditionIds 입력 시 질환 ID 유효성 확인 → 미존재 시 404 반환
            8. 건강 상태 매핑 저장 후 memberId 반환

            **예외**
            - 400 VALIDATION_ERROR: 요청 필드 유효성 검사 실패
            - 400 INVALID_LOCATION: HOME 또는 WORK 위치 누락
            - 400 INVALID_CITY: 지원하지 않는 도시명
            - 404 CONDITION_NOT_FOUND: 존재하지 않는 질환 ID
            - 409 DUPLICATE_LOGIN_ID: 이미 사용 중인 아이디
            """
    )
    @ApiResponse(responseCode = "201", description = "회원가입 성공")
    @ApiResponse(responseCode = "400", description = "유효성 검사 실패 / 위치 누락 / 지원하지 않는 도시명")
    @ApiResponse(responseCode = "404", description = "존재하지 않는 질환 ID")
    @ApiResponse(responseCode = "409", description = "아이디 중복")
    @PostMapping("/signup")
    public ResponseEntity<SignUpResponse> signUp(@RequestBody @Valid SignUpRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(memberService.signUp(request));
    }

    @Operation(
        summary = "로그인",
        description = """
            **호출 주체**: 가입된 사용자
            **인증 필요 여부**: 불필요 (public endpoint)

            **비즈니스 로직**
            1. 요청 필드 유효성 검사
            2. loginId로 회원 조회 → 미존재 시 404 반환
            3. 입력 비밀번호와 저장된 BCrypt 해시 비교 → 불일치 시 401 반환
            4. AccessToken 생성 (만료: JWT_ACCESS_EXPIRATION ms)
            5. RefreshToken 생성 (만료: JWT_REFRESH_EXPIRATION ms)
            6. 기존 RefreshToken 존재 시 갱신, 없으면 신규 저장
            7. AccessToken, RefreshToken 반환

            **예외**
            - 400 VALIDATION_ERROR: 요청 필드 유효성 검사 실패
            - 401 INVALID_PASSWORD: 비밀번호 불일치
            - 404 MEMBER_NOT_FOUND: 존재하지 않는 아이디
            """
    )
    @ApiResponse(responseCode = "200", description = "로그인 성공")
    @ApiResponse(responseCode = "400", description = "유효성 검사 실패")
    @ApiResponse(responseCode = "401", description = "비밀번호 불일치")
    @ApiResponse(responseCode = "404", description = "존재하지 않는 아이디")
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody @Valid LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @Operation(
        summary = "로그아웃",
        description = """
            **호출 주체**: 로그인된 사용자
            **인증 필요 여부**: 불필요 (public endpoint)

            **비즈니스 로직**
            1. 요청 필드 유효성 검사
            2. refreshToken으로 DB 조회 → 미존재 시 401 반환
            3. RefreshToken DB에서 삭제
            4. 이후 해당 RefreshToken으로 재발급 불가

            **예외**
            - 400 VALIDATION_ERROR: 요청 필드 유효성 검사 실패
            - 401 INVALID_TOKEN: 유효하지 않은 리프레시 토큰
            """
    )
    @ApiResponse(responseCode = "200", description = "로그아웃 성공")
    @ApiResponse(responseCode = "400", description = "유효성 검사 실패")
    @ApiResponse(responseCode = "401", description = "유효하지 않은 리프레시 토큰")
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestBody @Valid LogoutRequest request) {
        authService.logout(request);
        return ResponseEntity.ok().build();
    }

    @Operation(
        summary = "토큰 재발급",
        description = """
            **호출 주체**: AccessToken이 만료된 사용자
            **인증 필요 여부**: 불필요 (public endpoint)

            **비즈니스 로직**
            1. 요청 필드 유효성 검사
            2. refreshToken JWT 서명/만료 검증 → 실패 시 401 반환
            3. refreshToken으로 DB 조회 → 미존재 시 401 반환
            4. 연결된 회원 정보로 새 AccessToken 생성
            5. 새 RefreshToken 생성 (Refresh Token Rotation)
            6. DB의 RefreshToken 갱신
            7. 새 AccessToken, RefreshToken 반환

            **예외**
            - 400 VALIDATION_ERROR: 요청 필드 유효성 검사 실패
            - 401 EXPIRED_TOKEN: 만료된 리프레시 토큰
            - 401 INVALID_TOKEN: 유효하지 않은 리프레시 토큰
            """
    )
    @ApiResponse(responseCode = "200", description = "토큰 재발급 성공")
    @ApiResponse(responseCode = "400", description = "유효성 검사 실패")
    @ApiResponse(responseCode = "401", description = "만료되거나 유효하지 않은 리프레시 토큰")
    @PostMapping("/reissue")
    public ResponseEntity<ReissueResponse> reissue(@RequestBody @Valid ReissueRequest request) {
        return ResponseEntity.ok(authService.reissue(request));
    }
}
