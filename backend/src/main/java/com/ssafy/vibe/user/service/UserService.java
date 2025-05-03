package com.ssafy.vibe.user.service;

import com.ssafy.vibe.user.controller.request.UserLoginRequest;
import com.ssafy.vibe.user.controller.request.UserSignupRequest;
import com.ssafy.vibe.user.service.dto.UserInfoDTO;

public interface UserService {
	UserInfoDTO getUserInfo(Long userId);

	String signup(UserSignupRequest request);

	String login(UserLoginRequest request);
}
