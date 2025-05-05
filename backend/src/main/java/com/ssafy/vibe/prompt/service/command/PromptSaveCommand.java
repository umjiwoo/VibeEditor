package com.ssafy.vibe.prompt.service.command;

import java.util.List;

import com.ssafy.vibe.notion.domain.NotionDatabaseEntity;
import com.ssafy.vibe.prompt.domain.PromptEntity;
import com.ssafy.vibe.prompt.service.dto.PromptDTO;
import com.ssafy.vibe.template.domain.TemplateEntity;
import com.ssafy.vibe.user.domain.UserEntity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Builder
@AllArgsConstructor
@Data
public class PromptSaveCommand {
	private Long parentPromptId;
	private Long templateId;
	private String promptName;
	private String postType;
	private String comment;
	private List<SnapshotCommand> promptAttachList;
	private Long[] promptOptionList;
	private Long notionDatabaseId;

	public PromptDTO toPromptDTO(
		PromptEntity parentPrompt,
		TemplateEntity template,
		UserEntity user,
		NotionDatabaseEntity notionDatabase
	) {
		return PromptDTO.builder()
			.parentPrompt(parentPrompt)
			.template(template)
			.user(user)
			.promptName(this.promptName)
			.postType(this.postType)
			.comment(this.comment)
			.notionDatabase(notionDatabase)
			.build();
	}
}
