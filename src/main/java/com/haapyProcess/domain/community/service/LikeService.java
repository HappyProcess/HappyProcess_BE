package com.haapyProcess.domain.community.service;

import com.haapyProcess.domain.community.entity.Post;
import com.haapyProcess.domain.community.entity.PostLike;
import com.haapyProcess.domain.community.repository.PostLikeRepository;
import com.haapyProcess.domain.community.repository.PostRepository;
import com.haapyProcess.domain.member.entity.Member;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class LikeService {

    private final PostLikeRepository postLikeRepository;
    private final PostRepository postRepository;

    /**
     * 공감 누르기 / 취소하기 토글(Toggle)
     * 반환값: 누른 후의 최종 상태 (true = 공감됨, false = 공감 취소됨)
     */
    @Transactional
    public boolean toggleLike(Member currentMember, Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("해당 게시글을 찾을 수 없습니다."));

        Optional<PostLike> existingLike = postLikeRepository.findByPostAndMember(post, currentMember);

        if (existingLike.isPresent()) {
            postLikeRepository.delete(existingLike.get());
            post.removeLikeCount();
            return false;
        } else {
            PostLike newLike = PostLike.builder()
                    .post(post)
                    .member(currentMember)
                    .build();
            postLikeRepository.save(newLike);
            post.addLikeCount();
            return true;
        }
    }
}