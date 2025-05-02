package com.ssafy.vibe.post.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.ssafy.vibe.post.domain.PostEntity;

public interface PostRepository extends JpaRepository<PostEntity, Long> {

	@Query("""
		select p
		from PostEntity p
			join fetch p.prompt pr
			join fetch pr.notionDatabase
		where p.id = :id
		""")
	Optional<PostEntity> findByIdWithPromptAndNotionDatabase(@Param("id") Long id);

}
