package com.ssafy.vibe.post.service.dto;

import java.time.ZoneId;
import java.time.ZonedDateTime;

import com.ssafy.vibe.post.controller.response.RetrieveAiPostResponse;
import com.ssafy.vibe.post.domain.PostEntity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PostRetrieveDTO {
	private Long postId;
	private String postTitle;
	private ZonedDateTime createdAt;
	private ZonedDateTime updatedAt;

	public static PostRetrieveDTO fromEntity(PostEntity entity) {
		return new PostRetrieveDTO(
			entity.getId(),
			entity.getPostTitle(),
			entity.getCreatedAt().atZone(ZoneId.of("UTC")),
			entity.getUpdatedAt().atZone(ZoneId.of("UTC"))
		);
	}

	public RetrieveAiPostResponse toResponse() {
		return new RetrieveAiPostResponse(
			postId,
			postTitle,
			createdAt,
			updatedAt
		);
	}
}
