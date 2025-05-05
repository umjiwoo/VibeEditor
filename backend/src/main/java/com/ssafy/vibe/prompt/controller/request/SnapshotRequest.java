package com.ssafy.vibe.prompt.controller.request;

import com.ssafy.vibe.prompt.service.command.SnapshotCommand;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SnapshotRequest {
	@NotBlank(message = "스냅샷 번호를 입력해주세요.")
	private Long snapshotId;
	private String description;

	public SnapshotCommand toCommand() {
		return new SnapshotCommand(
			this.snapshotId,
			this.description
		);
	}
}
