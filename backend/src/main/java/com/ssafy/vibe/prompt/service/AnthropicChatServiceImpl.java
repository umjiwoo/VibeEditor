package com.ssafy.vibe.prompt.service;

import static com.ssafy.vibe.common.exception.ExceptionCode.*;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.ssafy.vibe.common.exception.ExternalAPIException;
import com.ssafy.vibe.prompt.service.dto.AiChatInputDTO;
import com.ssafy.vibe.prompt.util.AnthropicUtil;
import com.ssafy.vibe.prompt.util.PromptUtil;
import com.ssafy.vibe.user.domain.AiBrandName;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Service
public class AnthropicChatServiceImpl implements AiChatService {

	private final AnthropicUtil anthropicUtil;
	private final PromptUtil promptUtil;

	@Value("${spring.ai.anthropic.base-url}")
	private String baseUrl;
	@Value("${spring.ai.anthropic.api-key}")
	private String defaultApiKey;

	@Override
	public AiBrandName getBrand() {
		return AiBrandName.Anthropic;
	}

	@Override
	public String[] generateChat(AiChatInputDTO input) {
		return new String[0];
	}

	@Override
	public void validateApiKey(String apiKey) {
		try {
			WebClient webClient = WebClient.builder()
				.baseUrl(baseUrl)
				.defaultHeader("x-api-key", apiKey)
				.defaultHeader("anthropic-version", "2023-06-01")
				.build();

			HttpStatusCode statusCode = webClient.get()
				.uri("/models")
				.retrieve()
				.toBodilessEntity()
				.map(ResponseEntity::getStatusCode)
				.block();
			if (statusCode == null || !statusCode.is2xxSuccessful()) {
				log.error("사용자 Claude API 등록 중 오류: 잘못된 API key나 요청");
				throw new ExternalAPIException(CLAUDE_AUTHENTICATION_ERROR);
			}
		} catch (Exception e) {
			log.error("사용자 Claude API 등록 중 오류: {}", e.getMessage());
			throw new ExternalAPIException(CLAUDE_AUTHENTICATION_ERROR);
		}
	}
}
