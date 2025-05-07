package com.ssafy.vibe.notion.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

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

class NotionServiceImplTest {

	private final Long userId = 1L;
	private final String rawToken = "notion-token";
	private final String encryptedToken = "encrypted-token";
	private final String dbUid = "db-uid";
	private final String dbName = "db-name";
	private final NotionConnectInfoCommand connectCommand =
		new NotionConnectInfoCommand(userId, rawToken);
	private final NotionRegisterDatabaseCommand registerCommand =
		new NotionRegisterDatabaseCommand(userId, dbName, dbUid);
	@InjectMocks
	private NotionServiceImpl notionService;
	@Mock
	private Aes256Util encryptor;
	@Mock
	private UserRepository userRepository;
	@Mock
	private NotionApiClient notionApiClient;
	@Mock
	private NotionDatabaseRepository notionDatabaseRepository;
	@Captor
	private ArgumentCaptor<UserEntity> userCaptor;

	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);
	}

	@Nested
	@DisplayName("saveNotionKey")
	class SaveNotionKeyTest {

		@Test
		@DisplayName("정상: 노션 API 토큰 저장 성공")
		void saveNotionKey_success() {
			UserEntity user = mock(UserEntity.class);

			when(userRepository.findById(userId)).thenReturn(Optional.of(user));
			when(notionApiClient.validateNotionToken(rawToken)).thenReturn(true);
			when(encryptor.encrypt(rawToken)).thenReturn(encryptedToken);

			notionService.saveNotionKey(connectCommand);

			verify(notionApiClient).validateNotionToken(rawToken);
			verify(user).updateSecretKey(encryptedToken);
			verify(user).updateNotionActive(true);
			verify(userRepository).save(user);
		}

		@Test
		@DisplayName("실패: 노션 토큰이 유효하지 않은 경우 예외 발생")
		void saveNotionKey_invalidToken_throwException() {
			UserEntity user = mock(UserEntity.class);
			when(userRepository.findById(userId)).thenReturn(Optional.of(user));
			when(notionApiClient.validateNotionToken(rawToken)).thenReturn(false);

			assertThatThrownBy(() -> notionService.saveNotionKey(connectCommand))
				.isInstanceOf(BadRequestException.class)
				.hasFieldOrPropertyWithValue("code", ExceptionCode.INVALID_NOTION_TOKEN.getCode());

			verify(userRepository, never()).save(any());
			verify(user, never()).updateSecretKey(any());
		}

		@Test
		@DisplayName("실패: 사용자가 존재하지 않을 때 예외")
		void saveNotionKey_userNotFound_throwException() {
			when(userRepository.findById(userId)).thenReturn(Optional.empty());

			assertThatThrownBy(() -> notionService.saveNotionKey(connectCommand))
				.isInstanceOf(BadRequestException.class)
				.hasFieldOrPropertyWithValue("code", ExceptionCode.USER_NOT_FOUND.getCode());

			verify(notionApiClient, never()).validateNotionToken(any());
			verify(userRepository, never()).save(any());
		}
	}

	@Nested
	@DisplayName("registerNotionDatabase")
	class RegisterNotionDatabaseTest {

		@Test
		@DisplayName("정상: 노션 데이터베이스 등록 성공")
		void registerDatabase_success() {
			UserEntity user = mock(UserEntity.class);
			when(userRepository.findById(userId)).thenReturn(Optional.of(user));
			when(encryptor.decrypt(any())).thenReturn(rawToken);
			when(user.getNotionSecretKey()).thenReturn(encryptedToken);
			when(notionApiClient.validateNotionDatabase(rawToken, dbUid)).thenReturn(true);

			notionService.registerNotionDatabase(registerCommand);

			verify(notionApiClient).validateNotionDatabase(rawToken, dbUid);
			verify(notionDatabaseRepository).save(any(NotionDatabaseEntity.class));
		}

		@Test
		@DisplayName("실패: DB UID 유효성 실패하면 예외")
		void registerDatabase_fail_invalidDbUid() {
			UserEntity user = mock(UserEntity.class);
			when(userRepository.findById(userId)).thenReturn(Optional.of(user));
			when(encryptor.decrypt(any())).thenReturn(rawToken);
			when(user.getNotionSecretKey()).thenReturn(encryptedToken);
			when(notionApiClient.validateNotionDatabase(rawToken, dbUid)).thenReturn(false);

			assertThatThrownBy(() -> notionService.registerNotionDatabase(registerCommand))
				.isInstanceOf(BadRequestException.class)
				.hasFieldOrPropertyWithValue("code", ExceptionCode.RETRIEVE_NOTION_DATABASE_FAILED.getCode());

			verify(notionDatabaseRepository, never()).save(any());
		}

		@Test
		@DisplayName("실패: 사용자가 존재하지 않을 때 예외")
		void registerDatabase_userNotFound_throwException() {
			when(userRepository.findById(userId)).thenReturn(Optional.empty());

			assertThatThrownBy(() -> notionService.registerNotionDatabase(registerCommand))
				.isInstanceOf(BadRequestException.class)
				.hasFieldOrPropertyWithValue("code", ExceptionCode.USER_NOT_FOUND.getCode());

			verify(notionApiClient, never()).validateNotionDatabase(any(), any());
			verify(notionDatabaseRepository, never()).save(any());
		}
	}
}