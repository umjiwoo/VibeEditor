package com.ssafy.vibe.notion.service;

import org.springframework.stereotype.Service;

import com.ssafy.vibe.common.exception.BadRequestException;
import com.ssafy.vibe.common.exception.ExceptionCode;
import com.ssafy.vibe.common.util.Aes256Util;
import com.ssafy.vibe.notion.service.dto.NotionConnectInfoDto;
import com.ssafy.vibe.user.domain.UserEntity;
import com.ssafy.vibe.user.repository.UserRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NotionServiceImpl implements NotionService {
	private final Aes256Util encryptor;
	private final UserRepository userRepository;

	@Override
	@Transactional
	public void saveNotionKey(NotionConnectInfoDto info) {
		UserEntity user = userRepository.findById(info.getUserId())
			.orElseThrow(() -> new BadRequestException(ExceptionCode.USER_NOT_FOUND));
		String encryptedKey = encryptor.encrypt(info.getNotionApiKey());
		user.setApiKey(encryptedKey);
		userRepository.save(user);
	}
}
