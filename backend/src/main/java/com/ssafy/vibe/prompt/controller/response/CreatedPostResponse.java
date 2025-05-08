package com.ssafy.vibe.prompt.controller.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class CreatedPostResponse {
	private String postTitle;
	private String postContent;

	public static CreatedPostResponse from(String postTitle, String postContent) {
		return CreatedPostResponse.builder()
			.postTitle(postTitle)
			.postContent(postContent)
			.build();
	}
}
