package com.ssafy.vibe.post.domain;

import com.ssafy.vibe.prompt.domain.PromptEntity;
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
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "ai_post")
public class PostEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "post_id")
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "parent_post_id", nullable = false)
	private PostEntity parentPost;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "prompt_id", nullable = false)
	private PromptEntity prompt;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false)
	private UserEntity user;

	@Column(name = "title", nullable = false)
	private String postTitle;

	@Column(name = "post_type")
	@Enumerated(EnumType.STRING)
	private PostType postType;

	@Column(name = "document_id", nullable = false)
	private String documentId;

	@Column(name = "content")
	private String postContent;

	@Column(name = "is_modified")
	private boolean isModified;
}
