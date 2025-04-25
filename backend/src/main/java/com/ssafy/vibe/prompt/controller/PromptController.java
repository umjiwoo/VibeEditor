package com.ssafy.vibe.prompt.controller;

import java.util.stream.Collectors;

import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ssafy.vibe.common.schema.BaseResponse;
import com.ssafy.vibe.prompt.controller.request.PromptRequest;
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

	@PostMapping("/test/claude")
	@Operation(
		summary = "클로드 연결 테스트",
		description = "클로드 API와 연동이 성공적으로 되었는지 간단한 테스트를 합니다."
	)
	public ResponseEntity<BaseResponse<String>> generateClaude(
		@Valid @RequestBody PromptRequest body,
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
			log.info("Received request to generate blog post: type={}, option={}",
				body.getPostType(), body.getOption()); // 요청 로깅
			String markdownBlog = promptService.getAnswer(body.toCommand());
			return ResponseEntity.ok(BaseResponse.success(markdownBlog));
		} catch (Exception e) {
			log.error("Error generating blog post: {}", e.getMessage(), e); // 에러 로깅
			// 서비스에서 발생한 예외 처리 (전역 예외 처리기 @ControllerAdvice 사용 권장)
			return ResponseEntity.internalServerError()
				.contentType(MediaType.TEXT_PLAIN)
				.body(BaseResponse.error("블로그 생성 중 서버 오류가 발생했습니다."));
		}
	}

	// @PostMapping("/test/openai")
	// @Operation(
	// 	summary = "GPT 연결 테스트",
	// 	description = "GPT API와 연동이 성공적으로 되었는지 간단한 테스트를 합니다."
	// )
	// public ResponseEntity<BaseResponse<String>> generateGpt(
	// 	@RequestBody Map<String, String> body
	// ) {
	// 	String prompt = body.get("prompt");
	// 	if (prompt == null || prompt.isEmpty()) {
	// 		throw new BadRequestException("prompt is empty");
	// 	}
	//
	// 	String response = promptService.getAnswer(prompt);
	// 	return ResponseEntity.ok(BaseResponse.success(response));
	// }

}