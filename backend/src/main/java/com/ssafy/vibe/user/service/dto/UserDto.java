package com.ssafy.vibe.user.service.dto;

import com.ssafy.vibe.user.domain.ProviderName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@AllArgsConstructor
@Getter
@Setter
public class UserDto {
    private Long userId;
    private String userName;
    private String email;
    private ProviderName providerName;
    private String providerUid;

    public static UserDto createUserDto(String username, String email,
                                        ProviderName providerName, String providerUid) {
        return UserDto.builder()
                .userName(username)
                .email(email)
                .providerName(providerName)
                .providerUid(providerUid)
                .build();
    }
}