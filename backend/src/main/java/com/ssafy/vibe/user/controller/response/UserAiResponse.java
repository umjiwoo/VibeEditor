package com.ssafy.vibe.user.controller.response;

import com.ssafy.vibe.user.domain.AiBrandName;
import com.ssafy.vibe.user.domain.UserAiProviderEntity;

import lombok.Builder;

@Builder
public record UserAiResponse(
	Long userAiProviderID,
	AiBrandName brand,
	String model,
	Boolean isDefault
) {
	public static UserAiResponse from(UserAiProviderEntity userAiProvider) {
		return UserAiResponse.builder()
			.userAiProviderID(userAiProvider.getId())
			.brand(userAiProvider.getAiProvider().getBrand())
			.model(userAiProvider.getAiProvider().getModel())
			.isDefault(userAiProvider.getIsDefault())
			.build();
	}
}
