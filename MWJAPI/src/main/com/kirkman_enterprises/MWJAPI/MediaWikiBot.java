package com.kirkman_enterprises.MWJAPI;

import com.kirkman_enterprises.MWJAPI.objects.EditParameters;

public class MediaWikiBot {
	
	private String baseUrl;
	private String userAgent;
	private String localStorage;
	
	/**
	 * Constructor for MediaWikiBot API.  This should be instantiated to use the MediaWiki Java API.
	 */
	public MediaWikiBot() {
	}
	
	/**
	 * Constructor for MediaWikiBot API.  This should be instantiated to use the MediaWiki Java API.
	 * 
	 * @param url - This is the base URL for the MediaWiki site the bot/program will be interacting with.
	 * 		For the English Wikipedia site this will be http://en.wikipedia.org/w/
	 * @param userAgent - This is the user agent for the bot/program.  Some sites using MediaWiki require
	 * 		that bots use a self-identifying user agent.
	 * @throws MalformedURLException
	 * @deprecated Use constructor with no parameter for better modularity
	 */
	@Deprecated
	public MediaWikiBot(String url, String userAgent) {
		this.baseUrl = url;
		this.userAgent = userAgent;
	}
	
	/**
	 * Constructor for MediaWikiBot API.  This should be instantiated to use the MediaWiki Java API.
	 * 
	 * @param url - This is the base URL for the MediaWiki site the bot/program will be interacting with.
	 * 		For the English Wikipedia site this will be http://en.wikipedia.org/w/
	 * @param userAgent - This is the user agent for the bot/program.  Some sites using MediaWiki require
	 * 		that bots use a self-identifying user agent.
	 * @param localStorage - This should be a String version of the directory path to where the API
	 * 		can edit and store files.  Currently this is only used for the cookie file.
	 * @throws MalformedURLException
	 * @deprecated Use constructor with no parameter for better modularity
	 */
	@Deprecated
	public MediaWikiBot(String url, String userAgent, String localStorage) {
		this.baseUrl = url;
		this.userAgent = userAgent;
		this.localStorage = localStorage;
	}
	
	public void setBaseUrl(String baseUrl) {
		this.baseUrl = baseUrl;
	}

	public void setUserAgent(String userAgent) {
		this.userAgent = userAgent;
	}

	public void setLocalStorage(String localStorage) {
		this.localStorage = localStorage;
	}

	/**
	 * This method will retrieve the contents of specified article. This function should
	 * be used to simply return the page contents and not for more complex actions such as editing
	 * pages.
	 * 
	 * @param articleTitle - The title of the article to retrieve the contents of.
	 * @return String - The contents of the article, including any wiki markup.
	 * @throws HTTPException
	 */
	public String getPageContent(String articleTitle) {
		WikiPages pageContent = new WikiPages(articleTitle);
		if (baseUrl != null)
			pageContent.setBaseUrl(baseUrl);
		if (localStorage != null)
			pageContent.setLocalStorage(localStorage);
		if (userAgent != null)
			pageContent.setUserAgent(userAgent);
		return pageContent.getPageContent();
	}
	
	/**
	 * This method logs the bot into the specified MediaWiki site. 
	 * 
	 * @param username - The username of the bot or editor.
	 * @param password - The password associated with the username.
	 * @return boolean - Whether or not the login was successful.
	 * @throws HTTPException
	 */
	// TODO MediaWiki prefers OAuth/Owner-only authentication.  Need to research.
//	public boolean login(String username, String password) throws HTTPException {
//		Login login = new Login(username, password, url, client, localStorage);
//		return login.login();
//	}  // end login()
	
	/**
	 * This method should be used if the bot/program will be editing a particular article.  It returns
	 * an object containing the article's page contents and other variables needed for editing an article.
	 * This object includes a 'content' attribute containing the wiki-page's contents which you are 
	 * attempting to edit.
	 * 
	 * @param articleTitle - The title of the article that is to be edited.
	 * @return EditParameters - An object containing information about the article to be edited.
	 */
	public EditParameters getEditPage(String articleTitle) {
		WikiPages editPage = new WikiPages(articleTitle);
		return editPage.getEditPage();
	}
	
	/**
	 * This method should be called to save an edited article.  
	 * 
	 * @param articleTitle - The title of the article to save changes to.
	 * @param editParams - The EditParameters object containing information about the article
	 * 		and the edits to be performed.
	 * @return String - The result of attempting to save the edits to the article.  More explanation
	 * 		of the return value can be provided later.
	 * @throws HTTPException
	 */
//	public String savePageEdit(String articleTitle, EditParameters editParams) throws HTTPException {
//		PageContents pageContent = new PageContents(articleTitle, url, client);
//		return pageContent.savePageEdit(editParams);
//	}
	
	/**
	 * This method is used to get the articles contained with in a particular category.
	 * 
	 * @param category - The category to get articles within.
	 * @return List<String> - An ArrayList of Strings containing the articles in the category.
	 * @throws HTTPException
	 */
//	public List<String> getCategoryMembers(String category) throws HTTPException {
//		WikiCategories wikiCategory = new WikiCategories(category, url, client);
//		return wikiCategory.getCategoryMembers();
//	}
	
	/**
	 * This is a Convenience method used to determine if a particular article is redirected
	 * to another article.  It utilizes MediaWiki's API for redirection detection.
	 * 
	 * @param articleTitle - The name of the article to determine if it is redirected.
	 * @return Boolean - Whether or not the article is redirected to another article.
	 */
//	public boolean isRedirected(String articleTitle) {
//		PageContents pageContent = new PageContents(articleTitle, url, client);
//		return pageContent.isRedirected();
//	}
	
	/**
	 * This is a convenience method to obtain the name of the article that a specified article
	 * is redirected to.
	 * 
	 * @param articleTitle - The name of the original article.
	 * @return String - The name of article that the original article is redirected to.
	 */
//	public String getRedirectName(String articleTitle) {
//		PageContents pageContent = new PageContents(articleTitle, url, client);
//		return pageContent.getRedirectName();
//	}


//	private void readCookiesFromFile() {
//		try {
//			CookieStore cookieStore = new BasicCookieStore();
//			BufferedReader in = new BufferedReader(new FileReader(localStorage+"/cookie.txt"));
//			String info;
//			String[] params;
//			BasicClientCookie cookie;
//			while ( (info = in.readLine()) != null) {
//				params = info.split(",");
//				cookie = new BasicClientCookie(params[3], params[5]);
//				cookie.setComment(params[0]);
//				cookie.setDomain(params[2]);
//				cookie.setVersion(Integer.parseInt(params[6]));
//				DateFormat dateFormat = new SimpleDateFormat("E MMM dd HH:mm:ss z yyyy");
//				cookie.setExpiryDate(dateFormat.parse(params[7]));
//				cookieStore.addCookie(cookie);
//			}
//			client.setCookieStore(cookieStore);
//			in.close();
//		} catch (FileNotFoundException e) {
//			System.err.println("The cookie file was not found.");
//		} catch (IOException e) {
//			System.err.println("Error reading from cookie file.");
//			e.printStackTrace();
//		} catch (ParseException e) {
//			System.err.println("Error parsing the expiration date.");
//			e.printStackTrace();
//		}
//	}  //end readCookiesFromFile()
	
}
