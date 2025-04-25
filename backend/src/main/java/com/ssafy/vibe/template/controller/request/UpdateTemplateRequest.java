package com.ssafy.vibe.template.controller.request;

public record UpdateTemplateRequest(
	Long templateId,
	String templateName
) {
}
