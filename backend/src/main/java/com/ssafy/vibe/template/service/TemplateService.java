package com.ssafy.vibe.template.service;

import com.ssafy.vibe.template.controller.request.UpdateTemplateRequest;
import com.ssafy.vibe.template.controller.response.TemplateDetailResponse;
import com.ssafy.vibe.template.controller.response.TemplateListResponse;
import com.ssafy.vibe.user.domain.UserEntity;

public interface TemplateService {
	TemplateDetailResponse createTemplate(UserEntity user, String templateName);

	TemplateDetailResponse updateTemplate(UserEntity user, UpdateTemplateRequest request);

	void deleteTemplate(UserEntity user, Long templateId);

	TemplateListResponse getTemplateList(UserEntity user);

	TemplateDetailResponse getTemplateDetail(UserEntity user, Long templateId);
}
