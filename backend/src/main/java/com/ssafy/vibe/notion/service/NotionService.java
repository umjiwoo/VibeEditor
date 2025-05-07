package com.ssafy.vibe.notion.service;

import com.ssafy.vibe.notion.service.command.NotionConnectInfoCommand;
import com.ssafy.vibe.notion.service.command.NotionRegisterDatabaseCommand;

public interface NotionService {

	void saveNotionKey(NotionConnectInfoCommand command);

	void registerNotionDatabase(NotionRegisterDatabaseCommand command);
}
