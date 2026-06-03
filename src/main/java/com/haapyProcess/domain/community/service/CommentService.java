package com.haapyProcess.domain.community.service;

import com.haapyProcess.domain.community.dto.CommentResponse;
import com.haapyProcess.domain.community.dto.CommentUpdateRequest;
import com.haapyProcess.domain.community.entity.Comment;
import com.haapyProcess.domain.community.entity.Post;
import com.haapyProcess.domain.community.repository.CommentRepository;
import com.haapyProcess.domain.community.repository.PostRepository;
import com.haapyProcess.domain.member.entity.Member;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;

    @Transactional
    public Long createComment(Member currentMember, Long postId, String content) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("해당 게시글을 찾을 수 없습니다."));

        Comment comment = Comment.builder()
                .post(post)
                .member(currentMember)
                .content(content)
                .build();

        commentRepository.save(comment);
        post.addCommentCount();

        return comment.getCommentId();
    }

    @Transactional
    public void updateComment(Member currentMember, Long postId, Long commentId, CommentUpdateRequest request) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("해당 댓글을 찾을 수 없습니다."));

        if (!comment.getMember().getMemberId().equals(currentMember.getMemberId())) {
            throw new IllegalArgumentException("댓글 수정 권한이 없습니다.");
        }
        if (!comment.getPost().getPostId().equals(postId)) {
            throw new IllegalArgumentException("잘못된 요청 경로입니다.");
        }

        comment.updateContent(request.getContent());
    }

    @Transactional
    public void deleteComment(Member currentMember, Long postId, Long commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("해당 댓글을 찾을 수 없습니다."));

        if (!comment.getMember().getMemberId().equals(currentMember.getMemberId())) {
            throw new IllegalArgumentException("댓글 삭제 권한이 없습니다.");
        }
        if (!comment.getPost().getPostId().equals(postId)) {
            throw new IllegalArgumentException("잘못된 요청 경로입니다.");
        }

        commentRepository.delete(comment);
        comment.getPost().removeCommentCount();
    }


    @Transactional(readOnly = true)
    public Page<CommentResponse> getMyComments(Member currentMember, Pageable pageable) {
        Page<Comment> commentPage = commentRepository.findAllByMember(currentMember, pageable);

        return commentPage.map(comment -> CommentResponse.builder()
                .commentId(comment.getCommentId())
                .writerName(comment.getMember().getName())
                .content(comment.getContent())
                .createdAt(comment.getCreatedAt())
                .build());
    }
}