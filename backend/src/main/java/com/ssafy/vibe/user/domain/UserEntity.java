package com.ssafy.vibe.user.domain;

import java.time.ZonedDateTime;

import com.ssafy.vibe.common.domain.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "users")
public class UserEntity extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "user_id")
	private Long id;

	@Column(name = "user_name", nullable = false)
	private String userName;

	@Column(name = "email", nullable = false)
	private String email;

	@Column(name = "provider_name", nullable = false)
	@Enumerated(EnumType.STRING)
	private ProviderName providerName;

	@Column(name = "provider_uid", nullable = false)
	private String providerUid;

	@Column(name = "notion_api")
	private String notionApi;

	@Column(name = "notion_active")
	private boolean notionActive;

	@Column(name = "last_login_at")
	private ZonedDateTime lastLoginAt;
}
