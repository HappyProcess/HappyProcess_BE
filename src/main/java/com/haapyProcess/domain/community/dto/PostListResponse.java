package com.haapyProcess.domain.community.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class PostListResponse {
    private Long postId;
    private String title;
    private String writerName;
    private int viewCount;
    private int likeCount;
    private int commentCount;
    private LocalDateTime createdAt;

    // 프론트엔드에서 사진 아이콘을 띄울지 말지 결정하는 용도
    private boolean hasImage;

    private List<String> categories;
}