package com.ssafy.vibe.user.controller.request;

import com.ssafy.vibe.user.domain.AiBrandName;

public record UserAiUpdateRequest(
	AiBrandName brand,
	String apiKey
) {
}
