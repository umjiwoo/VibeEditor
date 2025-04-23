package com.ssafy.vibe.user.domain;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "users")
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

	@Column(name = "provider_name", nullable = false)
	@Enumerated(EnumType.STRING)
	private ProviderName providerName;

	@Column(name = "provider_uid", nullable = false)
	private String providerUid;

	@Column(name = "notion_api")
	private String notionApi;

	@Column(name = "notion_active")
	private Boolean notionActive;

    @Column(name = "last_login_at")
    private ZonedDateTime lastLoginAt;

    public static UserEntity from(UserDto userDto) {
        return UserEntity.builder()
                .userName(userDto.getUserName())
                .email(userDto.getEmail())
                .providerName(userDto.getProviderName())
                .providerUid(userDto.getProviderUid())
                .notionActive(false)
                .build();
    }
}