package com.ssafy.vibe.prompt.controller.request;

import com.ssafy.vibe.prompt.service.command.PromptCommand;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class PromptRequest {

	@NotBlank(message = "포스트 타입을 입력해주세요 (CS 정리 or 트러블슈팅)")
	private String postType;

	@NotBlank(message = "스냅샷(코드, 로그 등)을 입력해주세요.")
	private String snapshot;

	@NotBlank(message = "스냅샷 설명을 입력해주세요.")
	private String snapshotDescription;

	@NotBlank(message = "유저 코멘트를 입력해주세요.")
	private String userComment; // 사용자 코멘트는 필수 아닐 수 있음

	@NotBlank(message = "옵션(이모지, 말투)을 입력해주세요.")
	private String option; // 예: "이모지 O, ~습니다 체"

	public PromptCommand toCommand() {
		return new PromptCommand(
			this.postType,
			this.snapshot,
			this.snapshotDescription,
			this.userComment,
			this.option
		);
	}
}
