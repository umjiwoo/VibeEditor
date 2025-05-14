package com.ssafy.vibe.prompt.service.dto;

import com.ssafy.vibe.prompt.domain.PromptEntity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Builder
@AllArgsConstructor
@Getter
public class RetrievePromptDTO {
	private Long parentPromptId;
	private Long templateId;
	private Long userId;
	private String promptName;
	private String postType;
	private String comment;
	private Long notionDatabaseId;
	private Long userAIProviderId;

	public static RetrievePromptDTO fromEntity(PromptEntity promptEntity) {
		return RetrievePromptDTO.builder()
			.parentPromptId(
				promptEntity.getParentPrompt() != null ?
					promptEntity.getParentPrompt().getId() : null)
			.templateId(promptEntity.getTemplate().getId())
			.userId(promptEntity.getUser().getId())
			.promptName(promptEntity.getPromptName())
			.postType(promptEntity.getPostType().toString())
			.comment(promptEntity.getComment())
			.notionDatabaseId(promptEntity.getNotionDatabase().getId())
			.userAIProviderId(promptEntity.getUserAiProvider().getId())
			.build();
	}
}
