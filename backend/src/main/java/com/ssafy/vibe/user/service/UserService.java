package com.ssafy.vibe.user.service;

import com.ssafy.vibe.user.service.dto.UserInfoDTO;

public interface UserService {
	public UserInfoDTO getUserInfo(Long userId);
}
