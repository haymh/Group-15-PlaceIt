package com.fifteen.placeit;

import android.view.Gravity;

//Class that holds PlaceItDetailActivity display content
public class DetailContent {
	public CharSequence description;
	public CharSequence content;
	public int contentFontSize;
	public int contentAlignment;
	public int descriptionFontSize;
	public int descriptionAlignment;
	
	// Helper class to Place-It Detail Activity
	// Sets format
	
	public DetailContent(CharSequence description, CharSequence content, int contentFontSize) {
		setDefault();
		this.description = description;
		this.content = content;
		this.contentFontSize = contentFontSize;
	}
	
	public DetailContent(CharSequence description, CharSequence content) {
		setDefault();
		this.description = description;
		this.content = content;
	}
	
	public DetailContent(CharSequence content, int contentFontSize, int contentAlignment){
		setDefault();
		this.content = content;
		this.contentFontSize = contentFontSize;
		this.contentAlignment = contentAlignment;
	}
	
	public DetailContent() {
		setDefault();
	}
	
	private void setDefault() {
		descriptionFontSize = DetailContentFormatter.REGULAR_FONT;
		descriptionAlignment = Gravity.LEFT;
		contentFontSize = DetailContentFormatter.REGULAR_FONT;
		contentAlignment = Gravity.LEFT;
	}
}
