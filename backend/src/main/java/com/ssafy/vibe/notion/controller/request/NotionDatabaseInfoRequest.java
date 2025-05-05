package com.ssafy.vibe.notion.controller.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class NotionDatabaseInfoRequest {
	private String databaseName;
	private String notionPageId;
}
