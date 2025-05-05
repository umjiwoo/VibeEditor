package com.ssafy.vibe.prompt.controller.response;

import com.ssafy.vibe.prompt.service.dto.PromptAttachDTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class PromptAttachListRespnose {
	private Long attachId;
	private Long snapshotId;
	private String description;

	public static PromptAttachListRespnose from(Long attachId, PromptAttachDTO promptAttachDTO) {
		return PromptAttachListRespnose.builder()
			.attachId(attachId)
			.snapshotId(promptAttachDTO.getSnapshot().getId())
			.description(promptAttachDTO.getDescription())
			.build();
	}
}
