package com.ssafy.vibe.post.controller.request;

import com.ssafy.vibe.post.service.command.NotionPostCommand;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotionPostRequest {
	private Long notionId;
	private String uploadTitle;
	private String uploadContent;

	public NotionPostCommand toCommand() {
		return NotionPostCommand.builder()
			.notionId(this.notionId)
			.title(this.uploadTitle)
			.content(this.uploadContent)
			.build();
	}
}