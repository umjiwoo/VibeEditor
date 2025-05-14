package com.ssafy.vibe.user.controller.response;

import com.ssafy.vibe.user.domain.AiBrandName;

public record UserAiResponse(
	Long userAIProviderID,
	AiBrandName brand,
	String model,
	Boolean isDefault
) {
}
