package com.ssafy.vibe.template.domain;

import java.util.ArrayList;
import java.util.List;

import com.ssafy.vibe.common.domain.BaseEntity;
import com.ssafy.vibe.prompt.domain.PromptEntity;
import com.ssafy.vibe.snapshot.domain.SnapshotEntity;
import com.ssafy.vibe.user.domain.UserEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
@Table(name = "template")
public class TemplateEntity extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "template_id")
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false)
	private UserEntity user;

	@Column(name = "template_name", nullable = false)
	private String templateName;

	@OneToMany(mappedBy = "template")
	private List<PromptEntity> prompts = new ArrayList<>();

	@OneToMany(mappedBy = "template")
	private List<SnapshotEntity> snapshots = new ArrayList<>();
}
