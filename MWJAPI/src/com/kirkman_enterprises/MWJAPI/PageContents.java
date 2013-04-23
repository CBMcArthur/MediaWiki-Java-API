package com.kirkman_enterprises.MWJAPI;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.http.NameValuePair;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.kirkman_enterprises.MWJAPI.objects.EditParameters;

public class PageContents {

	private String articleTitle;
	private String url;
	private DefaultHttpClient client;
	
	public PageContents(String articleTitle, String url, DefaultHttpClient client) {
		this.articleTitle = articleTitle;
		this.url = url;
		this.client = client;
	} // end constructor
	
	public String getPageContent() throws HTTPException {
		String content = "";
		
		// Construct the query
		String query = url+"api.php?action=query&format=xml";
		try {
			query += "&prop=revisions&titles="+URLEncoder.encode(articleTitle.replace(' ' , '_'), "UTF-8")+"&rvprop=content";
		} catch (UnsupportedEncodingException e1) {
			throw new HTTPException("Article title cound not be encoded for the URL.");
		}

		String contentResponse = HttpQueries.sendGetQuery(query, client);
		//System.out.println("contentReponse for "+articleTitle+" is: \n"+contentResponse);
		
		try {
			// Setup XML helpers
			DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
			InputStream in = new ByteArrayInputStream(contentResponse.getBytes());
			Reader reader = new InputStreamReader(in, "UTF-8");
			InputSource is = new InputSource(reader);
			is.setEncoding("UTF-8");
			Document doc = docBuilder.parse(is);

			// Parse out the XML
			NodeList revList = doc.getElementsByTagName("rev");
			Node rev = revList.item(0);
			if (rev == null)
				content = "";
			else 
				content = rev.getTextContent();
			
		} catch (ParserConfigurationException e) {
			System.err.println("An error occurred parsing the server response.");
			e.printStackTrace();
		} catch (SAXException e) {
			System.err.println("An error occurred parsing the server response.");
			e.printStackTrace();
		} catch (IOException e) {
			System.err.println("An error occurred parsing the server response.");
			e.printStackTrace();
		}
		
		return content;
	}  // end getPageContent()
	
	public EditParameters getEditPage() throws HTTPException {
		EditParameters editParams = new EditParameters();
		
		String query = url+"api.php?action=query&format=xml";
		try {
			query += "&prop=info%7Crevisions&intoken=edit&titles="+URLEncoder.encode(articleTitle.replace(' ' , '_'), "UTF-8");
		} catch (UnsupportedEncodingException e1) {
			throw new HTTPException("The article title could not be encoded into a URL.");
		}
		query += "&rvprop=content%7Ctimestamp";
		
		String response = HttpQueries.sendGetQuery(query, client);

		// DEBUG!!
		//System.out.println("Response for "+articleTitle+"\n"+response);
		
		try {
			// Setup XML helpers
			DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
			InputStream in = new ByteArrayInputStream(response.getBytes("UTF-8"));
			Reader reader = new InputStreamReader(in, "UTF-8"); 
			InputSource is = new InputSource(reader);
			is.setEncoding("UTF-8");
			Document doc = docBuilder.parse(is);

			NamedNodeMap properties;
			Node propNode;
			
			// Parse out the XML
			NodeList pageList = doc.getElementsByTagName("page");
			Node page = pageList.item(0);
			properties = page.getAttributes();
			propNode = properties.getNamedItem("title");
			editParams.setArticleTitle(propNode.getNodeValue());
			propNode = properties.getNamedItem("starttimestamp");
			editParams.setStartTimeStamp(propNode.getNodeValue());
			propNode = properties.getNamedItem("edittoken");
			editParams.setEditToken(propNode.getNodeValue());
			
			propNode = properties.getNamedItem("missing");
			if (propNode == null) {
				NodeList revList = doc.getElementsByTagName("rev");
				Node rev = revList.item(0);
				editParams.setContent(rev.getTextContent());
				properties = rev.getAttributes();
				propNode = properties.getNamedItem("timestamp");
				editParams.setBaseTimeStamp(propNode.getNodeValue());
			} else {
				editParams.setContent("");
			}
		} catch (ParserConfigurationException e) {
			System.err.println("An error occurred parsing the server response.");
			e.printStackTrace();
		} catch (SAXException e) {
			System.err.println("An error occurred parsing the server response.");
			e.printStackTrace();
		} catch (IOException e) {
			System.err.println("An error occurred parsing the server response.");
			e.printStackTrace();
		}
		return editParams;
	}  // end getEditPage()
	
