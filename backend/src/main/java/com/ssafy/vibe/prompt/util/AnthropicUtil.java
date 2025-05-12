package com.ssafy.vibe.prompt.util;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ssafy.vibe.prompt.domain.AnthropicErrorResponse;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class AnthropicUtil {
	private static final ObjectMapper mapper = new ObjectMapper();

	public String parseAnthropicErrorMessage(String responseBody) {
		try {
			AnthropicErrorResponse errorResponse = mapper.readValue(responseBody, AnthropicErrorResponse.class);
			log.error("Anthropic 오류 응답 파싱 실패 util: {}", errorResponse.getError().getMessage());
			return "";
			// return errorResponse.getError().getMessage();
		} catch (JsonProcessingException e) {
			log.error("Anthropic 오류 응답 파싱 실패: {}", e.getMessage());
			return "알 수 없는 오류가 발생했습니다.";
		}
	}
}
