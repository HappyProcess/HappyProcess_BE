package com.haapyProcess.global.util;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Component
public class FileUploader {

    @Value("${file.dir}")
    private String fileDir;

    @PostConstruct
    public void init() {
        File dir = new File(fileDir);
        if (!dir.exists()) {
            boolean wasSuccessful = dir.mkdirs();
            if (wasSuccessful) {
                log.info("사진 저장용 폴더가 자동 생성되었습니다: {}", fileDir);
            } else {
                log.warn("사진 저장용 폴더 생성에 실패했습니다. 권한을 확인하세요: {}", fileDir);
            }
        }
    }

    public List<String> uploadFiles(List<MultipartFile> multipartFiles) {
        List<String> imageUrls = new ArrayList<>();

        if (multipartFiles == null || multipartFiles.isEmpty()) {
            return imageUrls;
        }

        for (MultipartFile file : multipartFiles) {
            if (!file.isEmpty()) {
                String uploadedUrl = uploadSingleFile(file);
                imageUrls.add(uploadedUrl);
            }
        }
        return imageUrls;
    }

    private String uploadSingleFile(MultipartFile multipartFile) {
        String originalFilename = multipartFile.getOriginalFilename();

        String extension = extractExtension(originalFilename);

        String savedFilename = UUID.randomUUID().toString() + extension;

        String fullPath = fileDir + savedFilename;

        try {
            multipartFile.transferTo(new File(fullPath));
            log.info("새로운 사진이 성공적으로 저장되었습니다: {}", fullPath);

        } catch (IOException e) {
            log.error("사진 저장 중 치명적인 오류가 발생했습니다: {}", originalFilename, e);
            throw new RuntimeException("파일 업로드에 실패했습니다.", e);
        }

        return "/images/" + savedFilename;
    }

    private String extractExtension(String originalFilename) {
        if (originalFilename == null || !originalFilename.contains(".")) {
            return "";
        }
        int pos = originalFilename.lastIndexOf(".");
        return originalFilename.substring(pos);
    }
}