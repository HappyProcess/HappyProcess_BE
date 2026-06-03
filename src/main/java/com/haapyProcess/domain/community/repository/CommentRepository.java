package com.haapyProcess.domain.community.repository;

import com.haapyProcess.domain.community.entity.Comment;
import com.haapyProcess.domain.community.entity.Post;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    @EntityGraph(attributePaths = {"member"})
    List<Comment> findAllByPostOrderByCreatedAtAsc(Post post);
}