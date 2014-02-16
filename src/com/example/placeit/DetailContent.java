package com.example.placeit;

public class DetailContent {
	public String description;
	public String content;
	public int contentFontSize;
	
	public DetailContent(String description, String content, int contentFontSize) {
		this.description = description;
		this.content = content;
		this.contentFontSize = contentFontSize;
	}
	
	public DetailContent(String description, String content) {
		this.description = description;
		this.content = content;
	}
}
