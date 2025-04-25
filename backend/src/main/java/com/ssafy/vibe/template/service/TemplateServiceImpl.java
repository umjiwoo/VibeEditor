package com.ssafy.vibe.template.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ssafy.vibe.common.exception.ExceptionCode;
import com.ssafy.vibe.common.exception.NotFoundException;
import com.ssafy.vibe.template.controller.request.UpdateTemplateRequest;
import com.ssafy.vibe.template.controller.response.TemplateDetailResponse;
import com.ssafy.vibe.template.controller.response.TemplateListResponse;
import com.ssafy.vibe.template.domain.TemplateEntity;
import com.ssafy.vibe.template.repository.TemplateRepository;
import com.ssafy.vibe.user.domain.UserEntity;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Transactional
@RequiredArgsConstructor
@Slf4j
@Service
public class TemplateServiceImpl implements TemplateService {

	private final TemplateRepository templateRepository;

	@Override
	public TemplateDetailResponse createTemplate(UserEntity user, String templateName) {
		TemplateEntity template = TemplateEntity.createTemplate(user, templateName);
		templateRepository.save(template);

		return TemplateDetailResponse.from(template);
	}

	@Override
	public TemplateDetailResponse updateTemplate(UserEntity user, UpdateTemplateRequest request) {
		TemplateEntity template = templateRepository.findByIdAndActive(user.getId(), request.templateId())
			.orElseThrow(() -> new NotFoundException(ExceptionCode.TEMPLATE_NOT_FOUND));
		template.updateTemplateName(request.templateName());
		templateRepository.save(template);

		return TemplateDetailResponse.from(template);
	}

	@Override
	public void deleteTemplate(UserEntity user, Long templateId) {
		TemplateEntity template = templateRepository.findByIdAndActive(user.getId(), templateId)
			.orElseThrow(() -> new NotFoundException(ExceptionCode.TEMPLATE_NOT_FOUND));
		template.setIsActive(false);
		templateRepository.save(template);
	}

	@Override
	public TemplateListResponse getTemplateList(UserEntity user) {
		List<TemplateEntity> templates = templateRepository.findByUserIdAndActive(user.getId());

		return TemplateListResponse.from(templates);
	}

	@Override
	public TemplateDetailResponse getTemplateDetail(UserEntity user, Long templateId) {
		TemplateEntity template = templateRepository.findByIdAndActive(user.getId(), templateId)
			.orElseThrow(() -> new NotFoundException(ExceptionCode.TEMPLATE_NOT_FOUND));

		return TemplateDetailResponse.from(template);
	}
}
