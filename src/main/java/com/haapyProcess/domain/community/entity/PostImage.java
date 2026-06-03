package com.haapyProcess.domain.community.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "POST_IMAGE")
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class PostImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "POST_IMAGE_ID")
    private Long postImageId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "POST_ID", nullable = false)
    private Post post;

    // 로컬 폴더에 저장된 이미지의 접근 주소
    @Column(name = "IMAGE_URL", nullable = false, length = 500)
    private String imageUrl;
}