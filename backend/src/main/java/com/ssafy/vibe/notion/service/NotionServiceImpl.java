package com.ssafy.vibe.notion.service;

import java.util.List;
import java.util.stream.Collectors;

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
import com.ssafy.vibe.notion.service.command.RetrieveNotionDatabasesCommand;
import com.ssafy.vibe.notion.service.dto.RetrieveNotionDatabasesDTO;
import com.ssafy.vibe.user.domain.UserEntity;
import com.ssafy.vibe.user.repository.UserRepository;
import com.ssafy.vibe.user.util.UserUtil;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NotionServiceImpl implements NotionService {
	private final Aes256Util encryptor;
	private final UserRepository userRepository;
	private final UserUtil userUtil;
	private final NotionApiClient notionApiClient;
	private final NotionDatabaseRepository notionDatabaseRepository;

	@Override
	@Transactional
	public void saveNotionKey(
		NotionConnectInfoCommand command
	) {
		UserEntity user = userUtil.getUser(command.getUserId());

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
	public void registerNotionDatabase(
		NotionRegisterDatabaseCommand command
	) {
		UserEntity user = userUtil.getUser(command.getUserId());
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

	@Override
	@Transactional(readOnly = true)
	public List<RetrieveNotionDatabasesDTO> retrieveNotionDatabases(
		RetrieveNotionDatabasesCommand command
	) {
		UserEntity user = userUtil.getUser(command.getUserId());

		List<NotionDatabaseEntity> databases = notionDatabaseRepository.findAllByUserIdOrderByUpdatedAtDesc(
			user.getId());

		return databases.stream()
			.<RetrieveNotionDatabasesDTO>
				mapMulti((entity, consumer) -> {
				if (entity != null) {
					consumer.accept(RetrieveNotionDatabasesDTO.fromEntity(entity));
				}
			})
			.collect(Collectors.toList());
	}
}
