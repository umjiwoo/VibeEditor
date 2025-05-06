package com.ssafy.vibe.prompt.controller.response;

import com.ssafy.vibe.prompt.domain.PromptEntity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class ParentPromptResponse {
	private Long parentPromptId;
	private String parentPromptName;

	public static ParentPromptResponse fromPromptEntity(PromptEntity parentPromptEntity) {
		return ParentPromptResponse.builder()
			.parentPromptId(parentPromptEntity.getId())
			.parentPromptName(parentPromptEntity.getPromptName())
			.build();
	}
}
