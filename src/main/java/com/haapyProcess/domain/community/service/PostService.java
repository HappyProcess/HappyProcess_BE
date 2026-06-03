package com.haapyProcess.domain.community.service;

import com.haapyProcess.domain.community.dto.PostCreateRequest;
import com.haapyProcess.domain.community.dto.PostDetailResponse;
import com.haapyProcess.domain.community.dto.PostListResponse;
import com.haapyProcess.domain.community.dto.CommentResponse;
import com.haapyProcess.domain.community.entity.Post;
import com.haapyProcess.domain.community.entity.PostCondition;
import com.haapyProcess.domain.community.entity.PostImage;
import com.haapyProcess.domain.community.repository.CommentRepository;
import com.haapyProcess.domain.community.repository.PostLikeRepository;
import com.haapyProcess.domain.community.repository.PostRepository;
import com.haapyProcess.domain.condition.entity.Condition;
import com.haapyProcess.domain.condition.repository.ConditionRepository;
import com.haapyProcess.domain.member.entity.Member;
import com.haapyProcess.global.util.FileUploader;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final ConditionRepository conditionRepository;
    private final CommentRepository commentRepository;
    private final PostLikeRepository postLikeRepository;

    private final FileUploader fileUploader;

    /**
     * [1. 게시글 작성 로직]
     */
    @Transactional
    public Long createPost(Member currentMember, PostCreateRequest request) {

        Post post = Post.builder()
                .member(currentMember)
                .title(request.getTitle())
                .content(request.getContent())
                .build();

        if (request.getImages() != null && !request.getImages().isEmpty()) {
            List<String> uploadedUrls = fileUploader.uploadFiles(request.getImages());

            for (String url : uploadedUrls) {
                PostImage postImage = PostImage.builder()
                        .post(post)
                        .imageUrl(url)
                        .build();
                post.getPostImages().add(postImage);
            }
        }

        if (request.getConditionIds() != null && !request.getConditionIds().isEmpty()) {
            List<Condition> conditions = conditionRepository.findAllById(request.getConditionIds());
            for (Condition condition : conditions) {
                PostCondition postCondition = PostCondition.builder()
                        .post(post)
                        .condition(condition)
                        .build();
                post.getPostConditions().add(postCondition);
            }
        }

        return postRepository.save(post).getPostId();
    }

    /**
     * [2. 게시글 목록 조회 로직 (필터링 적용)]
     */
    @Transactional(readOnly = true)
    public Page<PostListResponse> getPosts(boolean isFree, List<Long> conditionIds, Pageable pageable) {
        Page<Post> postPage;

        if (isFree) {
            postPage = postRepository.findFreePosts(pageable);
        } else if (conditionIds != null && !conditionIds.isEmpty()) {
            postPage = postRepository.findPostsByConditionIds(conditionIds, pageable);
        } else {
            postPage = postRepository.findAll(pageable);
        }

        return postPage.map(post -> {
            boolean hasImage = !post.getPostImages().isEmpty();
            List<String> categories = post.getPostConditions().stream()
                    .map(pc -> pc.getCondition().getConditionName())
                    .collect(Collectors.toList());

            return PostListResponse.builder()
                    .postId(post.getPostId())
                    .title(post.getTitle())
                    .writerName(post.getMember().getName())
                    .viewCount(post.getViewCount())
                    .likeCount(post.getLikeCount())
                    .commentCount(post.getCommentCount())
                    .createdAt(post.getCreatedAt())
                    .hasImage(hasImage)
                    .categories(categories)
                    .build();
        });
    }

    /**
     * [3. 게시글 상세 조회 로직]
     */
    @Transactional
    public PostDetailResponse getPostDetail(Member currentMember, Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("해당 게시글을 찾을 수 없습니다."));

        post.addViewCount();

        boolean isLikedByMe = postLikeRepository.existsByPostAndMember(post, currentMember);

        List<String> categories = post.getPostConditions().stream()
                .map(pc -> pc.getCondition().getConditionName())
                .collect(Collectors.toList());

        List<String> imageUrls = post.getPostImages().stream()
                .map(PostImage::getImageUrl)
                .collect(Collectors.toList());

        List<CommentResponse> comments = commentRepository.findAllByPostOrderByCreatedAtAsc(post).stream()
                .map(comment -> CommentResponse.builder()
                        .commentId(comment.getCommentId())
                        .writerName(comment.getMember().getName())
                        .content(comment.getContent())
                        .createdAt(comment.getCreatedAt())
                        .build())
                .collect(Collectors.toList());

        return PostDetailResponse.builder()
                .postId(post.getPostId())
                .title(post.getTitle())
                .content(post.getContent())
                .writerName(post.getMember().getName())
                .viewCount(post.getViewCount())
                .likeCount(post.getLikeCount())
                .commentCount(post.getCommentCount())
                .createdAt(post.getCreatedAt())
                .isLikedByMe(isLikedByMe)
                .categories(categories)
                .imageUrls(imageUrls)
                .comments(comments)
                .build();
    }
}