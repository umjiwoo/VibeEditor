package com.ssafy.vibe.notion.client;

import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotionApiClient {
	private final WebClient webClient;

	@Value("${notion.api.base_url}")
	private String NOTION_BASE_URL;
	@Value("${notion.api.version}")
	private String NOTION_VERSION;

	public Map<String, Object> createPage(Map<String, Object> pageRequest, String notionToken) {
		return webClient.post()
			.uri(NOTION_BASE_URL + "/pages")
			.headers(headers -> setNotionApiHeaders(headers, notionToken))
			.contentType(MediaType.APPLICATION_JSON)
			.body(BodyInserters.fromValue(pageRequest))
			.retrieve()
			.bodyToMono(Map.class)
			.block();
	}

	public boolean validateNotionToken(String notionToken) {
		try {
			webClient.get()
				.uri(NOTION_BASE_URL + "/users/me")
				.headers(headers -> setNotionApiHeaders(headers, notionToken))
				.accept(MediaType.APPLICATION_JSON)
				.retrieve()
				.bodyToMono(Void.class)
				.block();

			return true;
		} catch (Exception e) {
			return false;
		}
	}

	private void setNotionApiHeaders(HttpHeaders headers, String notionToken) {
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.set("Authorization", "Bearer " + notionToken);
		headers.set("Notion-Version", NOTION_VERSION);
	}
}