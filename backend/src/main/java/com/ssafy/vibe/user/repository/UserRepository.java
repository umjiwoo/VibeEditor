package com.ssafy.vibe.user.repository;

import com.ssafy.vibe.user.domain.ProviderName;
import org.springframework.data.jpa.repository.JpaRepository;
import com.ssafy.vibe.user.domain.UserEntity;

import java.util.Optional;

public interface UserRepository extends JpaRepository<UserEntity, Long> {
    Optional<UserEntity> findByProviderNameAndProviderUid(ProviderName providerName, String providerUid);
}
