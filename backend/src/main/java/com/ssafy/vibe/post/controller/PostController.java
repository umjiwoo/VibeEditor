package com.ssafy.vibe.post.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ssafy.vibe.auth.domain.UserPrincipal;
import com.ssafy.vibe.common.schema.BaseResponse;
import com.ssafy.vibe.post.controller.request.NotionPostRequest;
import com.ssafy.vibe.post.controller.request.NotionUpdateRequest;
import com.ssafy.vibe.post.controller.response.NotionPostResponse;
import com.ssafy.vibe.post.service.PostService;
import com.ssafy.vibe.post.service.command.NotionPostCommand;
import com.ssafy.vibe.post.service.command.NotionUpdateCommand;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@Tag(
	name = "Post Controller",
	description = "AI 게시글 관련 기능 (노션 업로드, 수정 등)"
)
@RestController
@RequestMapping("/api/v1/ai-post")
@RequiredArgsConstructor
public class PostController {

	private final PostService postService;

	@Operation(
		summary = "✅ AI 노션 업로드",
		description = "선택한 AI 게시글을 사용자의 노션에 업로드합니다."
	)
	@PostMapping("/upload")
	public ResponseEntity<BaseResponse<NotionPostResponse>> uploadPost(
		@AuthenticationPrincipal UserPrincipal userPrincipal,
		@RequestBody NotionPostRequest body
	) {
		NotionPostCommand command = new NotionPostCommand(userPrincipal.getUserId(), body.getPostId());
		NotionPostResponse response = postService.createNotionPost(command).toResponse();
		return ResponseEntity.ok(BaseResponse.success(response));
	}

	@Operation(
		summary = "✅게시글 제목/내용 수정",
		description = "AI 생성 게시글의 제목과 내용을 수정합니다."
	)
	@PutMapping("/{postId}")
	public ResponseEntity<BaseResponse<Boolean>> updatePost(
		@AuthenticationPrincipal UserPrincipal userPrincipal,
		@PathVariable("postId") Long postId,
		@RequestBody NotionUpdateRequest body
	) {
		NotionUpdateCommand command = body.toCommand(userPrincipal.getUserId(), postId, body);
		boolean response = postService.updateNotionPost(command);
		return ResponseEntity.ok(BaseResponse.success(response));
	}

}
