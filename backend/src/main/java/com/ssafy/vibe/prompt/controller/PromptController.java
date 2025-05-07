package com.ssafy.vibe.prompt.controller;

import static com.ssafy.vibe.common.exception.ExceptionCode.*;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.context.support.DefaultMessageSourceResolvable;
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
import com.ssafy.vibe.common.exception.ServerException;
import com.ssafy.vibe.common.schema.BaseResponse;
import com.ssafy.vibe.prompt.controller.request.GeneratePostRequest;
import com.ssafy.vibe.prompt.controller.request.PromptSaveRequest;
import com.ssafy.vibe.prompt.controller.request.PromptUpdateRequest;
import com.ssafy.vibe.prompt.controller.response.CreatedPostResponse;
import com.ssafy.vibe.prompt.controller.response.OptionResponse;
import com.ssafy.vibe.prompt.controller.response.SavedPromptResponse;
import com.ssafy.vibe.prompt.service.PromptService;

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
	public ResponseEntity<BaseResponse<CreatedPostResponse>> generateClaude(
		@Valid @RequestBody GeneratePostRequest generatePostRequest,
		BindingResult bindingResult
	) {
		if (bindingResult.hasErrors()) {
			String errorMessages = bindingResult.getAllErrors().stream()
				.map(DefaultMessageSourceResolvable::getDefaultMessage)
				.collect(Collectors.joining(", "));
			log.warn("Validation failed for blog request: {}", errorMessages);
			throw new ServerException(POST_GENERATE_FAILED);
		}

		try {
			CreatedPostResponse draftPost = promptService.getDraft(generatePostRequest.toCommand());
			return ResponseEntity.ok(BaseResponse.success(draftPost));
		} catch (Exception e) {
			log.error("Error generating blog post: {}", e.getMessage());
			throw new ServerException(POST_GENERATE_FAILED);
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