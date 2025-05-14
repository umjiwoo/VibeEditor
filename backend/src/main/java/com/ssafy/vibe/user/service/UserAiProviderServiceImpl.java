package com.ssafy.vibe.user.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ssafy.vibe.common.exception.BadRequestException;
import com.ssafy.vibe.common.exception.ExceptionCode;
import com.ssafy.vibe.common.util.Aes256Util;
import com.ssafy.vibe.user.controller.request.UserAiCreateRequest;
import com.ssafy.vibe.user.controller.request.UserAiUpdateRequest;
import com.ssafy.vibe.user.controller.response.UserAiResponse;
import com.ssafy.vibe.user.domain.AiProviderEntity;
import com.ssafy.vibe.user.domain.UserAiProviderEntity;
import com.ssafy.vibe.user.domain.UserEntity;
import com.ssafy.vibe.user.helper.UserHelper;
import com.ssafy.vibe.user.repository.AiProviderRepository;
import com.ssafy.vibe.user.repository.UserAiProviderRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Transactional
@RequiredArgsConstructor
@Slf4j
@Service
public class UserAiProviderServiceImpl implements UserAiProviderService {

	private final UserAiProviderRepository userAiProviderRepository;
	private final AiProviderRepository aiProviderRepository;
	private final Aes256Util aes256Util;
	private final UserHelper userHelper;

	@Override
	public void registerUserAPIKey(Long userId, UserAiCreateRequest request) {
		UserEntity user = userHelper.getUser(userId);
		List<AiProviderEntity> aiProviders = aiProviderRepository.findByBrand(request.brand());
		if (aiProviders.isEmpty()) {
			throw new BadRequestException(ExceptionCode.AI_BRAND_NOT_FOUND);
		}

		String encryptedApiKey = aes256Util.encrypt(request.apiKey());
		aiProviders.forEach(aiProvider -> {
			UserAiProviderEntity userAiProvider = UserAiProviderEntity.createUserAiProvider(
				encryptedApiKey, false, user, aiProvider
			);
			userAiProviderRepository.save(userAiProvider);
		});
	}

	@Override
	public void updateUserAPIKey(Long userId, UserAiUpdateRequest request) {
		String encryptedApiKey = aes256Util.encrypt(request.apiKey());
		List<UserAiProviderEntity> userAiProviders = userAiProviderRepository.findUserAiProviderByBrand(
			userId, request.brand()
		);
		userAiProviders.forEach(userAiProvider -> {
			userAiProvider.updateApiKey(encryptedApiKey);
			userAiProviderRepository.save(userAiProvider);
		});
	}

	@Override
	public List<UserAiResponse> getAiProviderList() {
		return List.of();
	}
}
