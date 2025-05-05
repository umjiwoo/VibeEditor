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

import com.ssafy.vibe.prompt.service.command.PromptCommand;
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

	// 프롬프트 템플릿 (이전과 동일하게 사용 가능)
	// (가독성을 위해 유지하거나, 필요시 PromptTemplate 객체 사용 고려)
	private static final String BLOG_PROMPT_TEMPLATE = """
		# 기술 블로그 포스트 생성 요청
		
		당신은 주어진 정보를 바탕으로 명확하고 유익한 기술 블로그 포스트를 Markdown 형식으로 작성하는 AI 어시스턴트입니다.
		
		## 제공 정보
		
		다음은 블로그 포스트 작성을 위해 제공되는 정보입니다. 각 항목을 주의 깊게 분석하여 포스트 내용에 반영해주세요.
		
		* **`postType`**: %s
		* **`snapshot`**:
		    ```
		    %s
		    ```
		* **`snapshotDescription`**: %s
		* **`userComment`**: %s
		* **`option`**: %s
		
		## 작성 가이드라인
		
		1.  **형식**: 최종 결과물은 반드시 **Markdown 형식**이어야 합니다.
		2.  **구조**: 다음과 같은 명확한 구조를 갖춰주세요.
		    * **제목**: `postType`과 핵심 내용을 반영하는 매력적인 제목 (Markdown H1 또는 H2 사용)
		    * **서론**: 포스트의 주제와 목적을 간략하게 소개합니다. `userComment`의 내용을 참고하여 독자의 흥미를 유발할 수 있습니다.
		    * **본론**:
		        * `snapshotDescription`을 바탕으로 `snapshot`의 내용을 상세하게 설명합니다.
		        * 코드는 Markdown 코드 블록(```)으로 감싸고, 적절한 언어(예: ```java)를 명시해주세요.
		        * `postType`이 '트러블슈팅'인 경우, 문제 상황, 원인 분석, 해결 과정을 명확히 제시합니다.
		        * `postType`이 'CS 정리'인 경우, 개념을 이해하기 쉽게 설명하고 `snapshot`을 예시로 활용합니다.
		    * **결론**: 내용을 요약하고, 독자에게 도움이 될 만한 추가 정보나 제언을 포함할 수 있습니다.
		3.  **내용**:
		    * 제공된 모든 정보 (`postType`, `snapshot`, `snapshotDescription`, `userComment`)를 충실히 반영하여 작성합니다.
		    * `userComment`에서 강조된 부분을 특히 신경 써주세요.
		4.  **스타일**:
		    * `option`에 명시된 이모지 사용 여부와 말투(~요 체 / ~습니다 체)를 일관되게 적용합니다.
		    * 전문적이면서도 이해하기 쉬운 언어를 사용합니다.
		
		**이제 위의 정보를 바탕으로 완성된 기술 블로그 포스트를 Markdown 형식으로 작성해주세요.**
		""";
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
	public String getAnswer(PromptCommand command) {
		// 1. 프롬프트 문자열 완성하기 (이전과 동일)
		String finalPrompt = String.format(BLOG_PROMPT_TEMPLATE,
			command.getPostType(),
			command.getSnapshot(),
			command.getSnapshotDescription(),
			command.getUserComment(),
			command.getOption()
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
			log.info("Successfully received blog content from Claude via Spring AI.");
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
