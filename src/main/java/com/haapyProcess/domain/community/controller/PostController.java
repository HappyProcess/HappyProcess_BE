package com.haapyProcess.domain.community.controller;

import com.haapyProcess.domain.community.dto.PostCreateRequest;
import com.haapyProcess.domain.community.dto.PostDetailResponse;
import com.haapyProcess.domain.community.dto.PostListResponse;
import com.haapyProcess.domain.community.service.CommentService;
import com.haapyProcess.domain.community.service.LikeService;
import com.haapyProcess.domain.community.service.PostService;
import com.haapyProcess.domain.member.entity.Member;
import com.haapyProcess.domain.member.service.MemberService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Tag(name = "Community", description = "커뮤니티 (게시글, 사진, 댓글, 공감) API")
@RestController
@RequestMapping("/api/v1/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;
    private final LikeService likeService;
    private final CommentService commentService;

    private final MemberService memberService;

    // 1. 게시글 관련 API (생성, 목록 조회, 상세 조회)
    @Operation(
            summary = "게시글 작성",
            description = """
            ## **📋 Request Fields (multipart/form-data)**

            | **키** | **설명** | **타입** | **필수** |
            |---|---|---|:---:|
            | **title** | 게시글 제목 | String | ✅ |
            | **content** | 게시글 본문 | String | ✅ |
            | **conditionIds** | 질환 ID 목록 (비워두면 자유 게시판 글로 등록됨) | List&lt;Long&gt; | ❌ |
            | **images** | 첨부 사진 파일 목록 | List&lt;MultipartFile&gt; | ❌ |

            > ⚠️ **주의:** JSON 형식이 아닌 `multipart/form-data` 형식으로 전송해야 합니다.

            ---

            ## **📋 Response Fields**

            | **키** | **설명** | **타입** | **필수** |
            |---|---|---|:---:|
            | **postId** | 생성된 게시글 ID (응답값 자체) | Long | ✅ |
            """
    )
    @ApiResponse(responseCode = "200", description = "게시글 작성 성공")
    @ApiResponse(responseCode = "400", description = "유효성 검사 실패 (필수값 누락)")
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Long> createPost(@ModelAttribute PostCreateRequest request) {
        Member currentMember = memberService.getCurrentMember();

        Long postId = postService.createPost(currentMember, request);
        return ResponseEntity.ok(postId);
    }

    @Operation(
            summary = "게시글 목록 조회",
            description = """
            ## **📋 Request Parameters (Query String)**

            | **파라미터** | **설명** | **타입** | **기본값** | **필수** |
            |---|---|---|---|:---:|
            | **isFree** | 순수 자유글(태그 없는 글)만 조회할지 여부 | boolean | false | ❌ |
            | **conditionIds** | 필터링할 질병 ID 목록 (예: 1, 2) | List&lt;Long&gt; | | ❌ |
            | **page** | 페이지 번호 (0부터 시작) | int | 0 | ❌ |
            | **size** | 한 페이지당 불러올 글의 개수 | int | 10 | ❌ |
            | **sort** | 정렬 기준 (`createdAt,desc` 또는 `likeCount,desc`) | String | createdAt,desc | ❌ |

            ---

            ## **📋 Response Fields (Page 객체 내부 content 구조)**

            | **키** | **설명** | **타입** |
            |---|---|---|
            | **postId** | 게시글 ID | Long |
            | **title** | 게시글 제목 | String |
            | **writerName** | 작성자 이름 | String |
            | **viewCount** | 조회수 | int |
            | **likeCount** | 공감수 | int |
            | **commentCount**| 댓글수 | int |
            | **createdAt** | 작성 시간 | LocalDateTime |
            | **hasImage** | 첨부 사진 포함 여부 (목록 아이콘 표시용) | boolean |
            | **categories** | 태그된 질환 이름 목록 (예: ["천식", "고혈압"]) | List&lt;String&gt; |
            """
    )
    @ApiResponse(responseCode = "200", description = "게시글 목록 조회 성공")
    @GetMapping
    public ResponseEntity<Page<PostListResponse>> getPosts(
            @Parameter(description = "자유글만 볼지 여부") @RequestParam(defaultValue = "false") boolean isFree,
            @Parameter(description = "필터링할 질병 ID 목록 (예: 1,2)") @RequestParam(required = false) List<Long> conditionIds,
            @Parameter(description = "정렬 및 페이징 설정 (기본: 최신순)")
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

        Page<PostListResponse> response = postService.getPosts(isFree, conditionIds, pageable);
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "게시글 상세 조회",
            description = """
            호출 시 **자동으로 조회수가 1 증가**합니다.

            ## **📋 Request Fields (Path Variable)**
            * `postId`: 조회할 게시글의 ID

            ---

            ## **📋 Response Fields**

            | **키** | **설명** | **타입** |
            |---|---|---|
            | **postId** | 게시글 ID | Long |
            | **title** | 게시글 제목 | String |
            | **content** | 게시글 본문 | String |
            | **writerName** | 작성자 이름 | String |
            | **viewCount** | 조회수 | int |
            | **likeCount** | 공감수 | int |
            | **commentCount**| 댓글수 | int |
            | **createdAt** | 작성 시간 | LocalDateTime |
            | **isLikedByMe** | 내가 이 글에 공감을 눌렀는지 여부 | boolean |
            | **categories** | 태그된 질환 이름 목록 | List&lt;String&gt; |
            | **imageUrls** | 첨부된 이미지 접근 URL 목록 | List&lt;String&gt; |
            | **comments** | 이 글에 달린 댓글 목록 객체 배열 | List&lt;CommentResponse&gt; |
            """
    )
    @ApiResponse(responseCode = "200", description = "상세 조회 성공")
    @ApiResponse(responseCode = "404", description = "존재하지 않는 게시글")
    @GetMapping("/{postId}")
    public ResponseEntity<PostDetailResponse> getPostDetail(@PathVariable Long postId) {
        Member currentMember = memberService.getCurrentMember();
        PostDetailResponse response = postService.getPostDetail(currentMember, postId);
        return ResponseEntity.ok(response);
    }


    @Operation(
            summary = "공감 누르기 / 취소",
            description = """
            이미 공감한 상태에서 한 번 더 호출하면 공감이 취소(Toggle)됩니다.

            ## **📋 Request Fields (Path Variable)**
            * `postId`: 공감을 누를 게시글의 ID

            ---

            ## **📋 Response Fields**

            | **키** | **설명** | **타입** | **필수** |
            |---|---|---|:---:|
            | **isLiked** | 최종 공감 상태 (true: 공감 완료, false: 공감 취소) | boolean | ✅ |
            """
    )
    @ApiResponse(responseCode = "200", description = "공감 토글 성공")
    @ApiResponse(responseCode = "404", description = "존재하지 않는 게시글")
    @PostMapping("/{postId}/likes")
    public ResponseEntity<Map<String, Boolean>> toggleLike(@PathVariable Long postId) {
        Member currentMember = memberService.getCurrentMember();

        boolean isLiked = likeService.toggleLike(currentMember, postId);

        Map<String, Boolean> response = new HashMap<>();
        response.put("isLiked", isLiked);
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "댓글 작성",
            description = """
            ## **📋 Request Fields (Path Variable & JSON Body)**
            * `postId`: 댓글을 작성할 게시글의 ID (경로 변수)

            | **키** | **설명** | **타입** | **필수** |
            |---|---|---|:---:|
            | **content** | 댓글 내용 | String | ✅ |

            ---

            ## **📋 Response Fields**

            | **키** | **설명** | **타입** | **필수** |
            |---|---|---|:---:|
            | **commentId** | 생성된 댓글 ID (응답값 자체) | Long | ✅ |
            """
    )
    @io.swagger.v3.oas.annotations.parameters.RequestBody(content = @Content(examples = @ExampleObject(value = """
            {
              "content": "이 정보 정말 유용하네요! 감사합니다."
            }
            """)))
    @ApiResponse(responseCode = "200", description = "댓글 작성 성공")
    @ApiResponse(responseCode = "404", description = "존재하지 않는 게시글")
    @PostMapping("/{postId}/comments")
    public ResponseEntity<Long> createComment(
            @PathVariable Long postId,
            @RequestBody Map<String, String> request) {

        Member currentMember = memberService.getCurrentMember();

        String content = request.get("content");
        Long commentId = commentService.createComment(currentMember, postId, content);

        return ResponseEntity.ok(commentId);
    }
}