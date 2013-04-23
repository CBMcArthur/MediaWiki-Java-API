package com.kirkman_enterprises.MWJAPI;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.http.impl.client.DefaultHttpClient;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class WikiCategories {
	
	private String category;
	private String url;
	private DefaultHttpClient client;

	public WikiCategories(String category, String url, DefaultHttpClient client) {
		this.category = category;
		this.url = url;
		this.client = client;
	}

	public List<String> getCategoryMembers() throws HTTPException {
		List<String> returningMembers = new ArrayList<String>();
		
		String query = url+"api.php?action=query&format=xml&list=categorymembers&cmtitle="+category;
		query += "&cmprop=title&cmlimit=max";
		
		String response = "";
		boolean hasMore = false;
		do {
			if (!hasMore) { 
				response = HttpQueries.sendGetQuery(query, client);
				//System.out.println("Query: "+query);
			} else {
				String contValue = parseQueryContinue(response);
				response = HttpQueries.sendGetQuery(query+"&cmcontinue="+contValue, client);
				//System.out.println("Query: "+query+"&cmcontinue="+contValue);
			}
			//System.out.println(response);
			returningMembers.addAll(parseCategoryMembers(response));
			hasMore = existsQueryContinue(response);
		} while (hasMore);
		
		return returningMembers;
	}  // end getCategoryMembers()
	
	private List<String> parseCategoryMembers(String xml) {
		List<String> ret = new ArrayList<String>();
		
		try {
			// Setup XML helpers
			DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
			InputStream in = new ByteArrayInputStream(xml.getBytes());
			Reader reader = new InputStreamReader(in, "UTF-8");
			InputSource is = new InputSource(reader);
			is.setEncoding("UTF-8");
			Document doc = docBuilder.parse(is);
	
			// Parse out the XML
			NodeList categoryMembers = doc.getElementsByTagName("cm");
			Node member, title;
			NamedNodeMap memberAttribs;
			for (int i=0; i < categoryMembers.getLength(); i++) {
				member = categoryMembers.item(i);
				memberAttribs = member.getAttributes();
				title = memberAttribs.getNamedItem("title");
				ret.add(title.getNodeValue());
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
		
		return ret;
	}  // end parseCategoryMembers
	
	private boolean existsQueryContinue(String xml) {
		
		try {
			// Setup XML helpers
			DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
			InputStream in = new ByteArrayInputStream(xml.getBytes());
			Reader reader = new InputStreamReader(in, "UTF-8");
			InputSource is = new InputSource(reader);
			is.setEncoding("UTF-8");
			Document doc = docBuilder.parse(is);
	
			// Parse out the XML
			NodeList queryContinue = doc.getElementsByTagName("query-continue");
			if (queryContinue.getLength() == 1)
				return true;
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
	}
	
	private String parseQueryContinue(String xml) {
		try {
			// Setup XML helpers
			DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
			InputStream in = new ByteArrayInputStream(xml.getBytes());
			Reader reader = new InputStreamReader(in, "UTF-8");
			InputSource is = new InputSource(reader);
			is.setEncoding("UTF-8");
			Document doc = docBuilder.parse(is);
	
			// Parse out the XML
			NodeList queryContinue = doc.getElementsByTagName("query-continue");
			Node queryContNode = queryContinue.item(0);
			NodeList categorymembers = queryContNode.getChildNodes();
			Node catMemNode = categorymembers.item(0);
			NamedNodeMap catMemAttrib = catMemNode.getAttributes();
			Node cmcontinue = catMemAttrib.getNamedItem("cmcontinue");
			return URLEncoder.encode(cmcontinue.getNodeValue().replace(' ', '_'), "UTF-8");
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
		
		return "";

	}

}
