package com.ssafy.vibe.template.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.ssafy.vibe.common.schema.BaseResponse;
import com.ssafy.vibe.template.controller.request.UpdateTemplateRequest;
import com.ssafy.vibe.template.controller.response.TemplateDetailResponse;
import com.ssafy.vibe.template.service.TemplateServiceImpl;
import com.ssafy.vibe.template.service.dto.TemplateDTO;
import com.ssafy.vibe.user.domain.UserEntity;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/template")
public class TemplateController {

	private final TemplateServiceImpl templateService;

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public ResponseEntity<?> createTemplate(
		UserEntity user,
		String templateName) {
		TemplateDetailResponse response = templateService.createTemplate(user, templateName);
		return ResponseEntity.ok(BaseResponse.success(response));
	}

	@PutMapping
	public ResponseEntity<?> updateTemplate(
		UserEntity user,
		UpdateTemplateRequest request) {
		TemplateDetailResponse response = templateService.updateTemplate(user, request);
		return ResponseEntity.ok(BaseResponse.success(response));
	}

	@DeleteMapping("/{templateId}")
	public ResponseEntity<?> deleteTemplate(
		UserEntity user,
		@PathVariable("templateId") Long templateId) {
		templateService.deleteTemplate(user, templateId);
		return ResponseEntity.ok(BaseResponse.success(null));
	}

	@GetMapping
	public ResponseEntity<?> getTemplateList(UserEntity user) {
		List<TemplateDTO> response = templateService.getTemplateList(user);
		return ResponseEntity.ok(BaseResponse.success(response));
	}

	@GetMapping("/{templateId}")
	public ResponseEntity<?> getTemplateDetail(
		UserEntity user,
		@PathVariable("templateId") Long templateId) {
		TemplateDetailResponse response = templateService.getTemplateDetail(user, templateId);
		return ResponseEntity.ok(BaseResponse.success(response));
	}
}
