package com.ssafy.vibe.prompt.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ssafy.vibe.auth.domain.UserPrincipal;
import com.ssafy.vibe.common.schema.BaseResponse;
import com.ssafy.vibe.prompt.controller.request.GeneratePostRequest;
import com.ssafy.vibe.prompt.controller.request.PromptSaveRequest;
import com.ssafy.vibe.prompt.controller.request.PromptUpdateRequest;
import com.ssafy.vibe.prompt.controller.response.OptionResponse;
import com.ssafy.vibe.prompt.controller.response.SavedPromptResponse;
import com.ssafy.vibe.prompt.service.PromptService;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/v1/prompt")
@RequiredArgsConstructor
@Slf4j
public class PromptController {

	private final PromptService promptService;

	@PostMapping("/ai-post")
	@Operation(
		summary = "클로드 연결 테스트",
		description = "클로드 API와 연동이 성공적으로 되었는지 간단한 테스트를 합니다."
	)
	public ResponseEntity<BaseResponse<String>> generateClaude(
		@Valid @RequestBody GeneratePostRequest generatePostRequest,
		BindingResult bindingResult
	) {
		if (bindingResult.hasErrors()) {
			String errorMessages = bindingResult.getAllErrors().stream()
				.map(DefaultMessageSourceResolvable::getDefaultMessage)
				.collect(Collectors.joining(", "));
			log.warn("Validation failed for blog request: {}", errorMessages); // 로깅 추가
			return ResponseEntity.badRequest()
				.contentType(MediaType.TEXT_PLAIN)
				.body(BaseResponse.error("입력값 오류: " + errorMessages));
		}

		try {
			String markdownBlog = promptService.getDraft(generatePostRequest.toGeneratePostCommand());
			return ResponseEntity.ok(BaseResponse.success(markdownBlog));
		} catch (Exception e) {
			log.error("Error generating blog post: {}", e.getMessage(), e);
			// 서비스에서 발생한 예외 처리 (전역 예외 처리기 @ControllerAdvice 사용 권장)
			return ResponseEntity.internalServerError()
				.contentType(MediaType.TEXT_PLAIN)
				.body(BaseResponse.error("포스트 생성 중 서버 오류가 발생했습니다."));
		}
	}

	@PostMapping
	public ResponseEntity<BaseResponse<String>> savePrompt(
		@AuthenticationPrincipal UserPrincipal userPrincipal,
		@RequestBody PromptSaveRequest promptRequest
	) {
		Long userId = userPrincipal.getUserId();
		promptService.savePrompt(userId, promptRequest.toCommand());
		return ResponseEntity.ok(BaseResponse.success(null));
	}

	@GetMapping("/{promptId}")
	public ResponseEntity<SavedPromptResponse> getPrmopt(
		@AuthenticationPrincipal UserPrincipal userPrincipal,
		@PathVariable Long promptId
	) {
		Long userId = userPrincipal.getUserId();
		SavedPromptResponse savedPromptResponse = promptService.getPrompt(userId, promptId);
		return ResponseEntity.ok(savedPromptResponse);
	}

	@PutMapping("/{promptId}")
	public ResponseEntity<BaseResponse<String>> updatePrompt(
		@AuthenticationPrincipal UserPrincipal userPrincipal,
		@PathVariable Long promptId,
		@RequestBody PromptUpdateRequest promptupdateRequest
	) {
		Long userId = userPrincipal.getUserId();
		promptService.updatePrompt(userId, promptId, promptupdateRequest.toCommand());

		return ResponseEntity.ok(BaseResponse.success(null));
	}

	@GetMapping("/option")
	public ResponseEntity<List<OptionResponse>> getOptionList() {
		List<OptionResponse> options = promptService.getOptionList();
		return ResponseEntity.ok(options);
	}
}