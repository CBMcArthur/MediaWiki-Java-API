package com.kirkman_enterprises.MWJAPI;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;

import com.kirkman_enterprises.MWJAPI.objects.LoginParameters;

public class Login {

	private String username;
	private String password;
	private String url;
	private DefaultHttpClient client;
	private String localStorage;
	
	public Login(String username, String password, String url, DefaultHttpClient client, String localStorage) {
		this.username = username;
		this.password = password;
		this.url = url;
		this.client = client;
		this.localStorage = localStorage;
	}

	public boolean login() throws HTTPException {
		LoginParameters loginParameters = new LoginParameters();
		
		// Check the client cookies.  If 'enwikiUserID', 'enwikiUserName', and 'centralauth_Token' are valid, already logged in.
		List<Cookie> cookies = client.getCookieStore().getCookies();
		int numCookieFound = 0;
		Iterator<Cookie> cookieItr = cookies.iterator();
		while (cookieItr.hasNext()) {
			Cookie cookie = cookieItr.next();
			if (cookie.getName().equalsIgnoreCase("enwikiUserID")) 
				numCookieFound++;
			else if (cookie.getName().equalsIgnoreCase("enwikiUsername"))
				numCookieFound++;
			else if (cookie.getName().equalsIgnoreCase("centralauth_Token"))
				numCookieFound++;
			if (numCookieFound == 3)
				return true;
		}
		
		
		// Send the initial login request
		String loginQuery = url+"api.php?action=login&lgname="+username+"&lgpassword="+password+"&format=xml";
		HttpPost loginRequest = new HttpPost(loginQuery);
		
		ResponseHandler<String> responseHandler = new BasicResponseHandler();
		String responseBody;
		try {
			responseBody = client.execute(loginRequest, responseHandler);
		} catch (ClientProtocolException e) {
			throw new HTTPException("An HTTP protocol error occurred.");
		} catch (IOException e) {
			e.printStackTrace();
			throw new HTTPException("The connection was aborted.");
		}		
		loginParameters.setXMLParameters(responseBody);
		
		// Send the confirm token request
		String confirmTokenQuery = url+"api.php?action=login&lgname="+username+"&lgpassword="+password+
				"&lgtoken="+loginParameters.getToken()+"&format=xml";
		HttpPost confirmTokenRequest = new HttpPost(confirmTokenQuery);
		try {
			responseBody = client.execute(confirmTokenRequest, responseHandler);
		} catch (ClientProtocolException e) {
			throw new HTTPException("An HTTP protocol error occurred.");
		} catch (IOException e) {
			throw new HTTPException("The connection was aborted.");
		} 
		loginParameters.setXMLParameters(responseBody);

		
		// Save the cookie information.
		cookies = client.getCookieStore().getCookies();
		cookieItr = cookies.iterator();
		ArrayList<String> cookieInfo = new ArrayList<String>();
		while (cookieItr.hasNext()) {
			Cookie cookie = cookieItr.next();
			if (cookie.isPersistent() && !cookie.isExpired(new Date())) {
				String cookieDetails = cookie.getComment()+","+cookie.getCommentURL()+","+cookie.getDomain()+","+
						cookie.getName()+","+cookie.getPath()+","+cookie.getValue()+","+cookie.getVersion()+","+
						cookie.getExpiryDate().toString();
				cookieInfo.add(cookieDetails);
			}
		}
		addCookiesToFile(cookieInfo);
		
		return false;
	}
	
	private void addCookiesToFile(ArrayList<String> cookieInfo) {
		if (localStorage == null)
			return;
		if (cookieInfo.size() == 0)
			return;
		
		try {
			PrintWriter out = new PrintWriter(new FileWriter(localStorage+"/cookie.txt"));
			Iterator<String> cookieItr = cookieInfo.iterator();
			String info;
			while (cookieItr.hasNext()) {
				info = cookieItr.next();
				out.println(info);
			}
			out.close();
		} catch (IOException e) {
			System.err.println("An error occured trying to write cookie information to file.");
			e.printStackTrace();
		}	
	}  // end addCookiesToFile()
	

}
