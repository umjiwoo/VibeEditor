package com.ssafy.vibe.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ssafy.vibe.user.domain.UserAiProviderEntity;

public interface UserAIProviderRepository extends JpaRepository<UserAiProviderEntity, Long> {
}
