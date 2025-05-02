package com.ssafy.vibe.post.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.ssafy.vibe.common.exception.BadRequestException;
import com.ssafy.vibe.common.exception.ExceptionCode;
import com.ssafy.vibe.common.util.Aes256Util;
import com.ssafy.vibe.notion.client.NotionApiClient;
import com.ssafy.vibe.notion.domain.NotionDatabaseEntity;
import com.ssafy.vibe.notion.factory.NotionPageRequestFactory;
import com.ssafy.vibe.post.domain.PostEntity;
import com.ssafy.vibe.post.repository.PostRepository;
import com.ssafy.vibe.post.service.command.NotionPostCommand;
import com.ssafy.vibe.post.service.dto.NotionPostDTO;
import com.ssafy.vibe.post.util.NotionUtil;
import com.ssafy.vibe.prompt.domain.PromptEntity;
import com.ssafy.vibe.user.domain.UserEntity;
import com.ssafy.vibe.user.repository.UserRepository;

class PostServiceImplTest {

	@InjectMocks
	private PostServiceImpl postService;

	@Mock
	private NotionApiClient notionApiClient;
	@Mock
	private NotionPageRequestFactory notionPageRequestFactory;
	@Mock
	private NotionUtil notionUtil;
	@Mock
	private Aes256Util aes256Util;
	@Mock
	private PostRepository postRepository;
	@Mock
	private UserRepository userRepository;

	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);
		postService = new PostServiceImpl(
			notionApiClient, notionPageRequestFactory, notionUtil, aes256Util, postRepository, userRepository
		);
	}

	@Test
	@DisplayName("성공: 노션 포스트 생성")
	void createNotionPost_success() {
		// given
		Long userId = 1L, postId = 2L;
		NotionPostCommand command = new NotionPostCommand(userId, postId);
		UserEntity user = mock(UserEntity.class);
		when(user.getNotionSecretKey()).thenReturn("enc-token");
		when(userRepository.findById(userId)).thenReturn(Optional.of(user));
		when(aes256Util.decrypt(anyString())).thenReturn("dec-token");

		NotionDatabaseEntity database = mock(NotionDatabaseEntity.class);
		when(database.getDatabaseUid()).thenReturn("dbid");
		PromptEntity prompt = mock(PromptEntity.class);
		when(prompt.getNotionDatabase()).thenReturn(database);
		PostEntity post = mock(PostEntity.class);
		when(post.getPrompt()).thenReturn(prompt);
		when(post.getPostTitle()).thenReturn("title");
		when(post.getPostContent()).thenReturn("content");
		when(postRepository.findByIdWithPromptAndNotionDatabase(postId)).thenReturn(Optional.of(post));

		List<Map<String, Object>> blocks = List.of(Map.of("key", "value"));
		when(notionUtil.parseMarkdownToNotionBlocks(any())).thenReturn(blocks);
		Map<String, Object> requestMap = Map.of("some", "thing");
		when(notionPageRequestFactory.createPageRequest(any(), any(), any())).thenReturn(requestMap);
		String expectedUrl = "https://notion.so/some";
		Map<String, Object> notionApiResult = Map.of("url", expectedUrl);
		when(notionApiClient.createPage(any(), any())).thenReturn(notionApiResult);

		// when
		NotionPostDTO result = postService.createNotionPost(command);

		// then
		assertThat(result).isNotNull();
		assertThat(result.getPostUrl()).isEqualTo(expectedUrl);
	}

	@Test
	@DisplayName("예외: 유저가 존재하지 않을 때")
	void createNotionPost_userNotFound() {
		// given
		Long userId = 99L, postId = 10L;
		NotionPostCommand command = new NotionPostCommand(userId, postId);
		when(userRepository.findById(userId)).thenReturn(Optional.empty());

		// when & then
		BadRequestException thrown = catchThrowableOfType(
			() -> postService.createNotionPost(command),
			BadRequestException.class
		);
		assertThat(thrown).isNotNull();
		assertThat(thrown.getCode()).isEqualTo(ExceptionCode.USER_NOT_FOUND.getCode());
		assertThat(thrown.getMessage()).isEqualTo(ExceptionCode.USER_NOT_FOUND.getMessage());
	}

	@Test
	@DisplayName("예외: 게시글이 존재하지 않을 때")
	void createNotionPost_postNotFound() {
		// given
		Long userId = 1L, postId = 123L;
		NotionPostCommand command = new NotionPostCommand(userId, postId);
		UserEntity user = mock(UserEntity.class);
		when(user.getNotionSecretKey()).thenReturn("enc-token");
		when(userRepository.findById(userId)).thenReturn(Optional.of(user));
		when(aes256Util.decrypt(anyString())).thenReturn("decrypted-token");
		when(postRepository.findByIdWithPromptAndNotionDatabase(postId)).thenReturn(Optional.empty());

		// when & then
		BadRequestException thrown = catchThrowableOfType(
			() -> postService.createNotionPost(command),
			BadRequestException.class
		);
		assertThat(thrown).isNotNull();
		assertThat(thrown.getCode()).isEqualTo(ExceptionCode.POST_NOT_FOUND.getCode());
		assertThat(thrown.getMessage()).isEqualTo(ExceptionCode.POST_NOT_FOUND.getMessage());
	}

	@Test
	@DisplayName("예외: Notion API 업로드 실패 시")
	void createNotionPost_notionApiError_fail() {
		// given
		Long userId = 1L, postId = 2L;
		NotionPostCommand command = new NotionPostCommand(userId, postId);

		UserEntity user = mock(UserEntity.class);
		when(user.getNotionSecretKey()).thenReturn("token");
		when(userRepository.findById(userId)).thenReturn(Optional.of(user));
		when(aes256Util.decrypt(anyString())).thenReturn("dec-token");

		NotionDatabaseEntity notiDb = mock(NotionDatabaseEntity.class);
		when(notiDb.getDatabaseUid()).thenReturn("dbid");
		PromptEntity prompt = mock(PromptEntity.class);
		when(prompt.getNotionDatabase()).thenReturn(notiDb);
		PostEntity post = mock(PostEntity.class);
		when(post.getPrompt()).thenReturn(prompt);
		when(post.getPostTitle()).thenReturn("t");
		when(post.getPostContent()).thenReturn("c");
		when(postRepository.findByIdWithPromptAndNotionDatabase(postId)).thenReturn(Optional.of(post));

		when(notionUtil.parseMarkdownToNotionBlocks(any())).thenThrow(new RuntimeException("error"));

		// when & then
		BadRequestException thrown = catchThrowableOfType(
			() -> postService.createNotionPost(command),
			BadRequestException.class
		);
		assertThat(thrown).isNotNull();
		assertThat(thrown.getCode()).isEqualTo(ExceptionCode.NOTION_UPLOAD_FAILED.getCode());
		assertThat(thrown.getMessage()).isEqualTo(ExceptionCode.NOTION_UPLOAD_FAILED.getMessage());
	}
}