package com.ssafy.vibe.auth.domain;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import com.ssafy.vibe.user.domain.ProviderName;
import com.ssafy.vibe.user.service.dto.UserDto;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class CustomOAuth2User implements OAuth2User {
    private final UserDto userDto;

    @Override
    public Map<String, Object> getAttributes() {
        return Map.of();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singletonList(new SimpleGrantedAuthority("USER"));
    }

    @Override
    public String getName() {
        return userDto.getEmail(); // 또는 providerUid도 가능
    }

    public Long getUserId() {
        return userDto.getUserId();
    }

    public String getUserName() {
        return userDto.getUserName();
    }

    public String getProvider() {
        return userDto.getProviderName().name();
    }

    public String getProviderUid() {
        return userDto.getProviderUid();
    }
}