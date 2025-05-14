package com.ssafy.vibe.user.controller.response;

public record UserAiResponse(
	Long userAIProviderID,
	String brand,
	String model,
	Boolean isDefault
) {
}
