package com.ssafy.vibe.prompt.controller.response;

import java.util.List;

import com.ssafy.vibe.prompt.service.dto.RetrievePromptDTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class RetrievePromptResponse {
	private ParentPromptResponse parentPrompt;
	private Long templateId;
	private List<PromptAttachRespnose> promptAttachList;
	private String promptName;
	private String postType;
	private String comment;
	private List<Long> promptOptionList;
	private Long notionDatabaseId;

	public static RetrievePromptResponse from(
		RetrievePromptDTO promptDTO,
		List<PromptAttachRespnose> promptAttachList,
		List<Long> promptOptionIds
	) {
		return RetrievePromptResponse.builder()
			.parentPrompt(ParentPromptResponse.from(promptDTO))
			.templateId(promptDTO.getTemplateId())
			.promptAttachList(promptAttachList)
			.promptName(promptDTO.getPromptName())
			.postType(promptDTO.getPostType())
			.comment(promptDTO.getComment())
			.promptOptionList(promptOptionIds)
			.notionDatabaseId(promptDTO.getNotionDatabaseId())
			.build();
	}
}
