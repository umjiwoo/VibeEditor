package com.ssafy.vibe.notion.controller.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class NotionDatabaseResponse {
	private String databaseId;
	private String databaseName;
	private String databaseUid;
}
