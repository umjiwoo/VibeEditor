package com.ssafy.vibe.prompt.controller.request;

import java.util.List;
import java.util.stream.Collectors;

import com.ssafy.vibe.prompt.service.command.PromptSaveCommand;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PromptSaveRequest {
	private Long parentPromptId;

	@NotNull(message = "템플릿 번호를 입력해주세요.")
	private Long templateId;

	private String promptName;

	private String postType;

	private String comment; // 사용자 코멘트는 필수 아닐 수 있음

	private List<SnapshotRequest> promptAttachList;

	private List<Long> promptOptionList; // 예: "이모지 O, ~습니다 체"

	private Long notionDatabaseId;

	public PromptSaveCommand toCommand() {
		return PromptSaveCommand.builder()
			.parentPromptId(this.parentPromptId)
			.templateId(this.templateId)
			.promptName(this.promptName)
			.postType(this.postType)
			.comment(this.comment)
			.promptAttachList(this.promptAttachList.stream()
				.map(SnapshotRequest::toCommand)
				.collect(Collectors.toList()))
			.promptOptionList(this.promptOptionList)
			.notionDatabaseId(this.notionDatabaseId)
			.build();
	}
}