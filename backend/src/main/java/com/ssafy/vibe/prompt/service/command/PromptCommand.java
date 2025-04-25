package com.ssafy.vibe.prompt.service.command;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class PromptCommand {
	private String postType;
	private String snapshot;
	private String snapshotDescription;
	private String userComment;
	private String option;
}
