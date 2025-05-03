package com.ssafy.vibe.notion.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ssafy.vibe.auth.domain.UserPrincipal;
import com.ssafy.vibe.common.schema.BaseResponse;
import com.ssafy.vibe.notion.controller.request.NotionConnectRequest;
import com.ssafy.vibe.notion.controller.request.NotionDatabaseInfoRequest;
import com.ssafy.vibe.notion.service.NotionService;
import com.ssafy.vibe.notion.service.command.NotionConnectInfoCommand;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@Tag(
	name = "Notion Controller",
	description = "노션 "
)
@RequestMapping("/api/v1/notion")
@RestController
@RequiredArgsConstructor
public class NotionController {

	private final NotionService notionService;

	@Operation(
		summary = "노션 API 키 등록",
		description = "사용자 생성한 노션 API 키 등록"
	)
	@PostMapping("/secretkey")
	public ResponseEntity<BaseResponse<Void>> saveNotionKey(
		@RequestBody NotionConnectRequest body,
		@AuthenticationPrincipal UserPrincipal principal
	) {
		NotionConnectInfoCommand command = new NotionConnectInfoCommand(principal.getUserId(),
			body.getNotionSecretKey());
		notionService.saveNotionKey(command);
		return ResponseEntity.ok(
			BaseResponse.success(
				null
			)
		);
	}

	@Operation(
		summary = "",
		description = ""
	)
	@PostMapping("/database")
	public ResponseEntity<BaseResponse<Void>> registerNotionDatabase(
		@RequestBody NotionDatabaseInfoRequest body,
		@AuthenticationPrincipal UserPrincipal userPrincipal
	) {
		// TODO : 노션 데이터베이스 등록
		return ResponseEntity.ok(
			BaseResponse.success(
				null
			)
		);
	}

	@Operation(
		summary = "",
		description = ""
	)
	@DeleteMapping("/database/{databaseId}")
	public ResponseEntity<BaseResponse<Void>> deleteNotionDatabase(
		@PathVariable Long databaseId,
		@AuthenticationPrincipal UserPrincipal principal
	) {
		// TODO : 데이터 베이스 삭제
		return ResponseEntity.ok(
			BaseResponse.success(
				null
			)
		);
	}

	@Operation(
		summary = "",
		description = ""
	)
	@GetMapping("/databases")
	public ResponseEntity<BaseResponse<Void>> retrieveNotionDatabases(
		@RequestBody NotionConnectRequest body,
		@AuthenticationPrincipal UserPrincipal principal
	) {
		// TODO : 등록된 노션 데이터베이스 조회.
		return ResponseEntity.ok(
			BaseResponse.success(
				null
			)
		);
	}

}
