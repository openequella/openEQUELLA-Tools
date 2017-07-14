package com.pearson.equella.support.oauthtester.util;

import java.io.IOException;
import java.util.ArrayList;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

public class UrlCaller {
	private static final Logger logger = LogManager.getLogger(UrlCaller.class);

	public static JSONObject handleTemporaryCode(String tempCode) throws ClientProtocolException, IOException {
		JSONObject resp = new JSONObject();
		String url = String.format("%soauth/access_token?grant_type=authorization_code&client_id=%s&redirect_uri=%s&code=%s",
				Config.getEndpoint(), Config.getClientId(), Config.getRedirectUrl(), tempCode);
		logger.info(String.format("URL:  [%s]", url));
		CloseableHttpClient httpclient = HttpClients.createDefault();
		HttpGet httpGet = new HttpGet(url);
		CloseableHttpResponse response1 = httpclient.execute(httpGet);
		
		try {
		    resp.put("status", ""+response1.getStatusLine().getStatusCode());
		    HttpEntity entity1 = response1.getEntity();
		    String respObj = IOUtils.toString(entity1.getContent());
		    logger.info(String.format("Response status:  [%d], Response json: [%s]", response1.getStatusLine().getStatusCode(), respObj));
			JSONObject jsonObj = new JSONObject(respObj);
		    String payload = "";
		    if(jsonObj.has("access_token")) {
		    	payload = jsonObj.getString("access_token");
		    } else {
		    	payload = respObj;
		    }
		    resp.put("payload", payload);
		    // do something useful with the response body
		    // and ensure it is fully consumed
		    EntityUtils.consume(entity1);
		} finally {
		    response1.close();
		}
		return resp;
	}

	public static JSONObject makeRestCall(String access_token) throws ClientProtocolException, IOException {
		JSONObject resp = new JSONObject();
		String url = String.format("%sapi/collection/", Config.getEndpoint());
		logger.info(String.format("list collections - URL:  [%s]", url));
		
		CloseableHttpClient httpclient = HttpClients.createDefault();
		HttpGet httpGet = new HttpGet(url);
		httpGet.setHeader("X-Authorization", String.format("access_token=%s", access_token));
		CloseableHttpResponse response1 = httpclient.execute(httpGet);
		// The underlying HTTP connection is still held by the response object
		// to allow the response content to be streamed directly from the network socket.
		// In order to ensure correct deallocation of system resources
		// the user MUST call CloseableHttpResponse#close() from a finally clause.
		// Please note that if response content is not fully consumed the underlying
		// connection cannot be safely re-used and will be shut down and discarded
		// by the connection manager. 
		try {
			String status = ""+response1.getStatusLine().getStatusCode();
		    resp.put("status", status);
		    HttpEntity entity1 = response1.getEntity();
		    String respObj = IOUtils.toString(entity1.getContent());
		    logger.info(String.format("list collections - Status: [%s] - Response: [%s]", status, respObj));
		    String payload = "";
		    if(status.equals("200")) {
			    JSONObject jsonObj = new JSONObject(respObj);
			    if(jsonObj.has("results")) {
			    	JSONArray collectionResults = jsonObj.getJSONArray("results");
			    	ArrayList<String> collectionNames = new ArrayList<String>();
			    	
			    	for(int i = 0; i < collectionResults.length(); i++) {
			    		collectionNames.add(collectionResults.getJSONObject(i).getString("name"));
			    	}
			    	payload = collectionNames.toString();
			    } else {
			    	payload = respObj;
			    }
		    } else {
		    	payload = "Error calling REST API.  Check logs for details.";
		    }
		    resp.put("payload", payload);
		    EntityUtils.consume(entity1);
		} finally {
		    response1.close();
		}
		return resp;
	}
}
