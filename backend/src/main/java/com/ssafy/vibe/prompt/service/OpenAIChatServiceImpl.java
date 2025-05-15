package com.ssafy.vibe.prompt.service;

import static com.ssafy.vibe.common.exception.ExceptionCode.*;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.ssafy.vibe.common.exception.ExternalAPIException;
import com.ssafy.vibe.prompt.service.dto.AiChatInputDTO;
import com.ssafy.vibe.user.domain.AiBrandName;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Service
public class OpenAIChatServiceImpl implements AiChatService {

	@Value("${spring.ai.openai.base-url}")
	private String baseUrl;

	@Override
	public AiBrandName getBrand() {
		return AiBrandName.OpenAI;
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
				.defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
				.build();

			HttpStatusCode statusCode = webClient.get()
				.uri("/models")
				.retrieve()
				.toBodilessEntity()
				.map(ResponseEntity::getStatusCode)
				.block();
			if (statusCode == null || !statusCode.is2xxSuccessful()) {
				log.error("사용자 OpenAI API 등록 중 오류: 잘못된 API key나 요청");
				throw new ExternalAPIException(OPENAI_AUTHENTICATION_ERROR);
			}
		} catch (Exception e) {
			log.error("사용자 OpenAI API 등록 중 오류: {}", e.getMessage());
			throw new ExternalAPIException(OPENAI_AUTHENTICATION_ERROR);
		}
	}
}
