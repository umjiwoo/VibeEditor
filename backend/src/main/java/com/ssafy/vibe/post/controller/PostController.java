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

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/ai-post")
@RequiredArgsConstructor
public class PostController {

	private final PostService postService;

	@PostMapping("/upload")
	public ResponseEntity<BaseResponse<NotionPostResponse>> uploadPost(
		@AuthenticationPrincipal UserPrincipal userPrincipal,
		@RequestBody NotionPostRequest body
	) {
		NotionPostCommand command = new NotionPostCommand(userPrincipal.getUserId(), body.getPostId());
		NotionPostResponse response = postService.createNotionPost(command).toResponse();
		return ResponseEntity.ok(BaseResponse.success(response));
	}

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
