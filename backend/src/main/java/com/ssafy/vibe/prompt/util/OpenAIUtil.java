package com.ssafy.vibe.prompt.util;

import static com.ssafy.vibe.common.exception.ExceptionCode.*;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;
import com.openai.models.chat.completions.ChatCompletion;
import com.openai.models.chat.completions.ChatCompletionCreateParams;
import com.openai.models.chat.completions.ChatCompletionMessage;
import com.ssafy.vibe.common.exception.ExternalAPIException;
import com.ssafy.vibe.common.exception.ServerException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Component
public class OpenAIUtil {
	private static final ObjectMapper mapper = new ObjectMapper();

	public ChatCompletion callOpenAIAPI(
		String model, Double temperature,
		String apiKey,
		String systemPromptContent, String userPromptContent
	) {
		OpenAIClient client = OpenAIOkHttpClient.builder()
			.apiKey(apiKey)
			.build();

		ChatCompletionCreateParams params = ChatCompletionCreateParams.builder()
			.model(model)
			// .responseFormat() JSON 포맷 설정 필요할 수 있음
			.addSystemMessage(systemPromptContent)
			.addUserMessage(userPromptContent)
			.temperature(temperature)
			.build();

		// TODO: 이 단계에서 예외가 터지므로 에러 핸들링 필요
		return client.chat().completions().create(params);
	}

	public String[] handleOpenAIResponse(ChatCompletion response) {
		if (response == null || response.choices().isEmpty()) {
			throw new ExternalAPIException(OPENAI_EMPTY_CONTENT);
		}

		return response.choices().stream()
			.map(ChatCompletion.Choice::message)
			.map(ChatCompletionMessage::content) // Optional<String>
			.filter(Optional::isPresent)
			.map(Optional::get)                  // String
			.findFirst()
			.map(this::parseContent)
			.orElseThrow(() -> new ExternalAPIException(OPENAI_EMPTY_CONTENT));
	}

	public String[] parseContent(String content) {
		Pattern pattern = Pattern.compile("```json\\s*(\\{.*?})\\s*```", Pattern.DOTALL);
		Matcher matcher = pattern.matcher(content);

		if (!matcher.find()) {
			throw new ExternalAPIException(OPENAI_REQUEST_DATA_NOT_FOUND);
		}

		String rawJsonBlock = matcher.group(1);

		String cleanedJson = rawJsonBlock
			.replace("\t", "  ")
			.replaceAll("[\\p{Cntrl}&&[^\r\n\t]]", "");

		try {
			JsonNode responseJson = mapper.readTree(cleanedJson);

			String postTitle = responseJson.path("postTitle").asText(null);
			String postContent = responseJson.path("postContent").asText(null);

			if (postTitle == null || postContent == null) {
				throw new ExternalAPIException(OPENAI_REQUEST_DATA_NOT_FOUND);
			}

			return new String[] {postTitle, postContent};
		} catch (JsonProcessingException e) {
			log.error("JSON 파싱 오류: {}", e.getMessage());
			throw new ServerException(OPENAI_JSON_PARSING_ERROR);
		}
	}
}
