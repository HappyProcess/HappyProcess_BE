package com.haapyProcess.domain.community.repository;

import com.haapyProcess.domain.community.entity.Post;
import com.haapyProcess.domain.community.entity.PostLike;
import com.haapyProcess.domain.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PostLikeRepository extends JpaRepository<PostLike, Long> {

    boolean existsByPostAndMember(Post post, Member member);

    Optional<PostLike> findByPostAndMember(Post post, Member member);
}
