package com.ssafy.vibe.prompt.controller.request;

import com.ssafy.vibe.prompt.service.command.GeneratePostCommand;

import lombok.Data;

@Data
public class GeneratePostRequest {
	private Long promptId;

	public GeneratePostCommand toGeneratePostCommand() {
		return GeneratePostCommand.builder()
			.promptId(this.promptId)
			.build();
	}
}
