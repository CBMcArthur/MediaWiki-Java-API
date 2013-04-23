package com.kirkman_enterprises.MWJAPI;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.zip.GZIPInputStream;

import org.apache.http.Header;
import org.apache.http.HeaderElement;
import org.apache.http.HttpEntity;
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.HttpResponse;
import org.apache.http.HttpResponseInterceptor;
import org.apache.http.client.CookieStore;
import org.apache.http.entity.HttpEntityWrapper;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.protocol.HttpContext;

import com.kirkman_enterprises.MWJAPI.objects.EditParameters;

public class MediaWikiBot {

	private String url;
	private String localStorage;
	private DefaultHttpClient client;
	
	/**
	 * Constructor for MediaWikiBot API.  This should be instantiated to use the MediaWiki Java API.
	 * 
	 * @param url - This is the base URL for the MediaWiki site the bot/program will be interacting with.
	 * 		For the English Wikipedia site this will be http://en.wikipedia.org/w/
	 * @param userAgent - This is the user agent for the bot/program.  Some sites using MediaWiki require
	 * 		that bots use a self-identifying user agent.
	 * @throws MalformedURLException
	 */
	public MediaWikiBot(String url, String userAgent) throws MalformedURLException {
		this.url = url;
		localStorage = null;
		client = new DefaultHttpClient();
		// TODO Change user agent to be function parameter.
		client.getParams().setParameter(CoreProtocolPNames.USER_AGENT, userAgent);
		client.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT, 50000);
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
	 */
	public MediaWikiBot(String url, String userAgent, String localStorage) throws MalformedURLException {
		this.url = url;
		this.localStorage = localStorage;
		@SuppressWarnings("unused")
		URL testValid = new URL(url);
		client = new DefaultHttpClient();
		// TODO Change user agent to be function parameter.
		client.getParams().setParameter(CoreProtocolPNames.USER_AGENT, userAgent);
		client.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT, 50000);
		readCookiesFromFile();
		//setupInterceptors();
	}  // end constructor
	
	/**
	 * This method logs the bot/program into the specified MediaWiki site. 
	 * 
	 * @param username - The username of the bot or editor.
	 * @param password - The password associated with the username.
	 * @return boolean - Whether or not the login was successful.
	 * @throws HTTPException
	 */
	public boolean login(String username, String password) throws HTTPException {
		Login login = new Login(username, password, url, client, localStorage);
		return login.login();
	}  // end login()
	
	/**
	 * This method will retrieve the contents of specified article.
	 * 
	 * @param articleTitle - The title of the article to retrieve the contents of.
	 * @return String - The contents of the article, including any wiki markup.
	 * @throws HTTPException
	 */
	public String getPageContent(String articleTitle) throws HTTPException {
		PageContents pageContent = new PageContents(articleTitle, url, client);
		return pageContent.getPageContent();
	}
	
	/**
	 * This method should be used if the bot/program will be editing a particular article.  It returns
	 * an object containing the article's page contents and other variables needed for editing an article.
	 * 
	 * @param articleTitle - The title of the article that is to be edited.
	 * @return EditParameters - An object containing information about the article to be edited.
	 * @throws HTTPException
	 */
	public EditParameters getEditPage(String articleTitle) throws HTTPException {
		PageContents pageContent = new PageContents(articleTitle, url, client);
		return pageContent.getEditPage();
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
	public String savePageEdit(String articleTitle, EditParameters editParams) throws HTTPException {
		PageContents pageContent = new PageContents(articleTitle, url, client);
		return pageContent.savePageEdit(editParams);
	}
	
	/**
	 * This method is used to get the articles contained with in a particular category.
	 * 
	 * @param category - The category to get articles within.
	 * @return List<String> - An ArrayList of Strings containing the articles in the category.
	 * @throws HTTPException
	 */
	public List<String> getCategoryMembers(String category) throws HTTPException {
		WikiCategories wikiCategory = new WikiCategories(category, url, client);
		return wikiCategory.getCategoryMembers();
	}
	
	/**
	 * This is a Convenience method used to determine if a particular article is redirected
	 * to another article.  It utilizes MediaWiki's API for redirection detection.
	 * 
	 * @param articleTitle - The name of the article to determine if it is redirected.
	 * @return Boolean - Whether or not the article is redirected to another article.
	 */
	public boolean isRedirected(String articleTitle) {
		PageContents pageContent = new PageContents(articleTitle, url, client);
		return pageContent.isRedirected();
	}
	
	/**
	 * This is a convenience method to obtain the name of the article that a specified article
	 * is redirected to.
	 * 
	 * @param articleTitle - The name of the original article.
	 * @return String - The name of article that the original article is redirected to.
	 */
	public String getRedirectName(String articleTitle) {
		PageContents pageContent = new PageContents(articleTitle, url, client);
		return pageContent.getRedirectName();
	}

	private void readCookiesFromFile() {
		try {
			CookieStore cookieStore = new BasicCookieStore();
			BufferedReader in = new BufferedReader(new FileReader(localStorage+"/cookie.txt"));
			String info;
			String[] params;
			BasicClientCookie cookie;
			while ( (info = in.readLine()) != null) {
				params = info.split(",");
				cookie = new BasicClientCookie(params[3], params[5]);
				cookie.setComment(params[0]);
				cookie.setDomain(params[2]);
				cookie.setVersion(Integer.parseInt(params[6]));
				DateFormat dateFormat = new SimpleDateFormat("E MMM dd HH:mm:ss z yyyy");
				cookie.setExpiryDate(dateFormat.parse(params[7]));
				cookieStore.addCookie(cookie);
			}
			client.setCookieStore(cookieStore);
			in.close();
		} catch (FileNotFoundException e) {
			System.err.println("The cookie file was not found.");
		} catch (IOException e) {
			System.err.println("Error reading from cookie file.");
			e.printStackTrace();
		} catch (ParseException e) {
			System.err.println("Error parsing the expiration date.");
			e.printStackTrace();
		}
	}  //end readCookiesFromFile()

	@SuppressWarnings("unused")
	private void setupInterceptors() {
		client.addRequestInterceptor(new HttpRequestInterceptor() {

			@Override
			public void process(HttpRequest request, HttpContext context)
					throws HttpException, IOException {
				if (!request.containsHeader("Accept-Encoding")) {
					request.addHeader("Accept-Encoding", "gzip");
				}
			}
			
		});  // end addRequestInterceptor
		
		client.addResponseInterceptor(new HttpResponseInterceptor() {

			@Override
			public void process(HttpResponse response, HttpContext context)
					throws HttpException, IOException {
				HttpEntity entity = response.getEntity();
				Header ceheader = entity.getContentEncoding();
				if (ceheader != null) {
					HeaderElement[] codecs = ceheader.getElements();
					for (int i=0; i < codecs.length; i++) {
						if (codecs[i].getName().equalsIgnoreCase("gzip")) {
							response.setEntity(new GzipDecompressingEntity(response.getEntity()));
							return;
						}
					}
				}
			}
			
		}); // end addResposneInterceptor
	}  // end setupInterceptors()
	
	private class GzipDecompressingEntity extends HttpEntityWrapper {

		public GzipDecompressingEntity(final HttpEntity entity) {
			super(entity);
		}
		
		@Override
		public InputStream getContent() throws IOException, IllegalStateException {
			// the wrapped entity's getContent() decides about repeatability
			InputStream wrappedin = wrappedEntity.getContent();
			return new GZIPInputStream(wrappedin);
		}
		
		@Override
		public long getContentLength() {
			// Length of ungzipped content is not known
			return -1;
		}
		
	}  // end inner class GzipDecompressingEntity
	
}
