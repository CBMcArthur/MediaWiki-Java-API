package com.kirkman_enterprises.MWJAPI;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.BasicResponseHandler;

public class HttpQueries {
	
	public static String sendGetQuery(String query, HttpClient client) throws HTTPException {
		// Send the query
		HttpGet queryRequest = new HttpGet(query);
		ResponseHandler<String> responseHandler = new BasicResponseHandler();

		String responseBody;
		try {
			responseBody = client.execute(queryRequest, responseHandler);
		} catch (ClientProtocolException e) {
			throw new HTTPException("An HTTP protocol error occurred.");
		} catch (IOException e) {
			e.printStackTrace();
			throw new HTTPException("The connection was aborted.");
		}
		
		//System.out.println(responseBody);
		return responseBody.toString();
	}
	
	public static String sendPostQuery(String url, List<NameValuePair> postParams, boolean setContentType, HttpClient client) 
			throws HTTPException {
		// Send the query
		HttpPost queryRequest = new HttpPost(url);
		UrlEncodedFormEntity entity;
		try {
			entity = new UrlEncodedFormEntity(postParams, "UTF-8");
		} catch (UnsupportedEncodingException e1) {
			e1.printStackTrace();
			throw new HTTPException("An error occured encoding the post request parameters.");
		}
		queryRequest.setEntity(entity);
		
		if (setContentType) {
			queryRequest.addHeader("Content-Type", "application/x-www-form-urlencoded");
		}
		
		ResponseHandler<String> responseHandler = new BasicResponseHandler();
		String responseBody;
		try {
			responseBody = client.execute(queryRequest, responseHandler);
		} catch (ClientProtocolException e) {
			e.printStackTrace();
			throw new HTTPException("An HTTP protocol error occurred.");
		} catch (IOException e) {
			e.printStackTrace();
			throw new HTTPException("The connection was aborted.");
		}
		
		return responseBody;
	}

}
