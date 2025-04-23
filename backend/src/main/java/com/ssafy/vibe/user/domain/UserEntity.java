package com.ssafy.vibe.user.domain;

import com.ssafy.vibe.common.domain.BaseEntity;
import com.ssafy.vibe.user.service.dto.UserDto;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "Users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserEntity extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long id;

    @Column(name = "user_name", nullable = false)
    private String userName;

    @Column(name = "email", nullable = false)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(name = "provider_name", nullable = false)
    private ProviderName providerName;

    @Column(name = "provider_uid", nullable = false)
    private String providerUid;

    @Column(name = "notion_api")
    private String notionApi;

    @Column(name = "notion_active")
    private Boolean notionActive = false;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(name = "last_login_at")
    private Instant lastLoginAt;

    public static UserEntity from(UserDto userDto) {
        return UserEntity.builder()
                .userName(userDto.getUserName())
                .email(userDto.getEmail())
                .providerName(userDto.getProviderName())
                .providerUid(userDto.getProviderUid())
                .build();
    }
}