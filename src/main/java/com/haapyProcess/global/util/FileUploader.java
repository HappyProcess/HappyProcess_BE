package com.haapyProcess.global.util;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class FileUploader {

    private final RestClient restClient;

    @Value("${supabase.url}")
    private String supabaseUrl;

    @Value("${supabase.service-key}")
    private String serviceKey;

    @Value("${supabase.bucket}")
    private String bucket;

    public List<String> uploadFiles(List<MultipartFile> multipartFiles) {
        List<String> imageUrls = new ArrayList<>();

        if (multipartFiles == null || multipartFiles.isEmpty()) {
            return imageUrls;
        }

        for (MultipartFile file : multipartFiles) {
            if (!file.isEmpty()) {
                imageUrls.add(uploadSingleFile(file));
            }
        }
        return imageUrls;
    }

    private String uploadSingleFile(MultipartFile multipartFile) {
        String originalFilename = multipartFile.getOriginalFilename();
        String extension = extractExtension(originalFilename);
        String objectPath = "posts/" + UUID.randomUUID() + extension;

        try {
            MediaType contentType = multipartFile.getContentType() != null
                    ? MediaType.parseMediaType(multipartFile.getContentType())
                    : MediaType.APPLICATION_OCTET_STREAM;

            restClient.post()
                    .uri(supabaseUrl + "/storage/v1/object/" + bucket + "/" + objectPath)
                    .header("Authorization", "Bearer " + serviceKey)
                    .contentType(contentType)
                    .body(multipartFile.getBytes())
                    .retrieve()
                    .toBodilessEntity();

            log.info("Supabase Storage 업로드 성공: {}", objectPath);

        } catch (IOException e) {
            log.error("파일 바이트 읽기 실패: {}", originalFilename, e);
            throw new RuntimeException("파일 업로드에 실패했습니다.", e);
        } catch (Exception e) {
            log.error("Supabase Storage 업로드 실패: {}", objectPath, e);
            throw new RuntimeException("파일 업로드에 실패했습니다.", e);
        }

        // public 버킷 기준 외부 접근 URL
        return supabaseUrl + "/storage/v1/object/public/" + bucket + "/" + objectPath;
    }

    private String extractExtension(String originalFilename) {
        if (originalFilename == null || !originalFilename.contains(".")) {
            return "";
        }
        int pos = originalFilename.lastIndexOf(".");
        return originalFilename.substring(pos);
    }
}
