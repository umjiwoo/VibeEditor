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

	// Notion 관련
	INVALID_NOTION_TOKEN("INVALID_NOTION_TOKEN", "유효하지 않은 노션 API 토큰 입니다."),
	DUPLICATED_NOTION_TOKEN("DUPLICATED_NOTION_TOKEN", "이미 등록된 노션 API 토큰입니다."),
	RETRIEVE_NOTION_DATABASE_FAILED("RETRIEVE_NOTION_DATABASE_FAILED", "노션 데이터베이스 조회 중 문제가 발생했습니다."),
	INVALID_NOTION_DATABASE_UUID("INVALID_NOTION_DATABASE_UUID", "유효하지 않은 노션 데이터베이스 UUID입니다."),
	NOTION_UPLOAD_FAILED("NOTION_UPLOAD_FAILED", "노션 게시에 에러가 발생했습니다."),
	DUPLICATED_NOTION_DATABASE_UUID("DUPLICATED_NOTION_DATABASE_UUID", "이미 등록된 노션 데이터베이스입니다."),
	NOTION_DATABASE_NOT_FOUND("NOTION_DATABASE_NOT_FOUND", "노션 데이터베이스 ID를 확인해주세요."),

	// Post 관련
	POST_NOT_FOUND("POST_NOT_FOUND", "포스트가 존재하지 않습니다."),

	// Template 관련
	TEMPLATE_NOT_FOUND("TEMPLATE_NOT_FOUND", "템플릿이 존재하지 않습니다."),

	//암호화 관련
	ENCRYPT_ERROR("ENCRYPT_ERROR", "암호화에 실패했습니다."),
	DECRYPTED_ERROR("DECRYPTED_ERROR", "복호화에 실패했습니다."),

	// Snapshot 관련
	SNAPSHOT_NOT_FOUND("SNAPSHOT_NOT_FOUND", "스냅샷이 존재하지 않습니다."),

	// Prompt, PromptAttach, PromptOption 관련
	PROMPT_NOT_FOUND("PROMPT_NOT_FOUND", "프롬프트가 존재하지 않습니다."),
	PROMPT_ATTACH_NOT_FOUND("PROMPT_ATTACH_NOT_FOUND", "스냅샷&설명 첨부 자료가 존재하지 않습니다."),
	OPTION_NOT_FOUND("OPTION_NOT_FOUND", "옵션이 존재하지 않습니다."),
	OWNER_MISMATCH("OWNER_MISMATCH", "프롬프트 작성자가 아닙니다."),
	TEMPLATE_MISMATCH("TEMPLATE_MISMATCH", "템플릿 ID를 확인해주세요."),
	PROMPT_CONTENT_NULL("PROMPT_CONTENT_NULL", "초안 생성을 위한 내용(유저 코멘트)이 비어있습니다."),
	USER_AI_PROVIDER_NOT_FOUND("USER_AI_PROVIDER_NOT_FOUND", "등록되지 않은 AI 프로바이더입니다."),

	// Post 관련
	POST_GENERATE_FAILED("POST_GENERATE_FAILED", "포스트 생성에 실패했습니다."),

	// Anthropic 관련
	CLAUDE_INVALID_REQUEST_ERROR("INVALID_REQUEST_ERROR", "CLAUDE 요청의 형식이나 내용에 문제가 있습니다."),
	CLAUDE_AUTHENTICATION_ERROR("AUTHENTICATION_ERROR", "CLAUDE API 키에 문제가 있습니다."),
	CLAUDE_PERMISSION_ERROR("PERMISSION_ERROR", "CLAUDE API 키에는 지정된 리소스를 사용할 권한이 없습니다."),
	CLAUDE_NOT_FOUND_ERROR("CLAUDE_NOT_FOUND_ERROR", "CLAUDE에 요청한 리소스를 찾을 수 없습니다."),
	CLAUDE_REQUEST_TOO_LARGE("CLAUDE_REQUEST_TOO_LARGE", "CLAUDE에 요청이 허용된 최대 바이트 수를 초과했습니다."),
	CLAUDE_RATE_LIMIT_ERROR("CLAUDE_RATE_LIMIT_ERROR", "사용자 CLAUDE 계정이 요금 한도에 도달했습니다."),
	CLAUDE_API_ERROR("CLAUDE_API_ERROR", "Anthropic 시스템 내부에 예기치 않은 오류가 발생했습니다."),
	CLAUDE_OVERLOADED_ERROR("CLAUDE_OVERLOADED_ERROR", "Anthropic의 API가 일시적으로 과부하되었습니다."),
	CLAUDE_EMPTY_CONTENT("CLAUDE_EMPTY_CONTENT", "Claude API 응답에 콘텐츠가 없습니다."),
	CLAUDE_JSON_PARSING_ERROR("CLAUDE_JSON_PARSING_ERROR", "응답 데이터 파싱 중 오류가 발생했습니다."),
	CLAUDE_REQUEST_DATA_NOT_FOUND("CLAUDE_REQUEST_DATA_NOT_FOUND", "Claude API 응답에서 요청한 데이터 형식을 찾을 수 없습니다."),
	CLAUDE_OVER_MAX_TOKEN("CLAUDE_OVER_MAX_TOKEN", "최대 토큰 수를 초과했습니다.");

	private final String code;
	private final String message;
}


