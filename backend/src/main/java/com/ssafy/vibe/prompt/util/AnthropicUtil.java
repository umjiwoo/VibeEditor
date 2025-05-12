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
			return errorResponse.getError().getMessage();
		} catch (JsonProcessingException e) {
			return e.getMessage();
		}
	}
}
