package com.haapyProcess.domain.community.repository;

import com.haapyProcess.domain.community.entity.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PostRepository extends JpaRepository<Post, Long> {

    @EntityGraph(attributePaths = {"member"})
    Page<Post> findAll(Pageable pageable);

    @EntityGraph(attributePaths = {"member"})
    @Query("SELECT p FROM Post p WHERE p.postConditions IS EMPTY")
    Page<Post> findFreePosts(Pageable pageable);

    @EntityGraph(attributePaths = {"member"})
    @Query("SELECT DISTINCT p FROM Post p JOIN p.postConditions pc WHERE pc.condition.conditionId IN :conditionIds")
    Page<Post> findPostsByConditionIds(@Param("conditionIds") List<Long> conditionIds, Pageable pageable);
}