package com.ssafy.vibe.snapshot.controller.request;

import com.ssafy.vibe.snapshot.domain.SnapshotType;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateSnapshotRequest(
	@NotBlank(message = "{required}")
	Long templateId,

	@NotBlank(message = "{required}")
	@Size(max = 30, message = "{max.length}")
	String snapshotName,

	@NotBlank(message = "{required}")
	SnapshotType snapshotType,

	@NotBlank(message = "{required}")
	String content
) {
}
