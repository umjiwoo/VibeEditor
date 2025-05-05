package com.ssafy.vibe.prompt.service.dto;

import com.ssafy.vibe.prompt.domain.PromptAttachEntity;
import com.ssafy.vibe.prompt.domain.PromptEntity;
import com.ssafy.vibe.snapshot.domain.SnapshotEntity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class PromptAttachDTO {
	private PromptEntity prompt;
	private SnapshotEntity snapshot;
	private String description;

	public PromptAttachEntity toPromptAttachEntity() {
		return PromptAttachEntity.builder()
			.prompt(prompt)
			.snapshot(snapshot)
			.description(description)
			.build();
	}

	public static PromptAttachDTO from(PromptAttachEntity promptAttachEntity) {
		return PromptAttachDTO.builder()
			.prompt(promptAttachEntity.getPrompt())
			.snapshot(promptAttachEntity.getSnapshot())
			.description(promptAttachEntity.getDescription())
			.build();
	}
}
