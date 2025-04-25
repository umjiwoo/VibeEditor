package com.ssafy.vibe.user.domain;

import java.time.ZonedDateTime;

import com.ssafy.vibe.common.domain.BaseEntity;
import com.ssafy.vibe.user.service.dto.UserDTO;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
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
	private Boolean notionActive;

	@Column(name = "last_login_at")
	private ZonedDateTime lastLoginAt;

	@Builder
	private UserEntity(String userName, String email, ProviderName providerName, String providerUid) {
		this.userName = userName;
		this.email = email;
		this.providerName = providerName;
		this.providerUid = providerUid;
	}

	public static UserEntity createUser(String userName, String email, ProviderName providerName, String providerUid) {
		return UserEntity.builder()
			.userName(userName)
			.email(email)
			.providerName(providerName)
			.providerUid(providerUid)
			.build();
	}

	public static UserEntity from(UserDTO userDto) {
		return UserEntity.builder()
			.userName(userDto.getUserName())
			.email(userDto.getEmail())
			.providerName(userDto.getProviderName())
			.providerUid(userDto.getProviderUid())
			.notionActive(false)
			.build();
	}
}