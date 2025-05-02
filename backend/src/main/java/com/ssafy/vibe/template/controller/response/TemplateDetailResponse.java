package com.ssafy.vibe.template.controller.response;

import java.util.List;

import com.ssafy.vibe.prompt.domain.PromptEntity;
import com.ssafy.vibe.prompt.service.dto.PromptDTO;
import com.ssafy.vibe.snapshot.domain.SnapshotEntity;
import com.ssafy.vibe.snapshot.service.dto.SnapshotDTO;
import com.ssafy.vibe.template.domain.TemplateEntity;

import lombok.Builder;

@Builder
public record TemplateDetailResponse(
	String templateName,
	List<SnapshotDTO> snapshotList,
	List<PromptDTO> promptList
) {
	public static TemplateDetailResponse from(TemplateEntity template) {
		return TemplateDetailResponse
			.builder()
			.templateName(template.getTemplateName())
			.snapshotList(
				template.getSnapshots().stream()
					.filter(SnapshotEntity::getIsActive)
					.map(SnapshotDTO::from).toList())
			.promptList(template.getPrompts().stream()
				.filter(PromptEntity::getIsActive)
				.map(PromptDTO::from).toList())
			.build();
	}
}
