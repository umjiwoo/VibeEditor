package com.ssafy.vibe.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ssafy.vibe.user.domain.AiProviderEntity;

@Repository
public interface AiProviderRepository extends JpaRepository<AiProviderEntity, Long> {
}
