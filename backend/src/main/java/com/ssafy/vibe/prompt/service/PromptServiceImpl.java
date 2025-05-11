package com.ssafy.vibe.prompt.service;

import static com.ssafy.vibe.common.exception.ExceptionCode.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.anthropic.client.AnthropicClient;
import com.anthropic.client.okhttp.AnthropicOkHttpClient;
import com.anthropic.core.http.HttpResponseFor;
import com.anthropic.models.messages.ContentBlock;
import com.anthropic.models.messages.Message;
import com.anthropic.models.messages.MessageCreateParams;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ssafy.vibe.common.exception.BadRequestException;
import com.ssafy.vibe.common.exception.ExternalAPIException;
import com.ssafy.vibe.common.exception.NotFoundException;
import com.ssafy.vibe.common.exception.ServerException;
import com.ssafy.vibe.notion.domain.NotionDatabaseEntity;
import com.ssafy.vibe.notion.repository.NotionDatabaseRepository;
import com.ssafy.vibe.post.domain.PostEntity;
import com.ssafy.vibe.post.domain.PostType;
import com.ssafy.vibe.post.repository.PostRepository;
import com.ssafy.vibe.post.service.dto.PostSaveDTO;
import com.ssafy.vibe.prompt.controller.response.CreatedPostResponse;
import com.ssafy.vibe.prompt.controller.response.OptionResponse;
import com.ssafy.vibe.prompt.controller.response.PromptAttachRespnose;
import com.ssafy.vibe.prompt.controller.response.RetrievePromptResponse;
import com.ssafy.vibe.prompt.domain.OptionEntity;
import com.ssafy.vibe.prompt.domain.PromptAttachEntity;
import com.ssafy.vibe.prompt.domain.PromptEntity;
import com.ssafy.vibe.prompt.domain.PromptOptionEntity;
import com.ssafy.vibe.prompt.repository.OptionRepository;
import com.ssafy.vibe.prompt.repository.PromptAttachRepository;
import com.ssafy.vibe.prompt.repository.PromptOptionRepository;
import com.ssafy.vibe.prompt.repository.PromptRepository;
import com.ssafy.vibe.prompt.service.command.GeneratePostCommand;
import com.ssafy.vibe.prompt.service.command.PromptAttachUpdateCommand;
import com.ssafy.vibe.prompt.service.command.PromptSaveCommand;
import com.ssafy.vibe.prompt.service.command.PromptUpdateCommand;
import com.ssafy.vibe.prompt.service.command.SnapshotCommand;
import com.ssafy.vibe.prompt.service.dto.OptionItemDTO;
import com.ssafy.vibe.prompt.service.dto.PromptAttachDTO;
import com.ssafy.vibe.prompt.service.dto.PromptOptionDTO;
import com.ssafy.vibe.prompt.service.dto.PromptSaveDTO;
import com.ssafy.vibe.prompt.service.dto.RetrievePromptAttachDTO;
import com.ssafy.vibe.prompt.service.dto.RetrievePromptDTO;
import com.ssafy.vibe.prompt.template.PromptTemplate;
import com.ssafy.vibe.snapshot.domain.SnapshotEntity;
import com.ssafy.vibe.snapshot.repository.SnapshotRepository;
import com.ssafy.vibe.template.domain.TemplateEntity;
import com.ssafy.vibe.template.repository.TemplateRepository;
import com.ssafy.vibe.user.domain.UserEntity;
import com.ssafy.vibe.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class PromptServiceImpl implements PromptService {
	private static final ObjectMapper mapper = new ObjectMapper();

	private ChatClient chatClient;
	private final PromptTemplate promptTemplate;
	private final UserRepository userRepository;
	private final SnapshotRepository snapshotRepository;
	private final OptionRepository optionRepository;
	private final TemplateRepository templateRepository;
	private final PromptRepository promptRepository;
	private final PromptAttachRepository promptAttachRepository;
	private final PromptOptionRepository promptOptionRepository;
	private final NotionDatabaseRepository notionDatabaseRepository;
	private PostRepository postRepository;

	@Value("${CLAUDE_APIKEY}")
	private String anthropicApiKey;
	@Value("${CLAUDE_MODEL}")
	private String anthropicModel;
	@Value("${CLAUDE_TEMPERATURE}")
	private float anthropicTemperature;
	@Value("${CLAUDE_MAX_TOKEN}")
	private int anthropicMaxTokens;

	@Autowired
	public PromptServiceImpl(
		@Qualifier("anthropicChatClient") ChatClient chatClient,
		PromptTemplate promptTemplate,
		UserRepository userRepository,
		SnapshotRepository snapshotRepository,
		OptionRepository optionRepository,
		TemplateRepository templateRepository,
		PromptRepository promptRepository,
		PromptAttachRepository promptAttachRepository,
		PromptOptionRepository promptOptionRepository,
		NotionDatabaseRepository notionDatabaseRepository, PostRepository postRepository) {
		this.chatClient = chatClient;
		this.promptTemplate = promptTemplate;
		this.userRepository = userRepository;
		this.snapshotRepository = snapshotRepository;
		this.optionRepository = optionRepository;
		this.templateRepository = templateRepository;
		this.promptRepository = promptRepository;
		this.promptAttachRepository = promptAttachRepository;
		this.promptOptionRepository = promptOptionRepository;
		this.notionDatabaseRepository = notionDatabaseRepository;
		this.postRepository = postRepository;
	}

	@Override
	public CreatedPostResponse createDraft(GeneratePostCommand generatePostCommand) {
		PromptEntity prompt = promptRepository.findById(generatePostCommand.getPromptId())
			.orElseThrow(() -> new NotFoundException(PROMPT_NOT_FOUND));

		if (prompt.getComment() == null) {
			throw new BadRequestException(PROMPT_CONTENT_NULL);
		}

		String generatedUserPrompt = buildUserPromptContent(prompt);

		String[] parsedContentArray = null;
		try (HttpResponseFor<Message> response = callClaudeAPI(generatedUserPrompt)) {
			if (response.statusCode() != 200) {
				log.error("Anthropic API 오류 - status code: {}", response.statusCode());
				switch (response.statusCode()) {
					case 400 -> throw new BadRequestException("잘못된 요청입니다 (400 Bad Request)");
					case 401 -> throw new ExternalAPIException("인증 오류입니다 (401 Unauthorized)");
					case 403 -> throw new ExternalAPIException("권한이 없습니다 (403 Forbidden)");
					case 404 -> throw new ExternalAPIException("엔드포인트를 찾을 수 없습니다 (404 Not Found)");
					case 429 -> throw new ExternalAPIException("요청이 너무 많습니다 (429 Too Many Requests)");
					case 500 -> throw new ExternalAPIException("서버 오류입니다 (500 Internal Server Error)");
					case 502 -> throw new ExternalAPIException("게이트웨이 오류입니다 (502 Bad Gateway)");
					default -> throw new ExternalAPIException("알 수 없는 API 오류입니다 - 상태 코드: " + response.statusCode());
				}
			}

			Message message = response.parse();

			if (message._stopReason().toString().equals("max_tokens")) {
				log.error("Anthropic API 오류 - max_tokens 초과");
				throw new ExternalAPIException(OVER_MAX_TOKEN);
			}

			List<ContentBlock> contentBlocks = message.content();
			if (contentBlocks == null || contentBlocks.isEmpty()) {
				log.error("Anthropic API 오류 - 응답 내용 없음");
				throw new ExternalAPIException(EMPTY_CONTENT);
			}

			String content = contentBlocks.getFirst().toString();
			parsedContentArray = parseContent(content);
		}

		PostSaveDTO postDTO = PostSaveDTO.from(
			null,
			prompt,
			parsedContentArray[0],
			parsedContentArray[1]
		);

		PostEntity post = postDTO.toEntity();
		post = postRepository.save(post);

		return CreatedPostResponse.from(
			post.getId(),
			post.getPostTitle(),
			post.getPostContent());
	}

	private HttpResponseFor<Message> callClaudeAPI(String userPromptContent) {
		AnthropicClient client = AnthropicOkHttpClient.builder()
			.apiKey(anthropicApiKey)
			.build();

		String systemPrompt = buildSystemPromptContent();
		log.info("systemPrompt: {}", systemPrompt);

		MessageCreateParams params = MessageCreateParams.builder()
			.maxTokens(anthropicMaxTokens)
			.model(anthropicModel)
			.system(systemPrompt)
			.addUserMessage(userPromptContent)
			.temperature(anthropicTemperature)
			.build();

		return client.messages().withRawResponse().create(params);
	}

	private String[] parseContent(String content) {
		Pattern pattern = Pattern.compile("```json\\s*(\\{.*?})\\s*```", Pattern.DOTALL);
		Matcher matcher = pattern.matcher(content);

		if (!matcher.find()) {
			throw new ExternalAPIException(REQUEST_DATA_NOT_FOUND);
		}

		JsonNode responseJson = null;
		try {
			responseJson = mapper.readTree(matcher.group(1));
		} catch (JsonProcessingException e) {
			log.error("JSON 파싱 오류: {}", e.getMessage());
			throw new ServerException(JSON_PARSING_ERROR);
		}

		// if (!responseJson.has("postTitle") || !responseJson.has("postContent")) {
		// 	log.error("Claude 응답에 postTitle 또는 postContent가 없음");
		// 	throw new ExternalAPIException(REQUEST_DATA_NOT_FOUND);
		// }

		String postTitle = responseJson.get("postTitle").asText();
		String postContent = responseJson.get("postContent").asText();

		return new String[] {postTitle, postContent};
	}

	private String buildSystemPromptContent() {
		String SYSTEM_PROMPT = promptTemplate.getSystemPrompt();
		return String.format(SYSTEM_PROMPT,
			anthropicMaxTokens
		);
	}

	private String buildUserPromptContent(PromptEntity prompt) {
		String snapshotsFormatted = formatSnapshots(prompt.getAttachments());
		String optionsFormatted = formatOptions(prompt.getPromptOptions());

		String BLOG_PROMPT_TEMPLATE = promptTemplate.getPromptTemplate();

		return String.format(BLOG_PROMPT_TEMPLATE,
			prompt.getPostType(),
			snapshotsFormatted,
			prompt.getComment(),
			optionsFormatted
		);
	}

	private String formatSnapshots(List<PromptAttachEntity> attachments) {
		String snapshotsFormatted = attachments.stream()
			.map(promptAttachEntity ->
				snapshotRepository.findById(promptAttachEntity.getSnapshot().getId())
					.map(
						snapshot -> String.format(
							"""
								    * snapshot :
								    ```
								    %s
								    ```
								
								    * description :
								    ```
								    %s
								    ```
								""",
							snapshot.getSnapshotContent(),
							promptAttachEntity.getDescription()))
					.orElseThrow(() -> new NotFoundException(SNAPSHOT_NOT_FOUND)))
			.collect(Collectors.joining("\n"));

		if (snapshotsFormatted.isEmpty()) {
			snapshotsFormatted = "입력된 코드 스냅샷-스냅샷에 대한 설명 쌍이 없습니다. 다른 정보들을 기반으로 포스트 초안을 만듭니다.";
		}

		return snapshotsFormatted;
	}

	private String formatOptions(List<PromptOptionEntity> promptOptions) {
		StringBuilder optionsFormatted = new StringBuilder();
		for (PromptOptionEntity promptOption : promptOptions) {
			Optional<OptionEntity> option = optionRepository.findById(promptOption.getOption().getId());
			option.ifPresent(optionEntity -> {
				optionsFormatted.append(option.get().getOptionName())
					.append(" : ")
					.append(option.get().getValue())
					.append("\n");
			});
		}

		if (optionsFormatted.isEmpty()) {
			optionsFormatted.append("이모지 포함 및 문체 스타일은 알아서 합니다.");
		}

		return optionsFormatted.toString();
	}

	@Override
	public void savePrompt(Long userId, PromptSaveCommand promptSaveCommand) {
		UserEntity user = userRepository.findById(userId)
			.orElseThrow(() -> new NotFoundException(USER_NOT_FOUND));

		PromptSaveDTO promptSaveDTO = new PromptSaveDTO(
			promptSaveCommand, user
		);

		PromptEntity parentPrompt = null;
		if (promptSaveDTO.getParentPromptId() != null) {
			parentPrompt = promptRepository.findById(promptSaveDTO.getParentPromptId())
				.orElse(null);
		}

		TemplateEntity template = templateRepository.findById(promptSaveDTO.getTemplateId())
			.orElseThrow(() -> new BadRequestException(TEMPLATE_NOT_FOUND));

		NotionDatabaseEntity notionDatabase = notionDatabaseRepository.findById(promptSaveDTO.getNotionDatabaseId())
			.orElseThrow(() -> new BadRequestException(NOTION_DATABASE_NOT_FOUND));

		PromptEntity prompt = promptSaveDTO.toEntity(parentPrompt, template, user, notionDatabase);
		prompt = promptRepository.save(prompt);

		List<PromptAttachEntity> promptAttachList = buildPromptAttachments(
			prompt.getId(),
			promptSaveCommand.getPromptAttachList());
		promptAttachRepository.saveAll(promptAttachList);
		prompt.getAttachments().addAll(promptAttachList);

		List<PromptOptionEntity> promptOptions = buildPromptOptions(
			prompt.getId(),
			promptSaveCommand.getPromptOptionList());
		promptOptionRepository.saveAll(promptOptions);
		prompt.getPromptOptions().addAll(promptOptions);

		promptRepository.save(prompt);
	}

	@Override
	public RetrievePromptResponse getPrompt(Long userId, Long promptId) {
		PromptEntity prompt = promptRepository.findById(promptId)
			.orElseThrow(() -> new NotFoundException(PROMPT_NOT_FOUND));

		checkPromptUser(userId, prompt.getUser().getId());

		RetrievePromptDTO retrievePromptDTO = RetrievePromptDTO.fromEntity(prompt);
		List<PromptAttachEntity> promptAttachEntityList = promptAttachRepository.findByPromptAndIsDeletedFalse(prompt);
		List<PromptOptionEntity> promptOptionEntityList = promptOptionRepository.findByPromptAndIsDeletedFalse(prompt);

		List<PromptAttachRespnose> promptAttachList = promptAttachEntityList.stream()
			.map(RetrievePromptAttachDTO::from)
			.map(PromptAttachRespnose::from)
			.toList();

		List<Long> promptOptionIds = promptOptionEntityList.stream()
			.map(promptOptionEntity -> promptOptionEntity.getOption().getId())
			.toList();

		PromptEntity parentPrompt = null;
		if (prompt.getParentPrompt() != null) {
			parentPrompt = promptRepository.findById(prompt.getParentPrompt().getId())
				.orElse(null);
		}

		RetrievePromptDTO retrieveParentPromptDTO = null;
		if (parentPrompt != null) {
			retrieveParentPromptDTO = RetrievePromptDTO.fromEntity(parentPrompt);
		}

		return RetrievePromptResponse.from(retrievePromptDTO, retrieveParentPromptDTO, promptAttachList,
			promptOptionIds);
	}

	@Override
	public void updatePrompt(Long userId, Long promptId, PromptUpdateCommand promptUpdateCommand) {
		PromptEntity prompt = promptRepository.findById(promptId)
			.orElseThrow(() -> new NotFoundException(PROMPT_NOT_FOUND));

		checkPromptUser(userId, prompt.getUser().getId());

		prompt.updatePromptName(promptUpdateCommand.getPromptName());
		prompt.updatePoostType(PostType.valueOf(promptUpdateCommand.getPostType()));
		prompt.updateComment(promptUpdateCommand.getComment());

		List<Long> promptAttachEntitiyIdsToUpdate = new ArrayList<>();
		List<PromptAttachEntity> newPromptAttachEntitiesToInsert = new ArrayList<>();

		// PromptAttach 업데이트
		// 1. 새롭게 받은 PromptAttachList의 promptAttachId 조회
		for (PromptAttachUpdateCommand command : promptUpdateCommand.getPromptAttachList()) {
			if (command.getAttachId() != null) { // 기존에 저장됐었던 PromptAttachEntity인 경우
				PromptAttachEntity existingPromptAttach = promptAttachRepository.findById(command.getAttachId())
					.orElseThrow(() -> new NotFoundException(PROMPT_ATTACH_NOT_FOUND));
				// 바로 내용 수정
				existingPromptAttach.updateDescription(command.getDescription());

				// 기존에 저장돼 있었던 PromptAttachEntity 중 수정 요청에 포함되지 않은 객체에 대해 isDeleted=true로 업데이트하기 위해 id 저장
				promptAttachEntitiyIdsToUpdate.add(existingPromptAttach.getId());
			} else { // 새롭게 입력된 PromptAttachEntity인 경우
				SnapshotEntity snapshot = snapshotRepository.findById(command.getSnapshotId())
					.orElseThrow(() -> new NotFoundException(SNAPSHOT_NOT_FOUND));

				PromptAttachDTO promptAttachDTO = command.toDTO(prompt.getId(), snapshot.getId());
				PromptAttachEntity promptAttachEntity = promptAttachDTO.toEntity(prompt, snapshot);

				newPromptAttachEntitiesToInsert.add(promptAttachEntity);
			}
		}

		// 2. 기존 PromptAttachEntityList 조회
		List<PromptAttachEntity> allPromptAttachEntities = promptAttachRepository.findByPrompt(prompt);

		// 3. 기존에 있고, isDeleted=false인데 새롭게 들어온 PromptAttachList > promptAttachIds 에 없는 경우 isDeleted=true 로 업데이트
		for (PromptAttachEntity existingPromptAttachEntity : allPromptAttachEntities) {
			Long promptAttachId = existingPromptAttachEntity.getId();

			if (!promptAttachEntitiyIdsToUpdate.contains(promptAttachId)) {
				existingPromptAttachEntity.setIsDeleted(true);
			}
		}

		// 4. 새롭게 들어온 PromptAttach 저장
		promptAttachRepository.saveAll(newPromptAttachEntitiesToInsert);

		// PromptOption 업데이트
		List<PromptOptionEntity> existingPromptOptions = promptOptionRepository.findByPrompt(prompt);
		List<Long> newOptionIdList = promptUpdateCommand.getPromptOptionList();
		updatePromptOptions(existingPromptOptions, newOptionIdList, prompt);

		promptRepository.save(prompt);
	}

	@Override
	public List<OptionResponse> getOptionList() {
		Map<String, List<OptionItemDTO>> optionMap = new HashMap<>();
		optionRepository.findAll().forEach(option -> {
			String optionName = option.getOptionName().toString();
			optionName = optionName.equals("EMOJI") ? "이모지" : "문체";
			OptionItemDTO optionItem = OptionItemDTO.from(option);

			optionMap.computeIfAbsent(optionName, k -> new ArrayList<>()).add(optionItem);
		});

		return optionMap.entrySet().stream()
			.map(option ->
				OptionResponse.from(option.getKey(), option.getValue())).collect(Collectors.toList());
	}

	private void updatePromptOptions(
		List<PromptOptionEntity> existingPromptOptions,
		List<Long> newOptionIdList,
		PromptEntity prompt) {
		existingPromptOptions.forEach(option -> option.setIsDeleted(true));

		existingPromptOptions.forEach(promptOption -> {
			Long existingOptionId = promptOption.getOption().getId();
			if (newOptionIdList.contains(existingOptionId)) {
				promptOption.setIsDeleted(false);
				newOptionIdList.remove(existingOptionId);
			}
		});

		List<PromptOptionEntity> newPromptOptions = buildPromptOptions(
			prompt.getId(),
			newOptionIdList);
		promptOptionRepository.saveAll(newPromptOptions);
	}

	private void checkPromptUser(Long userId, Long promptUserId) {
		if (!userId.equals(promptUserId)) {
			throw new BadRequestException(OWNER_MISMATCH);
		}
	}

	private List<PromptAttachEntity> buildPromptAttachments(
		Long promptId,
		List<SnapshotCommand> snapshotCommandList) {
		List<PromptAttachDTO> promptAttachDTOList = snapshotCommandList.stream()
			.map(snapshotCommand -> {
				SnapshotEntity snapshot = snapshotRepository.findById(snapshotCommand.getSnapshotId())
					.orElseThrow(() -> new NotFoundException(SNAPSHOT_NOT_FOUND));
				return snapshotCommand.toDTO(promptId, snapshot.getId());
			}).toList();

		List<PromptAttachEntity> promptAttachEntityList = promptAttachDTOList.stream()
			.map(promptAttachDTO -> {
				PromptEntity prompt = promptRepository.findById(promptAttachDTO.getPromptId())
					.orElseThrow(() -> new NotFoundException(PROMPT_NOT_FOUND));
				SnapshotEntity snapshot = snapshotRepository.findById(promptAttachDTO.getSnapshotId())
					.orElseThrow(() -> new NotFoundException(SNAPSHOT_NOT_FOUND));
				return promptAttachDTO.toEntity(prompt, snapshot);
			})
			.toList();

		return promptAttachEntityList;
	}

	private List<PromptOptionEntity> buildPromptOptions(
		Long promptId,
		List<Long> promptOptionIds) {
		List<PromptOptionDTO> promptOptionDTOList = promptOptionIds.stream()
			.map(optionId -> PromptOptionDTO.from(promptId, optionId))
			.toList();

		List<PromptOptionEntity> promptOptionEntityList = promptOptionDTOList.stream()
			.map(promptOptionDTO -> {
				PromptEntity prompt = promptRepository.findById(promptOptionDTO.getPromptId())
					.orElseThrow(() -> new NotFoundException(PROMPT_NOT_FOUND));
				OptionEntity option = optionRepository.findById(promptOptionDTO.getOptionId())
					.orElseThrow(() -> new NotFoundException(OPTION_NOT_FOUND));

				return PromptOptionDTO.toEntity(prompt, option);
			}).toList();

		return promptOptionEntityList;
	}
}