	public String savePageEdit(EditParameters editParams) throws HTTPException {
		// Construct the query
		String newURL = url+"api.php";

		List<NameValuePair> postParams = new ArrayList<NameValuePair>();
		postParams.add(new BasicNameValuePair("action", "edit"));
		postParams.add(new BasicNameValuePair("format", "xml"));
		postParams.add(new BasicNameValuePair("title", editParams.getArticleTitle()));
		postParams.add(new BasicNameValuePair("summary", editParams.getEditSummary()));
		postParams.add(new BasicNameValuePair("bot", "true"));
		postParams.add(new BasicNameValuePair("basetimestamp", editParams.getBaseTimeStamp()));
		postParams.add(new BasicNameValuePair("starttimestamp", editParams.getStartTimeStamp()));
		postParams.add(new BasicNameValuePair("text", editParams.getContent()));
		postParams.add(new BasicNameValuePair("token", editParams.getEditToken()));
		String response = HttpQueries.sendPostQuery(newURL, postParams, true, client);
		
		// Lets check the response to make sure the edit is successful
		try {
			// Setup XML helpers
			DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
			InputStream in = new ByteArrayInputStream(response.getBytes());
			Reader reader = new InputStreamReader(in, "UTF-8");
			InputSource is = new InputSource(reader);
			is.setEncoding("UTF-8");
			Document doc = docBuilder.parse(is);

			// Parse out the XML
			NodeList nodeList = doc.getElementsByTagName("edit");
			if (nodeList == null || nodeList.getLength() == 0) {
				System.err.println("The edit response lacked an edit tag.  Printing the response: ");
				System.err.println(response);
				return "Edit error";
			}
			Node node = nodeList.item(0);
			NamedNodeMap nodeProps = node.getAttributes();
			Node propNode = nodeProps.getNamedItem("result");
			if (propNode.getNodeValue().equalsIgnoreCase("Success"))
				return "Success";
			else {
				// get the error code
				nodeList = doc.getElementsByTagName("error");
				node = nodeList.item(0);
				nodeProps = node.getAttributes();
				propNode = nodeProps.getNamedItem("code");
				return propNode.getNodeValue();
			}
		} catch (ParserConfigurationException e) {
			System.err.println("An error occurred parsing the server response.");
			e.printStackTrace();
		} catch (SAXException e) {
			System.err.println("An error occurred parsing the server response.");
			e.printStackTrace();
		} catch (IOException e) {
			System.err.println("An error occurred parsing the server response.");
			e.printStackTrace();
		}

		return "Unknown Error";
	}  // end savePageEdit()
	
	public boolean isRedirected() {
		String query = url+"api.php?action=query&format=xml";
		try {
			query += "&redirects&titles="+URLEncoder.encode(articleTitle.replace(' ' , '_'), "UTF-8");
		} catch (UnsupportedEncodingException e1) {
			return false;
		}
		
		String response;
		try {
			response = HttpQueries.sendGetQuery(query, client);
		} catch (HTTPException e) {
			return false;
		}

		try {
			// Setup XML helpers
			DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
			InputStream in = new ByteArrayInputStream(response.getBytes());
			Reader reader = new InputStreamReader(in, "UTF-8");
			InputSource is = new InputSource(reader);
			is.setEncoding("UTF-8");
			Document doc = docBuilder.parse(is);

			// Parse out the XML
			NodeList redirectList = doc.getElementsByTagName("r");
			Node redirect = redirectList.item(0);
			if (redirect == null) {
				return false;
			} else {
				return true;
			}
		} catch (ParserConfigurationException e) {
			System.err.println("An error occurred parsing the server response.");
			e.printStackTrace();
		} catch (SAXException e) {
			System.err.println("An error occurred parsing the server response.");
			e.printStackTrace();
		} catch (IOException e) {
			System.err.println("An error occurred parsing the server response.");
			e.printStackTrace();
		}
		return false;
	}  // end isRedirected()
	
	public String getRedirectName() {
		String redirectName = "";
		String query = url+"api.php?action=query&format=xml";
		try {
			query += "&redirects&titles="+URLEncoder.encode(articleTitle.replace(' ' , '_'), "UTF-8");
		} catch (UnsupportedEncodingException e1) {
			return redirectName;
		}
		
		String response;
		try {
			response = HttpQueries.sendGetQuery(query, client);
		} catch (HTTPException e) {
			return redirectName;
		}

		try {
			// Setup XML helpers
			DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
			InputStream in = new ByteArrayInputStream(response.getBytes());
			Reader reader = new InputStreamReader(in, "UTF-8");
			InputSource is = new InputSource(reader);
			is.setEncoding("UTF-8");
			Document doc = docBuilder.parse(is);

			// Parse out the XML
			NodeList redirectList = doc.getElementsByTagName("r");
			Node redirect = redirectList.item(0);
			if (redirect == null) {
				return redirectName;
			} else {
				NamedNodeMap attributes = redirect.getAttributes();
				Node toAttrib = attributes.getNamedItem("to");
				redirectName = toAttrib.getNodeValue();
				return redirectName;
			}
		} catch (ParserConfigurationException e) {
			System.err.println("An error occurred parsing the server response.");
			e.printStackTrace();
		} catch (SAXException e) {
			System.err.println("An error occurred parsing the server response.");
			e.printStackTrace();
		} catch (IOException e) {
			System.err.println("An error occurred parsing the server response.");
			e.printStackTrace();
		}
		return redirectName;
	}
}
