package com.ssafy.vibe.post.service;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.ssafy.vibe.common.exception.BadRequestException;
import com.ssafy.vibe.common.exception.ExceptionCode;
import com.ssafy.vibe.common.util.Aes256Util;
import com.ssafy.vibe.notion.client.NotionApiClient;
import com.ssafy.vibe.notion.factory.NotionPageRequestFactory;
import com.ssafy.vibe.post.domain.PostEntity;
import com.ssafy.vibe.post.repository.PostRepository;
import com.ssafy.vibe.post.service.command.NotionPostCommand;
import com.ssafy.vibe.post.service.dto.NotionPostDTO;
import com.ssafy.vibe.post.util.NotionUtil;
import com.ssafy.vibe.user.domain.UserEntity;
import com.ssafy.vibe.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class PostServiceImpl implements PostService {
	private final NotionApiClient notionApiClient;
	private final NotionPageRequestFactory notionPageRequestFactory;
	private final NotionUtil notionUtil;
	private final Aes256Util aes256Util;
	private final PostRepository postRepository;
	private final UserRepository userRepository;

	@Override
	public NotionPostDTO createNotionPost(NotionPostCommand command) {
		UserEntity user = userRepository.findById(command.getUserId())
			.orElseThrow(() -> new BadRequestException(ExceptionCode.USER_NOT_FOUND));
		String notionToken = aes256Util.decrypt(user.getNotionApi());

		PostEntity post = postRepository.findByIdWithPromptAndNotionDatabase(command.getPostId())
			.orElseThrow(() -> new BadRequestException(ExceptionCode.POST_NOT_FOUND));
		String notionDatabaseId = post.getPrompt().getNotionDatabase().getDatabaseUid();

		try {
			List<Map<String, Object>> notionBlocks = notionUtil.parseMarkdownToNotionBlocks(post.getPreview());
			String title = post.getTitle();

			Map<String, Object> pageRequest =
				notionPageRequestFactory.createPageRequest(notionDatabaseId, title, notionBlocks);

			Map<String, Object> response = notionApiClient.createPage(pageRequest, notionToken);
			String postUrl = (String)response.get("url");

			return new NotionPostDTO(postUrl);
		} catch (Exception e) {
			throw new BadRequestException(ExceptionCode.NOTION_UPLOAD_FAILED);
		}
	}
}