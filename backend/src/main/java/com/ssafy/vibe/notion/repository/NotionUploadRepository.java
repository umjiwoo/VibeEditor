package com.ssafy.vibe.notion.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ssafy.vibe.notion.domain.NotionUploadEntity;

public interface NotionUploadRepository extends JpaRepository<NotionUploadEntity, Long> {
}
