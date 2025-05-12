package com.ssafy.vibe.prompt.domain;

import lombok.Getter;

@Getter
public class AnthropicErrorResponse {
	private ErrorDetail error;

	@Getter
	public static class ErrorDetail {
		private String type;
		private String message;
		private String name;
	}
}