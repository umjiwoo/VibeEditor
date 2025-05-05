package com.ssafy.vibe.template.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ssafy.vibe.template.domain.TemplateEntity;

@Repository
public interface TemplateRepository extends JpaRepository<TemplateEntity, Long> {
	Optional<TemplateEntity> findById(Long templateId);
}
