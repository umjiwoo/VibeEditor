package com.ssafy.vibe.post.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ssafy.vibe.common.exception.BadRequestException;
import com.ssafy.vibe.common.exception.ExceptionCode;
import com.ssafy.vibe.post.domain.PostEntity;
import com.ssafy.vibe.post.repository.PostRepository;
import com.ssafy.vibe.post.service.command.NotionPostCommand;
import com.ssafy.vibe.post.service.dto.NotionPostDTO;
import com.ssafy.vibe.post.util.NotionUtil;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class PostServiceImpl implements PostService {

	private final RestTemplate restTemplate;
	private final ObjectMapper objectMapper;
	private final NotionUtil notionUtil;

	private final PostRepository postRepository;
	private final String NOTION_TOKEN = "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";
	private final String NOTION_DATABASE_ID = "1e030c90fbb580a68cf5ea020934468f";

	@Value("${notion.api.base_url}")
	private String NOTION_BASE_URL;
	@Value("${notion.api.version}")
	private String NOTION_VERSION;

	@Override
	public NotionPostDTO createNotionPost(NotionPostCommand command) {

		PostEntity post = postRepository.findById(command.getPostId())
			.orElseThrow(() -> new BadRequestException(ExceptionCode.POST_NOT_FOUND));

		try {
			List<Map<String, Object>> notionBlocks = notionUtil.parseMarkdownToNotionBlocks(
				post.getPreview());

			// Extract title from the first heading or use default
			String title = post.getTitle();

			// Create page request
			Map<String, Object> createPageRequest = new HashMap<>();

			// Set parent - can be a page or database
			Map<String, Object> parent = new HashMap<>();
			// Otherwise assume it's a database ID
			parent.put("type", "database_id");
			parent.put("database_id", NOTION_DATABASE_ID);
			createPageRequest.put("parent", parent);

			// Set properties - at minimum a title
			Map<String, Object> properties = new HashMap<>();
			Map<String, Object> titleProperty = new HashMap<>();
			titleProperty.put("title", createRichTextArray(title));
			properties.put("title", titleProperty);
			createPageRequest.put("properties", properties);

			// Set children blocks
			createPageRequest.put("children", notionBlocks);

			// Make API call to create page
			HttpHeaders headers = getNotionApiHeaders();
			HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(createPageRequest, headers);
			String json = objectMapper.writeValueAsString(requestEntity.getBody());

			String createPageUrl = NOTION_BASE_URL + "/pages";
			Map response = restTemplate.exchange(
				createPageUrl,
				HttpMethod.POST,
				requestEntity,
				Map.class
			).getBody();

			String postUrl = (String)response.get("url");

			return new NotionPostDTO(postUrl);
		} catch (Exception e) {
			throw new BadRequestException(ExceptionCode.NOTION_UPLOAD_FAILED);
		}
	}

	private void appendBlocksToPage(String pageId, List<Map<String, Object>> blocks) {
		HttpHeaders headers = getNotionApiHeaders();
		Map<String, Object> requestBody = new HashMap<>();
		requestBody.put("children", blocks);

		HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);

		String appendBlocksUrl = NOTION_BASE_URL + "/blocks/" + pageId + "/children";
		restTemplate.exchange(
			appendBlocksUrl,
			HttpMethod.PATCH,
			requestEntity,
			Map.class
		);
	}

	private HttpHeaders getNotionApiHeaders() {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.set("Authorization", "Bearer " + NOTION_TOKEN);
		headers.set("Notion-Version", NOTION_VERSION);
		return headers;
	}

	private List<Map<String, Object>> createRichTextArray(String text) {
		Map<String, Object> richText = new HashMap<>();
		Map<String, Object> textContent = new HashMap<>();
		textContent.put("content", text);
		richText.put("type", "text");
		richText.put("text", textContent);
		return List.of(richText);
	}

}
