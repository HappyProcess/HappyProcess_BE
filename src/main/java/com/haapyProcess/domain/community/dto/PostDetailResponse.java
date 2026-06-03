package com.haapyProcess.domain.community.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class PostDetailResponse {
    private Long postId;
    private String title;
    private String content;
    private String writerName;
    private int viewCount;
    private int likeCount;
    private int commentCount;
    private LocalDateTime createdAt;

    // 현재 이 글을 조회하는 유저가 '공감'을 눌렀는지 여부
    private boolean isLikedByMe;

    private List<String> categories;
    private List<String> imageUrls; // 실제 사진을 보여주기 위한 URL 리스트
    private List<CommentResponse> comments;
}