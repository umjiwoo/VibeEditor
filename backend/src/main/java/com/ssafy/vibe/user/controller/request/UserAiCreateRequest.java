package com.ssafy.vibe.user.controller.request;

import com.ssafy.vibe.user.domain.AiBrandName;

public record UserAiCreateRequest(
	AiBrandName brand,
	String apiKey
) {
}
