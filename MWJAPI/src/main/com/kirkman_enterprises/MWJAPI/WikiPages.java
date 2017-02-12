package com.kirkman_enterprises.MWJAPI;

import java.io.IOException;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.CookieStore;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class WikiPages {

	private String userAgent = "MediaWikiBot (https://github.com/CBMcArthur/MediaWiki-Java-API)";
	private String localStorage = System.getProperty("java.io.tmpdir");
	private String baseUrl = "https://en.wikipedia.org/w/";
	private String articleTitle;
	private String editToken = null;
	private CookieStore cookieStore;
	
	public WikiPages(String articleTitle) {
		this.articleTitle = articleTitle;
		// createCookie();
	} // end constructor
	
	public void setUserAgent(String userAgent) {
		this.userAgent = userAgent;
	}

	public void setLocalStorage(String localStorage) {
		this.localStorage = localStorage;
	}

	public void setBaseUrl(String baseUrl) {
		this.baseUrl = baseUrl;
	}
	
	public String getArticleTitle() {
		return articleTitle;
	}

	public void setArticleTitle(String articleTitle) {
		this.articleTitle = articleTitle;
	}

	/**
	 * This function returns the Wiki-markup source for the specified article. This function should
	 * be used to simply return the page contents and not for more complex actions such as editing
	 * pages.
	 * 
	 * @return String - The contents of the article, including any wiki markup.
	 */
	public String getPageContent() {
		// Construct the query...
		List<NameValuePair> queryParameters = new ArrayList<NameValuePair>();
		queryParameters.add(new BasicNameValuePair("action", "query"));
		queryParameters.add(new BasicNameValuePair("format", "xml"));
		queryParameters.add(new BasicNameValuePair("prop", "revisions"));
		queryParameters.add(new BasicNameValuePair("rvprop", "content"));
		queryParameters.add(new BasicNameValuePair("titles", articleTitle));
		
		// Request page information and contents as XML
		String pageContents = httpGetRequest(queryParameters);
		
		// Parse the XML and extract the page contents
		pageContents = parseXML(pageContents, "rev", null);
		
		return pageContents;
	}  // end getPageContent()
	
	/**
	 * This function returns an object, EditParameters, that contains information needed to
	 * make changes to an wiki-page.  When editing a wiki-page a call should be made to this
	 * function first to retrieve the EditParameters, make changes to the content attribute
	 * of the EditParameters, then a call made to savePageEdit().
	 * 
	 * @return EditParameters
	 */
	public EditParameters getEditPage() {
		EditParameters editParams = new EditParameters();
		editParams.setArticleTitle(articleTitle);
		
		// Get the edit token if it hasn't been retrieved yet
		if (editToken == null)
			getEditToken();
		editParams.setEditToken(editToken);
		
		// Get the current page information and set appropriate Edit Parameters
		List<NameValuePair> queryParameters = new ArrayList<NameValuePair>();
		queryParameters.add(new BasicNameValuePair("action", "query"));
		queryParameters.add(new BasicNameValuePair("format", "xml"));
		queryParameters.add(new BasicNameValuePair("prop", "info|revisions"));
		queryParameters.add(new BasicNameValuePair("rvprop", "timestamp|content"));
		queryParameters.add(new BasicNameValuePair("titles", articleTitle));
		String pageContents = httpGetRequest(queryParameters);
		editParams.setContent(parseXML(pageContents, "rev", null));
		editParams.setBaseTimeStamp(parseXML(pageContents, "rev", "timestamp"));
		
		// Set start time stamp as current time
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
		Timestamp timestamp = new Timestamp(System.currentTimeMillis());
		editParams.setStartTimeStamp(sdf.format(timestamp));
		
		return editParams;
	}  // end getEditPage()
	
	public boolean savePageEdit(EditParameters editParams) {
		if (editParams == null || editParams.getEditToken() == null) {
			System.err.println("Edit Parameters have not been initialized. Please use getEditPage() first.");
			return false;
		}
		
		List<NameValuePair> queryParameters = new ArrayList<NameValuePair>();
		queryParameters.add(new BasicNameValuePair("action", "edit"));
		queryParameters.add(new BasicNameValuePair("format", "xml"));
		queryParameters.add(new BasicNameValuePair("title", editParams.getArticleTitle()));
		queryParameters.add(new BasicNameValuePair("summary", editParams.getEditSummary()));
		queryParameters.add(new BasicNameValuePair("bot", Integer.toString(editParams.getBotFlag())));
		queryParameters.add(new BasicNameValuePair("basetimestamp", editParams.getBaseTimeStamp()));
		queryParameters.add(new BasicNameValuePair("starttimestamp", editParams.getStartTimeStamp()));
		queryParameters.add(new BasicNameValuePair("text", editParams.getContent()));
		queryParameters.add(new BasicNameValuePair("token", editParams.getEditToken()));
		
		String response = httpPostRequest(queryParameters);
		//System.out.println(response);
		String result = parseXML(response, "edit", "result");
		
		if (result.equals("Success"))
			return true;
		else
			return false;
	}  // end savePageEdit()
	

	// TODO This is not working at all.
	// Also getting a warning "Invalid cookie header: Invalid 'expires' attribute" on all HTTP requests.
	private void createCookie() {
		String domain = baseUrl.substring(baseUrl.lastIndexOf('.', baseUrl.lastIndexOf('.')-1));
		cookieStore = new BasicCookieStore();
		BasicClientCookie cookie = new BasicClientCookie("name", "value");
		cookie.setDomain(domain);
		cookie.setPath("/");
		cookieStore.addCookie(cookie);
		return;
	}
	
	private void getEditToken() {
		if (editToken != null) return;
		
		// Construct the query...
		List<NameValuePair> queryParameters = new ArrayList<NameValuePair>();
		queryParameters.add(new BasicNameValuePair("action", "query"));
		queryParameters.add(new BasicNameValuePair("format", "xml"));
		queryParameters.add(new BasicNameValuePair("meta", "tokens"));
		String tokenResponse = httpGetRequest(queryParameters);
		editToken = parseXML(tokenResponse, "tokens", "csrftoken");
		// System.out.println(editToken);
	}
	
	private String httpGetRequest(List<NameValuePair> queryParameters) {
		String pageContents; 
		
		String requestQuery = "api.php?";
		try {
			requestQuery += httpBuildQuery(queryParameters);
		} catch (UnsupportedEncodingException e1) {
			System.err.println("One or more query parameters could not be encoded");
			return null;
		}
		// System.out.println(requestQuery);

		// Make the request...
		HttpClientBuilder client = HttpClientBuilder.create();
		client.setUserAgent(userAgent);
		client.setDefaultCookieStore(cookieStore);
		CloseableHttpClient cclient = client.build();
		HttpGet getRequest = new HttpGet(baseUrl+requestQuery);
		CloseableHttpResponse response;
		HttpEntity entity;
		try {
			response = cclient.execute(getRequest);
			entity = response.getEntity();
			pageContents = EntityUtils.toString(entity);
			response.close();
		} catch (IOException e) {
			System.err.println("IO Execption occurred....");
			e.printStackTrace();
			return null;
		}
		// System.out.println("Response content for "+articleTitle+" is: \n"+pageContents);
		return pageContents;
	}
	
	private String httpPostRequest(List<NameValuePair> queryParameters) {
		String responseContents;
		
		// Make the POST request...
		HttpClientBuilder client = HttpClientBuilder.create();
		client.setUserAgent(userAgent);
		client.setDefaultCookieStore(cookieStore);
		CloseableHttpClient cclient = client.build();
		HttpPost postRequest = new HttpPost(baseUrl+"api.php");
		postRequest.setHeader("Content_Type", "application/x-www-form-urlencoded");
		try {
			postRequest.setEntity(new UrlEncodedFormEntity(queryParameters));
		} catch (UnsupportedEncodingException e) {
			System.err.println("One or more query parameters could not be encoded");
			e.printStackTrace();
			return null;
		}
		
		CloseableHttpResponse response;
		HttpEntity entity;
		try {
			response = cclient.execute(postRequest);
			entity = response.getEntity();
			responseContents = EntityUtils.toString(entity);
			response.close();
		} catch (IOException e) {
			System.out.println("IO Exception occurred....");
			e.printStackTrace();
			return null;
		}
		// System.out.println(responseContents);
		
		return responseContents;
	}
	
	private String httpBuildQuery(List<NameValuePair> queryParameters) throws UnsupportedEncodingException {
		String encodingType = "UTF-8";
		String queryString = null;
		
		for (NameValuePair param : queryParameters) {
			if (queryString == null)
				queryString = URLEncoder.encode(param.getName(), encodingType)+"="+URLEncoder.encode(param.getValue(), encodingType);
			else
				queryString += "&"+URLEncoder.encode(param.getName(), encodingType)+"="+URLEncoder.encode(param.getValue(), encodingType);
		}
		
		return queryString;
	}
	
	private String parseXML(String xmlContent, String tagName, String attribute) {
		String value;
		// Parse the XML and extract the page contents
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			InputSource is = new InputSource(new StringReader(xmlContent));
			Document doc = builder.parse(is);
			Node rev = doc.getElementsByTagName(tagName).item(0);
			if (attribute == null) {
				value = rev.getTextContent();
			} else {
				value = rev.getAttributes().getNamedItem(attribute).getNodeValue();
			}
		} catch (ParserConfigurationException e) {
			System.err.println("Error configuring the XML parser...");
			e.printStackTrace();
			return null;
		} catch (SAXException e) {
			System.err.println("SAX Exception...");
			e.printStackTrace();
			return null;
		} catch (IOException e) {
			System.err.print("IO Exception...");
			e.printStackTrace();
			return null;
		} catch (NullPointerException e) {
			System.err.println("Unable to find specified tag and/or attribute");
			return null;
		}
		// System.out.println(value);
		return value;
	}
	
}
