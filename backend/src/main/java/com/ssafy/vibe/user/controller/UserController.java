package com.ssafy.vibe.user.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ssafy.vibe.auth.domain.UserPrincipal;
import com.ssafy.vibe.user.controller.request.UserLoginRequest;
import com.ssafy.vibe.user.controller.request.UserSignupRequest;
import com.ssafy.vibe.user.controller.response.UserInfoResponse;
import com.ssafy.vibe.user.service.UserService;
import com.ssafy.vibe.user.service.dto.UserInfoDTO;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/v1/user")
@RequiredArgsConstructor
public class UserController {
	private final UserService userService;

	@GetMapping
	public ResponseEntity<UserInfoResponse> getCurrentUser(@AuthenticationPrincipal UserPrincipal userPrincipal) {
		UserInfoDTO userInfoDto = userService.getUserInfo(userPrincipal.getUserId());
		UserInfoResponse userInfoResponse = UserInfoResponse.from(userInfoDto);

		return ResponseEntity.ok(userInfoResponse);
	}

	@PostMapping("/test/signup")
	public ResponseEntity<String> signup(
		@RequestBody UserSignupRequest request
	) {
		String jwtToken = userService.signup(request);
		return ResponseEntity.ok(jwtToken);
	}

	@PostMapping("/test/login")
	public ResponseEntity<String> login(@RequestBody UserLoginRequest request) {
		String jwtToken = userService.login(request);
		return ResponseEntity.ok(jwtToken); // JWT 토큰 반환
	}

}
