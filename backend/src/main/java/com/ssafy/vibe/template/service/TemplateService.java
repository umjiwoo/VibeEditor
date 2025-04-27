package com.ssafy.vibe.template.service;

import java.util.List;

import com.ssafy.vibe.template.controller.request.CreateTemplateRequest;
import com.ssafy.vibe.template.controller.request.UpdateTemplateRequest;
import com.ssafy.vibe.template.controller.response.TemplateDetailResponse;
import com.ssafy.vibe.template.service.dto.TemplateDTO;

public interface TemplateService {
	TemplateDetailResponse createTemplate(Long userId, CreateTemplateRequest request);

	TemplateDetailResponse updateTemplate(Long userId, UpdateTemplateRequest request);

	void deleteTemplate(Long userId, Long templateId);

	List<TemplateDTO> getTemplateList(Long userId);

	TemplateDetailResponse getTemplateDetail(Long userId, Long templateId);
}
