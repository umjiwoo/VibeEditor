package com.ssafy.vibe.prompt.controller.response;

import java.util.List;

import com.ssafy.vibe.prompt.service.dto.PromptDTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class SavedPromptResponse {
	private Long parentPromptId;
	private Long templateId;
	private List<PromptAttachListRespnose> promptAttachList;
	private String promptName;
	private String postType;
	private String comment;
	private Long[] promptOptionList;
	private Long notionDatabaseId;

	public static SavedPromptResponse from(
		PromptDTO promptDTO,
		List<PromptAttachListRespnose> promptAttachList,
		Long[] promptOptionIds
	) {
		return SavedPromptResponse.builder()
			.parentPromptId(promptDTO.getParentPrompt().getId())
			.templateId(promptDTO.getTemplate().getId())
			.promptAttachList(promptAttachList)
			.promptName(promptDTO.getPromptName())
			.postType(promptDTO.getPostType())
			.comment(promptDTO.getComment())
			.promptOptionList(promptOptionIds)
			.notionDatabaseId(promptDTO.getNotionDatabase().getId())
			.build();
	}
}
