package com.ssafy.vibe.user.controller.response;

import java.time.ZonedDateTime;

import com.ssafy.vibe.user.service.dto.UserInfoDTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Builder
@AllArgsConstructor
@Getter
public class UserInfoResponse {
	private Boolean notion_active;
	private ZonedDateTime lastLoginAt;
	private ZonedDateTime updatedAt;
	private ZonedDateTime createdAt;

	public static UserInfoResponse from(UserInfoDTO userInfo) {
		return UserInfoResponse.builder()
			.notion_active(userInfo.getNotionActive())
			.lastLoginAt(userInfo.getLastLoginAt())
			.updatedAt(userInfo.getUpdatedAt())
			.createdAt(userInfo.getCreatedAt())
			.build();
	}
}
