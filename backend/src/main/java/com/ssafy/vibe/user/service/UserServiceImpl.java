package com.ssafy.vibe.user.service;

import org.springframework.stereotype.Service;

import com.ssafy.vibe.common.exception.BadRequestException;
import com.ssafy.vibe.common.exception.ExceptionCode;
import com.ssafy.vibe.user.domain.UserEntity;
import com.ssafy.vibe.user.repository.UserRepository;
import com.ssafy.vibe.user.service.dto.UserInfoDTO;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
	private final UserRepository userRepository;

	public UserInfoDTO getUserInfo(Long userId) {
		UserEntity userEntity = userRepository.findById(userId)
			.orElseThrow(() -> new BadRequestException(ExceptionCode.USER_NOT_FOUND));

		return UserInfoDTO.from(userEntity);
	}
}
