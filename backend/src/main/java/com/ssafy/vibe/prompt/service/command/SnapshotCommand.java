package com.ssafy.vibe.prompt.service.command;

import com.ssafy.vibe.prompt.domain.PromptEntity;
import com.ssafy.vibe.prompt.service.dto.PromptAttachDTO;
import com.ssafy.vibe.snapshot.domain.SnapshotEntity;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class SnapshotCommand {
	private Long snapshotId;
	private String description;

	public PromptAttachDTO toPromptAttachDTO(
		PromptEntity prompt,
		SnapshotEntity snapshot) {
		return PromptAttachDTO.builder()
			.prompt(prompt)
			.snapshot(snapshot)
			.description(this.getDescription())
			.build();
	}
}
