package com.ssafy.vibe.post.service;

import com.ssafy.vibe.post.service.command.NotionPostCommand;
import com.ssafy.vibe.post.service.command.NotionUpdateCommand;
import com.ssafy.vibe.post.service.dto.NotionPostDTO;

public interface PostService {
	NotionPostDTO createNotionPost(NotionPostCommand command);

	boolean updateNotionPost(NotionUpdateCommand command);
}
