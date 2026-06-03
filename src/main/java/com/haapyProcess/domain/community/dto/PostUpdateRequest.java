package com.haapyProcess.domain.community.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class PostUpdateRequest {

    private String title;
    private String content;

    private List<Long> conditionIds;

    private List<MultipartFile> newImages;

    private List<Long> deleteImageIds;
}