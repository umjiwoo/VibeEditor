package com.ssafy.vibe.prompt.controller.request;

import java.util.List;
import java.util.stream.Collectors;

import com.ssafy.vibe.prompt.service.command.PromptSaveCommand;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

@Data
public class PromptSaveRequest {
	private Long parentPromptId;

	@NotBlank(message = "템플릿 번호를 입력해주세요.")
	private Long templateId;

	@NotBlank(message = "프롬프트 제목을 입력해주세요.")
	private String promptName;

	@NotBlank(message = "포스트 타입을 입력해주세요 (CS 정리 or 트러블슈팅)")
	private String postType;

	@NotBlank(message = "유저 코멘트를 입력해주세요.")
	private String comment; // 사용자 코멘트는 필수 아닐 수 있음

	@Valid
	@NotEmpty(message = "스냅샷 리스트를 입력해주세요.")
	private List<SnapshotRequest> promptAttachList;

	@NotBlank(message = "옵션(이모지, 말투)을 입력해주세요.")
	private Long[] promptOptionList; // 예: "이모지 O, ~습니다 체"

	@NotBlank(message = "노션 데이터베이스 id를 입력해주세요.")
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