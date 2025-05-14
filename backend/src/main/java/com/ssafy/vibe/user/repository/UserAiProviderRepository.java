package com.ssafy.vibe.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ssafy.vibe.user.domain.UserAiProviderEntity;

@Repository
public interface UserAiProviderRepository extends JpaRepository<UserAiProviderEntity, Long> {
}
