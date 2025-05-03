package com.ssafy.vibe.notion.service;

import org.springframework.stereotype.Service;

import com.ssafy.vibe.common.exception.BadRequestException;
import com.ssafy.vibe.common.exception.ExceptionCode;
import com.ssafy.vibe.common.util.Aes256Util;
import com.ssafy.vibe.notion.client.NotionApiClient;
import com.ssafy.vibe.notion.service.command.NotionConnectInfoCommand;
import com.ssafy.vibe.user.domain.UserEntity;
import com.ssafy.vibe.user.repository.UserRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NotionServiceImpl implements NotionService {
	private final Aes256Util encryptor;
	private final UserRepository userRepository;
	private final NotionApiClient notionApiClient;

	@Override
	@Transactional
	public void saveNotionKey(NotionConnectInfoCommand command) {
		UserEntity user = userRepository.findById(command.getUserId())
			.orElseThrow(() -> new BadRequestException(ExceptionCode.USER_NOT_FOUND));

		boolean response = notionApiClient.validateNotionToken(
			command.getNotionSecretKey());

		if (!response) {
			throw new BadRequestException(ExceptionCode.INVALID_NOTION_TOKEN);
		}

		String encryptedKey = encryptor.encrypt(command.getNotionSecretKey());
		user.updateSecretKey(encryptedKey);
		user.updateNotionActive(true);

		userRepository.save(user);
	}
}
