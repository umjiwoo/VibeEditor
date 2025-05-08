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
import com.ssafy.vibe.prompt.controller.request.PostGenerateRequest;
import com.ssafy.vibe.prompt.controller.request.PromptSaveRequest;
import com.ssafy.vibe.prompt.controller.request.PromptUpdateRequest;
import com.ssafy.vibe.prompt.controller.response.CreatedPostResponse;
import com.ssafy.vibe.prompt.controller.response.OptionResponse;
import com.ssafy.vibe.prompt.controller.response.RetrievePromptResponse;
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
		@Valid @RequestBody PostGenerateRequest postGenerateRequest,
		BindingResult bindingResult
	) {
		if (bindingResult.hasErrors()) {
			String errorMessages = bindingResult.getAllErrors().stream()
				.map(DefaultMessageSourceResolvable::getDefaultMessage)
				.collect(Collectors.joining(", "));
			log.warn("Validation failed for blog request: {}", errorMessages);
			throw new ServerException(POST_GENERATE_FAILED);
		}

		CreatedPostResponse draftPost = promptService.getDraft(postGenerateRequest.toCommand());
		return ResponseEntity.ok(BaseResponse.success(draftPost));
	}

	@PostMapping
	public ResponseEntity<BaseResponse<String>> savePrompt(
		@AuthenticationPrincipal UserPrincipal userPrincipal,
		@Valid @RequestBody PromptSaveRequest promptRequest
	) {
		Long userId = userPrincipal.getUserId();
		promptService.savePrompt(userId, promptRequest.toCommand());
		return ResponseEntity.ok(BaseResponse.success(null));
	}

	@GetMapping("/{promptId}")
	public ResponseEntity<RetrievePromptResponse> getPrmopt(
		@AuthenticationPrincipal UserPrincipal userPrincipal,
		@PathVariable("promptId") Long promptId
	) {
		Long userId = userPrincipal.getUserId();
		RetrievePromptResponse retrievePromptResponse = promptService.getPrompt(userId, promptId);
		return ResponseEntity.ok(retrievePromptResponse);
	}

	@PutMapping("/{promptId}")
	public ResponseEntity<BaseResponse<Void>> updatePrompt(
		@AuthenticationPrincipal UserPrincipal userPrincipal,
		@PathVariable("promptId") Long promptId,
		@Valid @RequestBody PromptUpdateRequest promptupdateRequest
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