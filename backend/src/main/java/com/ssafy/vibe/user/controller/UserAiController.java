package com.ssafy.vibe.user.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.ssafy.vibe.auth.domain.UserPrincipal;
import com.ssafy.vibe.common.schema.BaseResponse;
import com.ssafy.vibe.user.controller.request.UserAiCreateRequest;
import com.ssafy.vibe.user.controller.request.UserAiUpdateRequest;
import com.ssafy.vibe.user.controller.response.UserAiResponse;
import com.ssafy.vibe.user.service.UserAiProviderService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/v1/user/ai")
@RequiredArgsConstructor
public class UserAiController {

	private final UserAiProviderService userAiProviderService;

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public ResponseEntity<?> registerUserAPIKey(
		@AuthenticationPrincipal UserPrincipal userPrincipal,
		@Valid UserAiCreateRequest request
	) {
		userAiProviderService.registerUserAPIKey(userPrincipal.getUserId(), request);
		return ResponseEntity.ok(BaseResponse.success(null));
	}

	@PutMapping
	public ResponseEntity<?> updateUserAPIKey(
		@AuthenticationPrincipal UserPrincipal userPrincipal,
		@Valid UserAiUpdateRequest request
	) {
		userAiProviderService.updateUserAPIKey(userPrincipal.getUserId(), request);
		return ResponseEntity.ok(BaseResponse.success(null));
	}

	@GetMapping
	public ResponseEntity<?> getAiProviderList(
		@AuthenticationPrincipal UserPrincipal userPrincipal
	) {
		List<UserAiResponse> response = userAiProviderService.getAiProviderList(
			userPrincipal.getUserId()
		);
		return ResponseEntity.ok(BaseResponse.success(response));
	}
}
