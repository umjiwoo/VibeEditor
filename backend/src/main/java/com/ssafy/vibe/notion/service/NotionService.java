package com.ssafy.vibe.notion.service;

import com.ssafy.vibe.notion.service.command.NotionConnectInfoCommand;

public interface NotionService {

	void saveNotionKey(NotionConnectInfoCommand command);

}
