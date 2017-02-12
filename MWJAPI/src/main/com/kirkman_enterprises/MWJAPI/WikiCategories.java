package com.kirkman_enterprises.MWJAPI;

import java.util.ArrayList;
import java.util.List;

public class WikiCategories {
	
	private String category;
	private String url;

	public WikiCategories(String category) {
		this.category = category;
	}

	public List<String> getCategoryMembers() {
		List<String> returningMembers = new ArrayList<String>();
		
		String query = url+"api.php?action=query&format=xml&list=categorymembers&cmtitle="+category;
		query += "&cmprop=title&cmlimit=max";
		
		return returningMembers;
	}  // end getCategoryMembers()
	
}
