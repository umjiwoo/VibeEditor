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
import com.ssafy.vibe.notion.domain.NotionDatabaseEntity;
import com.ssafy.vibe.notion.repository.NotionDatabaseRepository;
import com.ssafy.vibe.post.domain.PostType;
import com.ssafy.vibe.prompt.controller.response.OptionResponse;
import com.ssafy.vibe.prompt.controller.response.PromptAttachListRespnose;
import com.ssafy.vibe.prompt.controller.response.SavedPromptResponse;
import com.ssafy.vibe.prompt.domain.OptionEntity;
import com.ssafy.vibe.prompt.domain.PromptAttachEntity;
import com.ssafy.vibe.prompt.domain.PromptEntity;
import com.ssafy.vibe.prompt.domain.PromptOptionEntity;
import com.ssafy.vibe.prompt.repository.OptionRepository;
import com.ssafy.vibe.prompt.repository.PromptAttachRepository;
import com.ssafy.vibe.prompt.repository.PromptOptionRepository;
import com.ssafy.vibe.prompt.repository.PromptRepository;
import com.ssafy.vibe.prompt.service.command.GeneratePostCommand;
import com.ssafy.vibe.prompt.service.command.PromptSaveCommand;
import com.ssafy.vibe.prompt.service.command.PromptUpdateCommand;
import com.ssafy.vibe.prompt.service.command.SnapshotCommand;
import com.ssafy.vibe.prompt.service.dto.OptionItemDTO;
import com.ssafy.vibe.prompt.service.dto.PromptAttachDTO;
import com.ssafy.vibe.prompt.service.dto.PromptDTO;
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
	private PromptTemplate promptTemplate;
	private final UserRepository userRepository;
	private final SnapshotRepository snapshotRepository;
	private final OptionRepository optionRepository;
	private final TemplateRepository templateRepository;
	private PromptRepository promptRepository;
	private PromptAttachRepository promptAttachRepository;
	private PromptOptionRepository promptOptionRepository;
	private NotionDatabaseRepository notionDatabaseRepository;

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

	/**
	 * 사용자 요청을 기반으로 기술 블로그 포스트 생성을 Claude API에 요청합니다. (Spring AI 사용)
	 * @param command 사용자 입력 데이터 DTO
	 * @return 생성된 Markdown 형식의 블로그 포스트 내용
	 */
	@Override
	public String getDraft(GeneratePostCommand generatePostCommand) {
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

		log.debug("Generated Prompt for Claude:\n{}", finalPrompt);

		try {
			// 2. Spring AI ChatClient를 사용하여 API 호출
			// 방법 1: Prompt 객체 사용
			// Prompt prompt = new Prompt(new UserMessage(finalPrompt));
			// ChatResponse response = chatClient.call(prompt);

			// 방법 2: ChatClient.Builder 스타일 (더 유연함)
			ChatResponse response = chatClient.prompt()
				.user(finalPrompt) // 사용자 메시지 설정
				// .system(...) // 필요시 시스템 메시지 추가
				.call()
				.chatResponse(); // ChatResponse 객체 얻기

			// 3. 결과에서 Markdown 텍스트 추출
			String generatedContent = response.getResult().getOutput().getText();
			// ArrayNode result = MarkdownToNotionConverter.convertMarkdownToBlocks(generatedContent);
			log.info("Successfully received blog content from Claude via Spring AI.");
			// return generatedContent;
			// return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(result);
			return generatedContent;
		} catch (Exception e) {
			// Spring AI 관련 예외 또는 API 통신 오류 처리
			log.error("Error calling Claude API via Spring AI: {}", e.getMessage(), e);
			// 실제 서비스에서는 더 구체적인 예외 처리 필요
			throw new RuntimeException("Claude API 호출 중 오류 발생 (Spring AI)", e);
		}
	}

	@Override
	public void savePrompt(Long userId, PromptSaveCommand promptSaveCommand) {
		TemplateEntity template = templateRepository.findById(promptSaveCommand.getTemplateId())
			.orElseThrow(() -> new NotFoundException(TEMPLATE_NOT_FOUND));

		UserEntity user = userRepository.findById(userId)
			.orElseThrow(() -> new NotFoundException(USER_NOT_FOUND));

		NotionDatabaseEntity notionDatabseEntity = notionDatabaseRepository.findById(
				promptSaveCommand.getNotionDatabaseId())
			.orElseThrow(() -> new NotFoundException(NOTION_DATABASE_NOT_FOUND));

		Long parentPromptId = promptSaveCommand.getParentPromptId();
		PromptEntity parentPrompt = null;
		if (parentPromptId != null) {
			parentPrompt = promptRepository.findById(promptSaveCommand.getParentPromptId())
				.orElseThrow(() -> new NotFoundException(PROMPT_NOT_FOUND));
		}
		PromptEntity prompt = promptRepository.save(
			promptSaveCommand.toPromptDTO(parentPrompt, template, user, notionDatabseEntity).toPromptEntity());

		if (parentPrompt == null) {
			prompt.setParentPrompt(prompt);
		}

		List<PromptAttachEntity> promptAttachList = buildPromptAttachments(prompt,
			promptSaveCommand.getPromptAttachList());
		promptAttachRepository.saveAll(promptAttachList);
		prompt.getAttachments().addAll(promptAttachList);

		List<PromptOptionEntity> promptOptions = buildPromptOptions(prompt, promptSaveCommand.getPromptOptionList());
		promptOptionRepository.saveAll(promptOptions);
		prompt.getPromptOptions().addAll(promptOptions);

		promptRepository.save(prompt);
	}

	@Override
	public SavedPromptResponse getPrompt(Long userId, Long promptId) {
		PromptEntity prompt = promptRepository.findById(promptId)
			.orElseThrow(() -> new NotFoundException(PROMPT_NOT_FOUND));

		checkPromptUser(userId, prompt.getUser().getId());

		PromptDTO promptDTO = PromptDTO.fromPromptEntity(prompt);
		List<PromptAttachEntity> promptAttachEntityList = promptAttachRepository.findByPromptAndIsDeletedFalse(prompt);
		List<PromptOptionEntity> promptOptionEntityList = promptOptionRepository.findByPromptAndIsDeletedFalse(prompt);

		List<PromptAttachListRespnose> promptAttachList = promptAttachEntityList.stream()
			.map(promptAttachEntity ->
				PromptAttachListRespnose.from(
					promptAttachEntity.getId(),
					PromptAttachDTO.from(promptAttachEntity))
			).collect(Collectors.toList());

		Long[] promptOptionIds = promptOptionEntityList.stream()
			.map(promptOptionEntity -> promptOptionEntity.getOption().getId())
			.toArray(Long[]::new);

		return SavedPromptResponse.from(promptDTO, promptAttachList, promptOptionIds);
	}

	@Override
	public void updatePrompt(Long userId, Long promptId, PromptUpdateCommand promptUpdateCommand) {
		PromptEntity prompt = promptRepository.findById(promptId)
			.orElseThrow(() -> new NotFoundException(PROMPT_NOT_FOUND));

		checkPromptUser(userId, prompt.getUser().getId());

		prompt.setPromptName(promptUpdateCommand.getPromptName());
		prompt.setPostType(PostType.valueOf(promptUpdateCommand.getPostType()));
		prompt.setComment(promptUpdateCommand.getComment());

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

	public void updatePromptOptions(
		List<PromptOptionEntity> existingPromptOptions,
		Set<Long> newOptionIdSet,
		PromptEntity prompt) {
		existingPromptOptions.forEach(option -> option.setIsDeleted(true));

		existingPromptOptions.forEach(promptOption -> {
			Long existingOptionId = promptOption.getOption().getId();
			if (newOptionIdSet.contains(existingOptionId)) {
				promptOption.setIsDeleted(false);
				newOptionIdSet.remove(existingOptionId);
			}
		});

		List<PromptOptionEntity> newPromptOptions = buildPromptOptions(
			prompt,
			newOptionIdSet.toArray(new Long[newOptionIdSet.size()]));
		promptOptionRepository.saveAll(newPromptOptions);
	}

	public void checkPromptUser(Long userId, Long promptUserId) {
		if (!userId.equals(promptUserId)) {
			throw new BadRequestException(OWNER_MISMATCH);
		}
	}

	public List<PromptAttachEntity> buildPromptAttachments(
		PromptEntity prompt,
		List<SnapshotCommand> snapshotCommandList) {
		return snapshotCommandList.stream()
			.map(attachment -> {
				SnapshotEntity snapshot = snapshotRepository.findById(attachment.getSnapshotId())
					.orElseThrow(() -> new NotFoundException(SNAPSHOT_NOT_FOUND));
				return attachment.toPromptAttachDTO(prompt, snapshot).toPromptAttachEntity();
			})
			.collect(Collectors.toList());
	}

	public List<PromptOptionEntity> buildPromptOptions(
		PromptEntity prompt,
		Long[] promptOptionIds) {
		return Arrays.stream(promptOptionIds)
			.map(optionId -> {
				OptionEntity option = optionRepository.findById(optionId)
					.orElseThrow(() -> new NotFoundException(OPTION_NOT_FOUND));
				return PromptOptionEntity.from(prompt, option);
			})
			.toList();
	}

}
