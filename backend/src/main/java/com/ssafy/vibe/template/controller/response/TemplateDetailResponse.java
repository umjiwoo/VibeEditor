package com.ssafy.vibe.template.controller.response;

import java.util.List;

import com.ssafy.vibe.prompt.service.dto.PromptDTO;
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
					.filter(snapshot -> !snapshot.getIsDeleted())
					.map(SnapshotDTO::from).toList())
			.promptList(template.getPrompts().stream()
				.filter(prompt -> !prompt.getIsDeleted())
				.map(PromptDTO::from).toList())
			.build();
	}
}
