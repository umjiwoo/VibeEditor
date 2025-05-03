package com.ssafy.vibe.user.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ssafy.vibe.auth.jwt.JwtUtil;
import com.ssafy.vibe.common.exception.BadRequestException;
import com.ssafy.vibe.common.exception.ExceptionCode;
import com.ssafy.vibe.user.controller.request.UserLoginRequest;
import com.ssafy.vibe.user.controller.request.UserSignupRequest;
import com.ssafy.vibe.user.domain.ProviderName;
import com.ssafy.vibe.user.domain.UserEntity;
import com.ssafy.vibe.user.repository.UserRepository;
import com.ssafy.vibe.user.service.dto.UserInfoDTO;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
	private final UserRepository userRepository;
	private final JwtUtil jwtUtil; // JwtUtil 주입

	public UserInfoDTO getUserInfo(Long userId) {
		UserEntity userEntity = userRepository.findById(userId)
			.orElseThrow(() -> new BadRequestException(ExceptionCode.USER_NOT_FOUND));

		return UserInfoDTO.from(userEntity);
	}

	@Override
	@Transactional
	public String signup(UserSignupRequest request) {
		if (userRepository.findByEmail(request.getEmail()).isPresent()) {
			throw new RuntimeException("이미 가입된 유저입니다.");
		}
		UserEntity user = UserEntity.createUser(
			request.getUserName(),
			request.getEmail(),
			ProviderName.valueOf(request.getProviderName()),
			request.getProviderUid()
		);
		UserEntity savedUser = userRepository.save(user);
		return jwtUtil.createJwt(savedUser.getId()); // JWT 토큰 발급
	}

	@Override
	@Transactional
	public String login(UserLoginRequest request) {
		UserEntity user = userRepository.findByEmail(request.getEmail())
			.orElseThrow(() -> new RuntimeException("유저 정보가 일치하지 않습니다."));
		user.updateLastLoginAt();
		return jwtUtil.createJwt(user.getId()); // 로그인 시 JWT 토큰 발급
	}

}
