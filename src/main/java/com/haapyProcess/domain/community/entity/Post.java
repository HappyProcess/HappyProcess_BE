package com.haapyProcess.domain.community.entity;

import com.haapyProcess.domain.member.entity.Member;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "POST")
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "POST_ID")
    private Long postId;

    // 작성자 정보
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MEMBER_ID", nullable = false)
    private Member member;

    @Column(name = "TITLE", length = 100, nullable = false)
    private String title;

    @Column(name = "CONTENT", columnDefinition = "TEXT", nullable = false)
    private String content;

    @Builder.Default
    @Column(name = "VIEW_COUNT", nullable = false)
    private int viewCount = 0;

    @Builder.Default
    @Column(name = "LIKE_COUNT", nullable = false)
    private int likeCount = 0;

    @Builder.Default
    @Column(name = "COMMENT_COUNT", nullable = false)
    private int commentCount = 0;

    @Column(name = "CREATED_AT", updatable = false, nullable = false)
    private LocalDateTime createdAt;

    // 게시글에 달린 여러 개의 질병 태그 (없으면 '자유' 글)
    @Builder.Default
    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PostCondition> postConditions = new ArrayList<>();

    // 사진 리스트
    @Builder.Default
    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PostImage> postImages = new ArrayList<>();

    // 공감 기록 리스트
    @Builder.Default
    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PostLike> postLikes = new ArrayList<>();

    // 댓글 리스트
    @Builder.Default
    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Comment> comments = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    public void addViewCount() { this.viewCount++; }
    public void addLikeCount() { this.likeCount++; }
    public void removeLikeCount() { if(this.likeCount > 0) this.likeCount--; }
    public void addCommentCount() { this.commentCount++; }
    public void removeCommentCount() { if(this.commentCount > 0) this.commentCount--; }
}