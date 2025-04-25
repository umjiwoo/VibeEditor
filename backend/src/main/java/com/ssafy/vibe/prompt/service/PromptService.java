package com.ssafy.vibe.prompt.service;

import com.ssafy.vibe.prompt.service.command.PromptCommand;

public interface PromptService {
	String getAnswer(PromptCommand command);
}
