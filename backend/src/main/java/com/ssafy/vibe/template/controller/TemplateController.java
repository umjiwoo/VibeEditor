package com.ssafy.vibe.template.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.ssafy.vibe.auth.domain.UserPrincipal;
import com.ssafy.vibe.common.schema.BaseResponse;
import com.ssafy.vibe.template.controller.request.CreateTemplateRequest;
import com.ssafy.vibe.template.controller.request.UpdateTemplateRequest;
import com.ssafy.vibe.template.controller.response.TemplateDetailResponse;
import com.ssafy.vibe.template.service.TemplateServiceImpl;
import com.ssafy.vibe.template.service.dto.TemplateDTO;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/template")
public class TemplateController {

	private final TemplateServiceImpl templateService;

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public ResponseEntity<?> createTemplate(
		@AuthenticationPrincipal UserPrincipal userPrincipal,
		@Valid @RequestBody CreateTemplateRequest request) {
		templateService.createTemplate(userPrincipal.getUserId(), request);
		return ResponseEntity.ok(BaseResponse.success(null));
	}

	@PutMapping
	public ResponseEntity<?> updateTemplate(
		@AuthenticationPrincipal UserPrincipal userPrincipal,
		@Valid @RequestBody UpdateTemplateRequest request) {
		templateService.updateTemplate(userPrincipal.getUserId(), request);
		return ResponseEntity.ok(BaseResponse.success(null));
	}

	@DeleteMapping("/{templateId}")
	public ResponseEntity<?> deleteTemplate(
		@AuthenticationPrincipal UserPrincipal userPrincipal,
		@PathVariable("templateId") Long templateId) {
		templateService.deleteTemplate(userPrincipal.getUserId(), templateId);
		return ResponseEntity.ok(BaseResponse.success(null));
	}

	@GetMapping
	public ResponseEntity<?> getTemplateList(
		@AuthenticationPrincipal UserPrincipal userPrincipal
	) {
		List<TemplateDTO> response = templateService.getTemplateList(userPrincipal.getUserId());
		return ResponseEntity.ok(BaseResponse.success(response));
	}

	@GetMapping("/{templateId}")
	public ResponseEntity<?> getTemplateDetail(
		@AuthenticationPrincipal UserPrincipal userPrincipal,
		@PathVariable("templateId") Long templateId) {
		TemplateDetailResponse response = templateService.getTemplateDetail(userPrincipal.getUserId(), templateId);
		return ResponseEntity.ok(BaseResponse.success(response));
	}
}
