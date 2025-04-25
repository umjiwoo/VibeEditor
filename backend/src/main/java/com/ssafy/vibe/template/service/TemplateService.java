package com.ssafy.vibe.template.service;

import java.util.List;

import com.ssafy.vibe.template.controller.request.UpdateTemplateRequest;
import com.ssafy.vibe.template.controller.response.TemplateDetailResponse;
import com.ssafy.vibe.template.service.dto.TemplateDTO;
import com.ssafy.vibe.user.domain.UserEntity;

public interface TemplateService {
	TemplateDetailResponse createTemplate(UserEntity user, String templateName);

	TemplateDetailResponse updateTemplate(UserEntity user, UpdateTemplateRequest request);

	void deleteTemplate(UserEntity user, Long templateId);

	List<TemplateDTO> getTemplateList(UserEntity user);

	TemplateDetailResponse getTemplateDetail(UserEntity user, Long templateId);
}
