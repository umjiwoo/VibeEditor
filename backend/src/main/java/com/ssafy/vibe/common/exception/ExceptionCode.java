package com.ssafy.vibe.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum ExceptionCode {
	// Auth 관련
	UNSUPPORTED_PROVIDER("UNSUPPORTED_PROVIDER", "지원하지 않는 프로바이더 입니다."),
	INVALID_TOKEN("INVALID_TOKEN", "유효하지 않은 토큰입니다."),
	AUTHENTICATION_FAILED("AUTHENTICATION_FAILED", "인증에 실패했습니다."),

	// User 관련
	USER_NOT_FOUND("USER_NOT_FOUND", "유저가 존재하지 않습니다."),
	DUPLICATE_EMAIL("DUPLICATE_EMAIL", "이미 존재하는 이메일입니다."),

	// Template 관련
	TEMPLATE_NOT_FOUND("TEMPLATE_NOT_FOUND", "템플릿이 존재하지 않습니다."),

	// Snapshot 관련
	SNAPSHOT_NOT_FOUND("SNAPSHOT_NOT_FOUND", "스냅샷이 존재하지 않습니다."),

	// Prompt, PromptOption 관련
	PROMPT_NOT_FOUND("PROMPT_NOT_FOUND", "프롬프트가 존재하지 않습니다."),
	OPTION_NOT_FOUND("OPTION_NOT_FOUND", "옵션이 존재하지 않습니다."),
	OWNER_MISMATCH("OWNER_MISMATCH", "프롬프트 작성자가 아닙니다."),
	TEMPLATE_MISMATCH("TEMPLATE_MISMATCH", "템플릿 ID를 확인해주세요."),

	// NotionDatabase 관련
	NOTION_DATABASE_NOT_FOUND("NOTION_DATABASE_NOT_FOUND", "노션 데이터베이스 ID를 확인해주세요.");

	private final String code;
	private final String message;
}


