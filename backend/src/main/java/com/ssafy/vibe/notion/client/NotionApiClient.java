package com.ssafy.vibe.notion.client;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class NotionApiClient {
	private final RestTemplate restTemplate;

	@Value("${notion.api.base_url}")
	private String NOTION_BASE_URL;
	@Value("${notion.api.version}")
	private String NOTION_VERSION;

	public Map<String, Object> createPage(Map<String, Object> pageRequest, String notionToken) {
		HttpHeaders headers = getNotionApiHeaders(notionToken);
		HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(pageRequest, headers);

		String url = NOTION_BASE_URL + "/pages";
		ResponseEntity<Map> response = restTemplate.exchange(
			url, HttpMethod.POST, requestEntity, Map.class
		);
		return response.getBody();
	}

	public void appendBlocksToPage(String pageId, List<Map<String, Object>> blocks, String notionToken) {
		HttpHeaders headers = getNotionApiHeaders(notionToken);
		Map<String, Object> requestBody = Map.of("children", blocks);
		HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);

		String url = NOTION_BASE_URL + "/blocks/" + pageId + "/children";
		restTemplate.exchange(url, HttpMethod.PATCH, requestEntity, Map.class);
	}

	private HttpHeaders getNotionApiHeaders(String notionToken) {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.set("Authorization", "Bearer " + notionToken);
		headers.set("Notion-Version", NOTION_VERSION);
		return headers;
	}
}
