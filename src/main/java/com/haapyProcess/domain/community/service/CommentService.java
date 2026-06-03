package com.haapyProcess.domain.community.service;

import com.haapyProcess.domain.community.entity.Comment;
import com.haapyProcess.domain.community.entity.Post;
import com.haapyProcess.domain.community.repository.CommentRepository;
import com.haapyProcess.domain.community.repository.PostRepository;
import com.haapyProcess.domain.member.entity.Member;
import lombok.RequiredArgsConstructor;
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
}