package com.haapyProcess.domain.community.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class CommentResponse {
    private Long commentId;
    private String writerName;
    private String content;
    private LocalDateTime createdAt;
}