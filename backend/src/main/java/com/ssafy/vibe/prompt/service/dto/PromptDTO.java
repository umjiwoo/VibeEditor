package com.ssafy.vibe.prompt.service.dto;

import com.ssafy.vibe.notion.domain.NotionDatabaseEntity;
import com.ssafy.vibe.post.domain.PostType;
import com.ssafy.vibe.prompt.domain.PromptEntity;
import com.ssafy.vibe.template.domain.TemplateEntity;
import com.ssafy.vibe.user.domain.UserEntity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Builder
@AllArgsConstructor
@Getter
public class PromptDTO {
	private PromptEntity parentPrompt;
	private TemplateEntity template;
	private UserEntity user;
	private String promptName;
	private String postType;
	private String comment;
	private NotionDatabaseEntity notionDatabase;

	public PromptEntity toPromptEntity() {
		return PromptEntity.builder()
			.parentPrompt(this.parentPrompt)
			.template(this.template)
			.user(this.user)
			.promptName(this.promptName)
			.postType(PostType.valueOf(this.postType))
			.comment(this.comment)
			.notionDatabase(this.notionDatabase)
			.build();
	}

	public static PromptDTO fromPromptEntity(PromptEntity promptEntity) {
		return PromptDTO.builder()
			.parentPrompt(promptEntity.getParentPrompt())
			.template(promptEntity.getTemplate())
			.user(promptEntity.getUser())
			.promptName(promptEntity.getPromptName())
			.postType(promptEntity.getPostType().toString())
			.comment(promptEntity.getComment())
			.build();
	}
}
