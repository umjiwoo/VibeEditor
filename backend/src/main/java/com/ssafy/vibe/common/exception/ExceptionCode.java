package com.ssafy.vibe.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum ExceptionCode {

	// User 관련
	USER_NOT_FOUND("USER_NOT_FOUND", "유저가 존재하지 않습니다."),
	DUPLICATE_EMAIL("DUPLICATE_EMAIL", "이미 존재하는 이메일입니다.");

	private final String code;
	private final String message;
}


