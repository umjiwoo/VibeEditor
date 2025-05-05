package com.ssafy.vibe.notion.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ssafy.vibe.notion.domain.NotionDatabaseEntity;

public interface NotionDatabaseRepository extends JpaRepository<NotionDatabaseEntity, Long> {
	Optional<NotionDatabaseEntity> findById(Long notionDatabaseId);
}
