package com.kirkman_enterprises.MWJAPI.objects;

public class EditParameters {

	private String articleTitle;
	private String content;
	private String editToken;
	private String baseTimeStamp;
	private String startTimeStamp;
	private String editSummary;
	private int bot = 1;
	
	public String getArticleTitle() {
		return articleTitle;
	}
	
	public void setArticleTitle(String articleTitle) {
		this.articleTitle = articleTitle;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public String getEditToken() {
		return editToken;
	}

	public void setEditToken(String editToken) {
		this.editToken = editToken;
	}

	public String getBaseTimeStamp() {
		return baseTimeStamp;
	}

	public void setBaseTimeStamp(String baseTimeStamp) {
		this.baseTimeStamp = baseTimeStamp;
	}

	public String getStartTimeStamp() {
		return startTimeStamp;
	}

	public void setStartTimeStamp(String startTimeStamp) {
		this.startTimeStamp = startTimeStamp;
	}

	public String getEditSummary() {
		return editSummary;
	}

	public void setEditSummary(String editSummary) {
		this.editSummary = editSummary;
	}
	
	public String toString() {
		String retString = "Edit Parameters:\n";
		retString += "Article Title: "+articleTitle+"\n";
		retString += "Edit Token: "+editToken+"\n";
		retString += "Bot Flag: "+bot+"\n";
		retString += "Base Timestamp: "+baseTimeStamp+"\n";
		retString += "Start Timestamp: "+startTimeStamp+"\n";
		retString += "Edit Summary: "+editSummary+"\n";
		retString += "Article Content: "+content+"\n";
		return retString;
	}
}
