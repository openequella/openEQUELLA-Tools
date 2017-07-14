package com.pearson.equella.support;

import java.io.InputStreamReader;

import org.apache.http.Header;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;
import org.json.JSONTokener;

public class RestExamplesDriver {
	private static final Logger logger = LogManager.getLogger(RestExamplesDriver.class);
	public static final String URL = "https://.....";
	public static final String CLIENT_ID = "....";
	public static final String CLIENT_SECRET = "....";
	public static final String unique = "99999";
	
	public static void main(String[] args) throws Exception {
		itemBuilder();
	}
	
	public static void itemBuilder() throws Exception {
		logger.info("Beginning REST Item Builder.");
		String token = findAccessToken();
		JSONObject item = new JSONObject();
		item.put("name", "...."+unique);
		item.put("metadata", "<xml><your-schema-nodes-here></xml>");
		JSONObject coll = new JSONObject();
		coll.put("uuid", "....");
		item.put("collection", coll);
		post(token, item.toString());
	}
	
	protected static String findAccessToken() throws Exception {
		String accessToken = null;
		CloseableHttpClient httpclient = HttpClients.createDefault();
		try {
			String url = URL + "/oauth/access_token?grant_type=client_credentials&client_id=" + CLIENT_ID +
					"&client_secret=" + CLIENT_SECRET +
					"&redirect_uri=default";
			HttpGet httpget = new HttpGet(url);
			
			// Execute HTTP request
			logger.info("Finding the access token using ["+url+"]");
			//httpget.addHeader("X-Authorization", "access_token=" + accessToken);
			CloseableHttpResponse response = httpclient.execute(httpget);
			try {
				int statusCode = response.getStatusLine().getStatusCode();

				if (statusCode == HttpStatus.SC_OK) {
					JSONObject jresponse = new JSONObject(new JSONTokener(
							new InputStreamReader(
									response.getEntity().getContent())));
					logger.info("Query response: " + jresponse.toString(2));

					if(!jresponse.has("access_token")) {
						logger.error("Request for access_token didn't return expected JSON key");
					} else {
						accessToken = jresponse.getString("access_token");
					}
				} else {
					logger.error("Request for access_token failed with ["+statusCode+"]: "+ response.getEntity().getContent());
				}
			} finally {
				response.close();
			}
		} finally {
			httpclient.close();
		}
		return accessToken;
	}
	
	protected static String post(String token, String json) throws Exception {
		String accessToken = null;
		CloseableHttpClient httpclient = HttpClients.createDefault();
		try {
			String url = URL + "/api/item?draft=true";
			HttpPost httpPost = new HttpPost(url);
			
			StringEntity input = new StringEntity(json);
			input.setContentType("application/json");
			httpPost.setEntity(input);
			
			// Execute HTTP request
			logger.info("Finding the access token using ["+url+"]");
			httpPost.addHeader("X-Authorization", "access_token=" + token);
			CloseableHttpResponse response = httpclient.execute(httpPost);
			try {
				int statusCode = response.getStatusLine().getStatusCode();
				logger.info("Response status code: " + statusCode);
				if(statusCode == 201) {
					Header h = response.getFirstHeader("Location");
					logger.info("Response header [Location]: " + h.getValue());
				} else {
					try {
						JSONObject jresponse = new JSONObject(new JSONTokener(
								new InputStreamReader(
										response.getEntity().getContent())));
						logger.info("Response data: " + jresponse.toString(2));
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			} finally {
				response.close();
			}
		} finally {
			httpclient.close();
		}
		return accessToken;
	}
}
