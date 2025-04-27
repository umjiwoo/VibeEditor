package com.ssafy.vibe.template.service;

import static org.junit.jupiter.api.Assertions.*;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import com.ssafy.vibe.common.exception.NotFoundException;
import com.ssafy.vibe.template.controller.request.CreateTemplateRequest;
import com.ssafy.vibe.template.controller.request.UpdateTemplateRequest;
import com.ssafy.vibe.template.domain.TemplateEntity;
import com.ssafy.vibe.template.repository.TemplateRepository;
import com.ssafy.vibe.user.domain.ProviderName;
import com.ssafy.vibe.user.domain.UserEntity;
import com.ssafy.vibe.user.repository.UserRepository;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
class TemplateServiceImplTest {

	@Autowired
	TemplateService templateService;

	@Autowired
	UserRepository userRepository;
	@Autowired
	TemplateRepository templateRepository;

	UserEntity user, otherUser;
	TemplateEntity template;

	@BeforeEach
	void setUp() {
		user = UserEntity.createUser("유저이름", "email@naver.com", ProviderName.github, "uid-uid");
		user = userRepository.save(user);

		otherUser = UserEntity.createUser("익명유저", "test@x.com", ProviderName.google, "anonymous-uid");
		otherUser = userRepository.save(otherUser);

		template = TemplateEntity.createTemplate(user, "초기 템플릿");
		template = templateRepository.save(template);
	}

	@Test
	void 템플릿생성_성공() {
		CreateTemplateRequest request = new CreateTemplateRequest("템플릿이름");
		assertEquals(1, templateRepository.findByUserIdAndActive(user.getId()).size());
		templateService.createTemplate(user.getId(), request);
		assertEquals(2, templateRepository.findByUserIdAndActive(user.getId()).size());
	}

	@Test
	void 템플릿이름변경_성공() {
		assertEquals("초기 템플릿", template.getTemplateName());
		UpdateTemplateRequest request = new UpdateTemplateRequest(template.getId(), "템플릿이름수정");
		templateService.updateTemplate(user.getId(), request);
		assertEquals("템플릿이름수정", template.getTemplateName());
	}

	@Test
	void 템플릿이름변경_실패_템플릿권한없음() {
		assertEquals("초기 템플릿", template.getTemplateName());
		UpdateTemplateRequest request = new UpdateTemplateRequest(template.getId(), "템플릿이름수정");
		Assertions.assertThatThrownBy(() -> templateService.updateTemplate(otherUser.getId(), request))
			.isInstanceOf(NotFoundException.class)
			.hasMessage("템플릿이 존재하지 않습니다.");
	}

	@Test
	void 템플릿삭제_성공() {
		assertEquals(1, templateRepository.findByUserIdAndActive(user.getId()).size());
		templateService.deleteTemplate(user.getId(), template.getId());
		assertEquals(0, templateRepository.findByUserIdAndActive(user.getId()).size());
	}

	@Test
	void 템플릿삭제_실패_템플릿권한없음() {
		Assertions.assertThatThrownBy(() -> templateService.deleteTemplate(otherUser.getId(), template.getId()))
			.isInstanceOf(NotFoundException.class)
			.hasMessage("템플릿이 존재하지 않습니다.");
	}
}