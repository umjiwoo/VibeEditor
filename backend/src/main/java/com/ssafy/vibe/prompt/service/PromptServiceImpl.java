package com.ssafy.vibe.prompt.service;

import static com.ssafy.vibe.common.exception.ExceptionCode.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ssafy.vibe.common.exception.BadRequestException;
import com.ssafy.vibe.common.exception.NotFoundException;
import com.ssafy.vibe.common.exception.ServerException;
import com.ssafy.vibe.notion.domain.NotionDatabaseEntity;
import com.ssafy.vibe.notion.repository.NotionDatabaseRepository;
import com.ssafy.vibe.post.domain.PostType;
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
		NotionDatabaseRepository notionDatabaseRepository) {
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
	}

	@Override
	public CreatedPostResponse getDraft(GeneratePostCommand generatePostCommand) {
		PromptEntity prompt = promptRepository.findById(generatePostCommand.getPromptId())
			.orElseThrow(() -> new NotFoundException(PROMPT_NOT_FOUND));

		String snapshotsFormatted = prompt.getAttachments().stream()
			.map(promptAttachEntity -> String.format("""
					* snapshot:
					```
					%s
					```
					* description: %s
					""",
				snapshotRepository.findById(promptAttachEntity.getSnapshot().getId()),
				promptAttachEntity.getDescription()))
			.collect(Collectors.joining("\n"));

		StringBuilder optionsFormatted = new StringBuilder();
		for (PromptOptionEntity promptOption : prompt.getPromptOptions()) {
			Optional<OptionEntity> option = optionRepository.findById(promptOption.getOption().getId());
			option.ifPresent(optionEntity -> {
				optionsFormatted.append(option.get().getValue());
			});
		}

		String BLOG_PROMPT_TEMPLATE = promptTemplate.getPromptTemplate();
		String finalPrompt = String.format(BLOG_PROMPT_TEMPLATE,
			prompt.getPostType(),
			snapshotsFormatted,
			prompt.getComment(),
			optionsFormatted
		);

		try {
			ChatResponse response = chatClient.prompt()
				.system(promptTemplate.getSystemPrompt()) // 시스템 메시지 설정
				.user(finalPrompt) // 사용자 메시지 설정
				.call()
				.chatResponse(); // ChatResponse 객체 얻기

			String generatedContent = response.getResult().getOutput().getText();

			String[] generatedContentArray = mapper.readValue(generatedContent, String[].class);
			String postTitle = generatedContentArray[0];
			postTitle = postTitle.replace("#", "").strip();
			String postContent = generatedContentArray[1];

			return CreatedPostResponse.from(postTitle, postContent);
		} catch (Exception e) {
			// Spring AI 관련 예외 또는 API 통신 오류 처리
			log.error("Error calling Claude API via Spring AI: {}", e.getMessage());
			throw new ServerException(AI_SERVER_COMMUNICATION_FAILED);
		}
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

		return RetrievePromptResponse.from(retrievePromptDTO, promptAttachList, promptOptionIds);
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
		// 1. 새롭게 받은 PromptAttachList의 snapshotIds 조회
		Map<Long, PromptAttachEntity> newPromptAttachMap = promptUpdateCommand.getPromptAttachList().stream()
			.map(snapshotCommand -> {
				SnapshotEntity snapshot = snapshotRepository.findById(snapshotCommand.getSnapshotId())
					.orElseThrow(() -> new NotFoundException(SNAPSHOT_NOT_FOUND));
				return snapshotCommand.toPromptAttachDTO(prompt, snapshot).toPromptAttachEntity();
			}).collect(Collectors.toMap(
				promptAttachEntity -> promptAttachEntity.getSnapshot().getId(),
				Function.identity()
			));

		// 2. 기존 PromptAttachEntityList 조회
		List<PromptAttachEntity> allPromptAttachEntities = promptAttachRepository.findByPrompt(prompt);
		Set<Long> newAttachIdSet = newPromptAttachMap.keySet();
		log.info("Updating prompt attach ids: {}", newAttachIdSet);

		/*
		3. 기존에 있고, 새롭게 들어온 PromptAttachList > snapshotIds에도 있으면 isDeleted=false, description 업데이트
		4. 기존에 있고, isDeleted=false인데 새롭게 들어온 PromptAttachList > snapshotIds에 없는 경우 isDeleted=true 로 업데이트
		isDeleted false->false, true->true인 경우에도 구분하지 않고 낙관적으로 처리
		*/
		Set<Long> existingAttachIdSet = new HashSet<>();
		for (PromptAttachEntity promptAttach : allPromptAttachEntities) {
			Long snapshotId = promptAttach.getSnapshot().getId();
			existingAttachIdSet.add(snapshotId);

			if (newAttachIdSet.contains(snapshotId)) { // 기존에 저장되어 있던 snapshot이 수정 요청에 포함된 경우
				promptAttach.setDescription(newPromptAttachMap.get(snapshotId).getDescription());
				promptAttach.setIsDeleted(false);
				newAttachIdSet.remove(snapshotId);
			} else { // 기존에 저장되어 있던 snapshot이 수정 요청에 포함되지 않은 경우
				promptAttach.setIsDeleted(true);
			}
		}

		// 5. 기존에 없었는데 새롭게 들어온 PromptAttachList > snapshotIds에 있는 경우 insert
		for (Long snapshotId : newAttachIdSet) {
			PromptAttachEntity promptAttachEntity = newPromptAttachMap.get(snapshotId);
			promptAttachRepository.save(promptAttachEntity);
		}

		// PromptOption 업데이트
		List<PromptOptionEntity> existingPromptOptions = promptOptionRepository.findByPrompt(prompt);
		Set<Long> newOptionIdSet = Arrays.stream(promptUpdateCommand.getPromptOptionList()).collect(Collectors.toSet());
		updatePromptOptions(existingPromptOptions, newOptionIdSet, prompt);

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
