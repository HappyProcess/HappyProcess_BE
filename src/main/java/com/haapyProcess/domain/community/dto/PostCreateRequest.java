package com.haapyProcess.domain.community.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class PostCreateRequest {
    private String title;
    private String content;

    // 유저가 선택한 질병 태그 ID 목록 (null이거나 비어있으면 '자유' 글로 판별)
    private List<Long> conditionIds;

    private List<MultipartFile> images;
}