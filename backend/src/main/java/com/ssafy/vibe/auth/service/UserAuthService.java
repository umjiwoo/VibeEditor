package com.ssafy.vibe.auth.service;

import com.ssafy.vibe.auth.domain.CustomOAuth2User;
import com.ssafy.vibe.user.domain.ProviderName;
import com.ssafy.vibe.user.domain.UserEntity;
import com.ssafy.vibe.user.repository.UserRepository;
import com.ssafy.vibe.user.service.dto.UserDto;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class UserAuthService extends DefaultOAuth2UserService {
    private final UserRepository userRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);
        String providerName = userRequest.getClientRegistration().getRegistrationId();

        Map<String, Object> attributes = oAuth2User.getAttributes();

        String email = (String) attributes.get("email");
        String name = (String) attributes.get("name");

        String providerUid;
        if (providerName.equals("google")) {
            providerUid = (String) attributes.get("sub"); // Google 고유 ID
        } else if (providerName.equals("github")) {
            providerUid = String.valueOf(attributes.get("id")); // GitHub 고유 ID
        } else {
            throw new OAuth2AuthenticationException("Unsupported provider: " + providerName);
        }

        UserDto userDto = UserDto.createUserDto(name, email, ProviderName.valueOf(providerName), providerUid);
        UserEntity user = userRepository.findByProviderNameAndProviderUid(ProviderName.valueOf(providerName), providerUid)
                .orElseGet(() -> {
                    return userRepository.save(UserEntity.from(userDto));
                });

        user.setLastLoginAt(Instant.now());

        userDto.setUserId(user.getId());
        return new CustomOAuth2User(userDto);
    }
}
