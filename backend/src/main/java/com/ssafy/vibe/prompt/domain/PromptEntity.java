package com.ssafy.vibe.prompt.domain;

import java.util.ArrayList;
import java.util.List;

import com.ssafy.vibe.common.domain.BaseEntity;
import com.ssafy.vibe.post.domain.PostType;
import com.ssafy.vibe.template.domain.TemplateEntity;
import com.ssafy.vibe.user.domain.UserEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "prompt")
public class PromptEntity extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "prompt_id")
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "parent_prompt_id", nullable = false)
	private PromptEntity parentPrompt;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "template_id", nullable = false)
	private TemplateEntity template;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false)
	private UserEntity user;

	@Column(name = "prompt_name", nullable = false)
	private String promptName;

	@Column(name = "post_type")
	@Enumerated(EnumType.STRING)
	private PostType postType;

	@Column(name = "comment")
	private String comment;

	@OneToMany(mappedBy = "prompt")
	private List<PromptAttachEntity> attachments = new ArrayList<>();

	@OneToMany(mappedBy = "prompt")
	private List<PromptOptionEntity> promptOptions = new ArrayList<>();
}
