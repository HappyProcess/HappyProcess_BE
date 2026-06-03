package com.haapyProcess.domain.member.controller;

import com.haapyProcess.domain.community.dto.CommentResponse;
import com.haapyProcess.domain.community.dto.PostListResponse;
import com.haapyProcess.domain.community.service.CommentService;
import com.haapyProcess.domain.community.service.PostService;
import com.haapyProcess.domain.member.entity.Member;
import com.haapyProcess.domain.member.service.MemberService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "MyPage", description = "마이페이지 (내 활동 내역 조회) API")
@RestController
@RequestMapping("/api/v1/members/me")
@RequiredArgsConstructor
public class MyPageController {

    private final PostService postService;
    private final CommentService commentService;
    private final MemberService memberService;

    @Operation(
            summary = "내가 쓴 글 목록 조회",
            description = "현재 로그인한 회원이 작성한 커뮤니티 게시글 목록을 불러옵니다. (메인 커뮤니티 목록과 동일한 반환 구조)"
    )
    @ApiResponse(responseCode = "200", description = "조회 성공")
    @GetMapping("/posts")
    public ResponseEntity<Page<PostListResponse>> getMyPosts(
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

        Member currentMember = memberService.getCurrentMember();
        Page<PostListResponse> response = postService.getMyPosts(currentMember, pageable);
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "내가 공감한 글 목록 조회",
            description = "현재 로그인한 회원이 하트(공감)를 누른 타인/본인의 게시글 목록을 불러옵니다."
    )
    @ApiResponse(responseCode = "200", description = "조회 성공")
    @GetMapping("/likes")
    public ResponseEntity<Page<PostListResponse>> getMyLikedPosts(
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

        Member currentMember = memberService.getCurrentMember();
        Page<PostListResponse> response = postService.getMyLikedPosts(currentMember, pageable);
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "내가 쓴 댓글 목록 조회",
            description = "현재 로그인한 회원이 작성한 댓글 목록을 불러옵니다. 프론트엔드에서 댓글 클릭 시 원본 글로 이동할 수 있도록 게시글 정보도 함께 포함할 수 있습니다."
    )
    @ApiResponse(responseCode = "200", description = "조회 성공")
    @GetMapping("/comments")
    public ResponseEntity<Page<CommentResponse>> getMyComments(
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

        Member currentMember = memberService.getCurrentMember();
        Page<CommentResponse> response = commentService.getMyComments(currentMember, pageable);
        return ResponseEntity.ok(response);
    }
}