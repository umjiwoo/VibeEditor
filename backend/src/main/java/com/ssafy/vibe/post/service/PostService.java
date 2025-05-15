package com.ssafy.vibe.post.service;

import java.util.List;

import com.ssafy.vibe.post.service.command.NotionPostCommand;
import com.ssafy.vibe.post.service.command.NotionUpdateCommand;
import com.ssafy.vibe.post.service.dto.NotionPostDTO;
import com.ssafy.vibe.post.service.dto.PostRetrieveDTO;

public interface PostService {
	NotionPostDTO createNotionPost(NotionPostCommand command);

	void updateNotionPost(NotionUpdateCommand command);

	List<PostRetrieveDTO> retrievePostList(Long userId);
}
