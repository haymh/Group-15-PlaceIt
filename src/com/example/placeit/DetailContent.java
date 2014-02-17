package com.example.placeit;

public class DetailContent {
	public String description;
	public String content;
	public int contentFontSize;
	public int contentAlignment;
	
	// Helper class to Place-It Detail Activity
	// Sets format
	
	public DetailContent(String description, String content, int contentFontSize) {
		this.description = description;
		this.content = content;
		this.contentFontSize = contentFontSize;
	}
	
	public DetailContent(String description, String content) {
		this.description = description;
		this.content = content;
	}
	
	public DetailContent(String content, int contentFontSize, int contentAlignment){
		this.content = content;
		this.contentFontSize = contentFontSize;
		this.contentAlignment = contentAlignment;
	}
}
