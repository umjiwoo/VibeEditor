package com.ssafy.vibe.prompt.service.dto;

public record AiChatInputDTO(
	String model,
	Double temperature,
	Integer maxTokens,
	String apiKey,
	String systemPrompt,
	String userPrompt
) {
}
