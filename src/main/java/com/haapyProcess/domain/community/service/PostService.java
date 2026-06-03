package com.haapyProcess.domain.community.service;

import com.haapyProcess.domain.community.dto.PostCreateRequest;
import com.haapyProcess.domain.community.dto.PostDetailResponse;
import com.haapyProcess.domain.community.dto.PostListResponse;
import com.haapyProcess.domain.community.dto.PostUpdateRequest;
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
                post.getPostImages().add(PostImage.builder().post(post).imageUrl(url).build());
            }
        }

        if (request.getConditionIds() != null && !request.getConditionIds().isEmpty()) {
            List<Condition> conditions = conditionRepository.findAllById(request.getConditionIds());
            for (Condition condition : conditions) {
                post.getPostConditions().add(PostCondition.builder().post(post).condition(condition).build());
            }
        }

        return postRepository.save(post).getPostId();
    }

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

    @Transactional
    public void updatePost(Member currentMember, Long postId, PostUpdateRequest request) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("해당 게시글을 찾을 수 없습니다."));

        if (!post.getMember().getMemberId().equals(currentMember.getMemberId())) {
            throw new IllegalArgumentException("게시글 수정 권한이 없습니다.");
        }

        post.updatePost(request.getTitle(), request.getContent());

        post.clearConditions();
        if (request.getConditionIds() != null && !request.getConditionIds().isEmpty()) {
            List<Condition> conditions = conditionRepository.findAllById(request.getConditionIds());
            for (Condition condition : conditions) {
                post.getPostConditions().add(PostCondition.builder().post(post).condition(condition).build());
            }
        }

        if (request.getDeleteImageIds() != null && !request.getDeleteImageIds().isEmpty()) {
            List<PostImage> imagesToRemove = post.getPostImages().stream()
                    .filter(img -> request.getDeleteImageIds().contains(img.getPostImageId()))
                    .collect(Collectors.toList());

            for (PostImage img : imagesToRemove) {
                post.removeImage(img);
            }
        }

        if (request.getNewImages() != null && !request.getNewImages().isEmpty()) {
            List<String> uploadedUrls = fileUploader.uploadFiles(request.getNewImages());
            for (String url : uploadedUrls) {
                post.getPostImages().add(PostImage.builder().post(post).imageUrl(url).build());
            }
        }
    }

    @Transactional
    public void deletePost(Member currentMember, Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("해당 게시글을 찾을 수 없습니다."));

        if (!post.getMember().getMemberId().equals(currentMember.getMemberId())) {
            throw new IllegalArgumentException("게시글 삭제 권한이 없습니다.");
        }

        postRepository.delete(post);
    }

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
        return mapToPostListResponse(postPage);
    }

    @Transactional(readOnly = true)
    public Page<PostListResponse> searchPosts(String keyword, Pageable pageable) {
        return mapToPostListResponse(postRepository.searchPosts(keyword, pageable));
    }

    @Transactional(readOnly = true)
    public Page<PostListResponse> getMyPosts(Member currentMember, Pageable pageable) {
        return mapToPostListResponse(postRepository.findAllByMember(currentMember, pageable));
    }

    @Transactional(readOnly = true)
    public Page<PostListResponse> getMyLikedPosts(Member currentMember, Pageable pageable) {
        return mapToPostListResponse(postRepository.findLikedPostsByMember(currentMember, pageable));
    }


    private Page<PostListResponse> mapToPostListResponse(Page<Post> postPage) {
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
}