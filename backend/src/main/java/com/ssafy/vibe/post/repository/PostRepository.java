package com.ssafy.vibe.post.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ssafy.vibe.post.domain.PostEntity;

public interface PostRepository extends JpaRepository<PostEntity, Long> {
}
