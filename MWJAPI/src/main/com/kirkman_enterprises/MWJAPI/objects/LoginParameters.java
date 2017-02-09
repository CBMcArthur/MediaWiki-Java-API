package com.kirkman_enterprises.MWJAPI.objects;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class LoginParameters {

	private String userid;
	private String username;
	private String token;
	private String cookieprefix;
	private String sessionid;
	private String status;
	
	public LoginParameters() {
		userid = "";
		username = "";
		token = "";
		cookieprefix = "";
		sessionid = "";
		status = "";
	}  // default constructor
	
	public void setXMLParameters(String xmlParameters) {
		try {
			// Setup Helper Objects
			DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
			
			// Parse the xml parameters
			Document doc = docBuilder.parse(new ByteArrayInputStream(xmlParameters.getBytes()));
			
			NodeList loginList = doc.getElementsByTagName("login");
			Node loginNode = loginList.item(0);
			NamedNodeMap loginProps = loginNode.getAttributes();
			
			status = loginProps.getNamedItem("result").getNodeValue();
			cookieprefix = loginProps.getNamedItem("cookieprefix").getNodeValue();
			sessionid = loginProps.getNamedItem("sessionid").getNodeValue();
			if (status.equals("NeedToken")) {
				token = loginProps.getNamedItem("token").getNodeValue();
			} else if (status.equals("Success")) {
				userid = loginProps.getNamedItem("lguserid").getNodeValue();
				username = loginProps.getNamedItem("lgusername").getNodeValue();
				token = loginProps.getNamedItem("lgtoken").getNodeValue();
			}

		} catch (ParserConfigurationException e) {
			System.out.println("Error creating XML document builder.");
			e.printStackTrace();
		} catch (SAXException e) {
			System.out.println("A SAX exception occured.");
			e.printStackTrace();
		} catch (IOException e) {
			System.out.println("An IO Exception occured.");
			e.printStackTrace();
		}
		
	}  // end setXMLParameters()
	
	public String getToken() {
		return token;
	}
	
	public String getUserid() {
		return userid;
	}
	
	public String getUsername() {
		return username;
	}
	
	public String getCookieprefix() {
		return cookieprefix;
	}
	
	public String getSessionid() {
		return sessionid;
	}
	
	public String getStatus() {
		return status;
	}
}
