package com.ssafy.vibe.notion.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ssafy.vibe.common.exception.BadRequestException;
import com.ssafy.vibe.common.exception.ExceptionCode;
import com.ssafy.vibe.common.util.Aes256Util;
import com.ssafy.vibe.notion.client.NotionApiClient;
import com.ssafy.vibe.notion.domain.NotionDatabaseEntity;
import com.ssafy.vibe.notion.repository.NotionDatabaseRepository;
import com.ssafy.vibe.notion.service.command.NotionConnectInfoCommand;
import com.ssafy.vibe.notion.service.command.NotionRegisterDatabaseCommand;
import com.ssafy.vibe.user.domain.UserEntity;
import com.ssafy.vibe.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NotionServiceImpl implements NotionService {
	private final Aes256Util encryptor;
	private final UserRepository userRepository;
	private final NotionApiClient notionApiClient;
	private final NotionDatabaseRepository notionDatabaseRepository;

	@Override
	@Transactional
	public void saveNotionKey(NotionConnectInfoCommand command) {
		UserEntity user = getUser(command.getUserId());

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

	@Override
	@Transactional
	public void registerNotionDatabase(NotionRegisterDatabaseCommand command) {
		UserEntity user = getUser(command.getUserId());
		String notionToken = encryptor.decrypt(user.getNotionSecretKey());

		boolean response = notionApiClient.validateNotionDatabase(
			notionToken,
			command.getNotionDatabaseUid()
		);

		if (!response) {
			throw new BadRequestException(ExceptionCode.RETRIEVE_NOTION_DATABASE_FAILED);
		}

		NotionDatabaseEntity notionDatabase = NotionDatabaseEntity.builder()
			.user(user)
			.databaseUid(command.getNotionDatabaseUid())
			.databaseName(command.getNotionDatabaseName())
			.build();

		notionDatabaseRepository.save(notionDatabase);
	}

	private UserEntity getUser(Long userId) {
		return userRepository.findById(userId)
			.orElseThrow(() -> new BadRequestException(ExceptionCode.USER_NOT_FOUND));
	}
}
