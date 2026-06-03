package com.haapyProcess.domain.community.repository;

import com.haapyProcess.domain.community.entity.Comment;
import com.haapyProcess.domain.community.entity.Post;
import com.haapyProcess.domain.member.entity.Member;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    @EntityGraph(attributePaths = {"member"})
    List<Comment> findAllByPostOrderByCreatedAtAsc(Post post);


    @EntityGraph(attributePaths = {"member", "post"})
    Page<Comment> findAllByMember(Member member, Pageable pageable);
}