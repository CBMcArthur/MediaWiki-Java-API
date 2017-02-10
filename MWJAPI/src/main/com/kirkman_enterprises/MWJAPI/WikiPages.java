package com.kirkman_enterprises.MWJAPI;

import java.io.IOException;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.http.HttpEntity;
import org.apache.http.client.CookieStore;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.apache.http.util.EntityUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.kirkman_enterprises.MWJAPI.objects.EditParameters;

public class WikiPages {

	private String userAgent = "MediaWikiBot (https://github.com/CBMcArthur/MediaWiki-Java-API)";
	private String localStorage = System.getProperty("java.io.tmpdir");
	private String baseUrl = "http://en.wikipedia.org/w/";
	private String articleTitle;
	private String editToken = null;
	private CookieStore cookieStore;
	
	public WikiPages(String articleTitle) {
		this.articleTitle = articleTitle;
		createCookie();
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

	/**
	 * This function returns the Wiki-markup source for the specified article. This function should
	 * be used to simply return the page contents and not for more complex actions such as editing
	 * pages.
	 * 
	 * @return String - The contents of the article, including any wiki markup.
	 */
	public String getPageContent() {
		// Construct the query...
		Map<String, String> queryParameters = new HashMap<String, String>();
		queryParameters.put("action", "query");
		queryParameters.put("format", "xml");
		queryParameters.put("prop", "revisions");
		queryParameters.put("rvprop", "content");
		queryParameters.put("titles", articleTitle);
		
		// Request page information and contents as XML
		String pageContents = httpRequest(queryParameters);
		
		// Parse the XML and extract the page contents
		pageContents = parseXML(pageContents, "rev", null);
		
		return pageContents;
	}  // end getPageContent()
	
	public EditParameters getEditPage() {
		EditParameters editParams = new EditParameters();
		editParams.setArticleTitle(articleTitle);
		
		// Get the edit token if it hasn't been retrieved yet
		if (editToken == null)
			getEditToken();
		editParams.setEditToken(editToken);
		
		// Get the current page information and set appropriate Edit Parameters
		Map<String, String> queryParameters = new HashMap<String, String>();
		queryParameters.put("action", "query");
		queryParameters.put("format", "xml");
		queryParameters.put("prop", "info|revisions");
		queryParameters.put("rvprop", "timestamp|content");
		queryParameters.put("titles", articleTitle);
		String pageContents = httpRequest(queryParameters);
		editParams.setContent(parseXML(pageContents, "rev", null));
		editParams.setBaseTimeStamp(parseXML(pageContents, "rev", "timestamp"));
		
		// Set start time stamp as current time
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
		Timestamp timestamp = new Timestamp(System.currentTimeMillis());
		editParams.setStartTimeStamp(sdf.format(timestamp));
		
		return editParams;
	}  // end getEditPage()
	
	private void getEditToken() {
		if (editToken != null) return;
		
		// Construct the query...
		Map<String, String> queryParameters = new HashMap<String, String>();
		queryParameters.put("action", "query");
		queryParameters.put("format", "xml");
		queryParameters.put("meta", "tokens");
		String tokenResponse = httpRequest(queryParameters);
		editToken = parseXML(tokenResponse, "tokens", "csrftoken");
		// System.out.println(editToken);
	}
	
	private String httpRequest(Map<String, String> queryParameters) {
		String pageContents; 
		
		String requestQuery = "api.php?";
		try {
			requestQuery += httpBuildQuery(queryParameters);
		} catch (UnsupportedEncodingException e1) {
			System.err.println("One or more query parameters could not be encoded; most likely the Article title.");
			System.exit(-1);
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
		// System.out.println("Reponse content for "+articleTitle+" is: \n"+pageContents);
		return pageContents;
	}
	
	private String httpBuildQuery(Map<String, String> parameters) throws UnsupportedEncodingException {
		String encodingType = "UTF-8";
		String queryString = null;
		
		for (Map.Entry<String, String> entry : parameters.entrySet()) {
			if (queryString == null)
				queryString = URLEncoder.encode(entry.getKey(), encodingType)+"="+URLEncoder.encode(entry.getValue(), encodingType);
			else
				queryString += "&"+URLEncoder.encode(entry.getKey(), encodingType)+"="+URLEncoder.encode(entry.getValue(), encodingType);
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
		}
		// System.out.println(value);
		return value;
	}
	
//	public String savePageEdit(EditParameters editParams) throws HTTPException {
//		// Construct the query
//		String newURL = url+"api.php";
//
//		List<NameValuePair> postParams = new ArrayList<NameValuePair>();
//		postParams.add(new BasicNameValuePair("action", "edit"));
//		postParams.add(new BasicNameValuePair("format", "xml"));
//		postParams.add(new BasicNameValuePair("title", editParams.getArticleTitle()));
//		postParams.add(new BasicNameValuePair("summary", editParams.getEditSummary()));
//		postParams.add(new BasicNameValuePair("bot", "true"));
//		postParams.add(new BasicNameValuePair("basetimestamp", editParams.getBaseTimeStamp()));
//		postParams.add(new BasicNameValuePair("starttimestamp", editParams.getStartTimeStamp()));
//		postParams.add(new BasicNameValuePair("text", editParams.getContent()));
//		postParams.add(new BasicNameValuePair("token", editParams.getEditToken()));
//		String response = HttpRequest.sendPostQuery(newURL, postParams, true, client);
//		
//		// Lets check the response to make sure the edit is successful
//		try {
//			// Setup XML helpers
//			DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
//			DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
//			InputStream in = new ByteArrayInputStream(response.getBytes());
//			Reader reader = new InputStreamReader(in, "UTF-8");
//			InputSource is = new InputSource(reader);
//			is.setEncoding("UTF-8");
//			Document doc = docBuilder.parse(is);
//
//			// Parse out the XML
//			NodeList nodeList = doc.getElementsByTagName("edit");
//			if (nodeList == null || nodeList.getLength() == 0) {
//				System.err.println("The edit response lacked an edit tag.  Printing the response: ");
//				System.err.println(response);
//				return "Edit error";
//			}
//			Node node = nodeList.item(0);
//			NamedNodeMap nodeProps = node.getAttributes();
//			Node propNode = nodeProps.getNamedItem("result");
//			if (propNode.getNodeValue().equalsIgnoreCase("Success"))
//				return "Success";
//			else {
//				// get the error code
//				nodeList = doc.getElementsByTagName("error");
//				node = nodeList.item(0);
//				nodeProps = node.getAttributes();
//				propNode = nodeProps.getNamedItem("code");
//				return propNode.getNodeValue();
//			}
//		} catch (ParserConfigurationException e) {
//			System.err.println("An error occurred parsing the server response.");
//			e.printStackTrace();
//		} catch (SAXException e) {
//			System.err.println("An error occurred parsing the server response.");
//			e.printStackTrace();
//		} catch (IOException e) {
//			System.err.println("An error occurred parsing the server response.");
//			e.printStackTrace();
//		}
//
//		return "Unknown Error";
//	}  // end savePageEdit()
	
//	public boolean isRedirected() {
//		String query = url+"api.php?action=query&format=xml";
//		try {
//			query += "&redirects&titles="+URLEncoder.encode(articleTitle.replace(' ' , '_'), "UTF-8");
//		} catch (UnsupportedEncodingException e1) {
//			return false;
//		}
//		
//		String response;
//		try {
//			response = HttpRequest.sendGetQuery(query, client);
//		} catch (HTTPException e) {
//			return false;
//		}
//
//		try {
//			// Setup XML helpers
//			DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
//			DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
//			InputStream in = new ByteArrayInputStream(response.getBytes());
//			Reader reader = new InputStreamReader(in, "UTF-8");
//			InputSource is = new InputSource(reader);
//			is.setEncoding("UTF-8");
//			Document doc = docBuilder.parse(is);
//
//			// Parse out the XML
//			NodeList redirectList = doc.getElementsByTagName("r");
//			Node redirect = redirectList.item(0);
//			if (redirect == null) {
//				return false;
//			} else {
//				return true;
//			}
//		} catch (ParserConfigurationException e) {
//			System.err.println("An error occurred parsing the server response.");
//			e.printStackTrace();
//		} catch (SAXException e) {
//			System.err.println("An error occurred parsing the server response.");
//			e.printStackTrace();
//		} catch (IOException e) {
//			System.err.println("An error occurred parsing the server response.");
//			e.printStackTrace();
//		}
//		return false;
//	}  // end isRedirected()
	
//	public String getRedirectName() {
//		String redirectName = "";
//		String query = url+"api.php?action=query&format=xml";
//		try {
//			query += "&redirects&titles="+URLEncoder.encode(articleTitle.replace(' ' , '_'), "UTF-8");
//		} catch (UnsupportedEncodingException e1) {
//			return redirectName;
//		}
//		
//		String response;
//		try {
//			response = HttpRequest.sendGetQuery(query, client);
//		} catch (HTTPException e) {
//			return redirectName;
//		}
//
//		try {
//			// Setup XML helpers
//			DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
//			DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
//			InputStream in = new ByteArrayInputStream(response.getBytes());
//			Reader reader = new InputStreamReader(in, "UTF-8");
//			InputSource is = new InputSource(reader);
//			is.setEncoding("UTF-8");
//			Document doc = docBuilder.parse(is);
//
//			// Parse out the XML
//			NodeList redirectList = doc.getElementsByTagName("r");
//			Node redirect = redirectList.item(0);
//			if (redirect == null) {
//				return redirectName;
//			} else {
//				NamedNodeMap attributes = redirect.getAttributes();
//				Node toAttrib = attributes.getNamedItem("to");
//				redirectName = toAttrib.getNodeValue();
//				return redirectName;
//			}
//		} catch (ParserConfigurationException e) {
//			System.err.println("An error occurred parsing the server response.");
//			e.printStackTrace();
//		} catch (SAXException e) {
//			System.err.println("An error occurred parsing the server response.");
//			e.printStackTrace();
//		} catch (IOException e) {
//			System.err.println("An error occurred parsing the server response.");
//			e.printStackTrace();
//		}
//		return redirectName;
//	}
}
