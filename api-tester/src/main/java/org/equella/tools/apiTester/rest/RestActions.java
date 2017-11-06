/*
 * Copyright 2017 Apereo
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.equella.tools.apiTester.rest;

import java.io.IOException;
import java.util.UUID;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;
import org.json.JSONTokener;

public class RestActions {
	private Logger logger = LogManager.getLogger(RestActions.class);

	private String accessToken = "NOTSET";
	private String baseUrl;
	private String clientId;
	private String clientSecret;
	private String testUserUuid;
	private String testUserName;
	private String testFirstName = "Temp";
	private String testLastName = "API Tester";
	private String testUserEmail = "no-reply@example.com";

	public RestActions(String baseUrl, String clientId, String clientSecret) {
		this.baseUrl = baseUrl;
		this.clientId = clientId;
		this.clientSecret = clientSecret;
	}

	public void gatherAccessToken() throws Exception {
		CloseableHttpClient httpclient = HttpClients.createDefault();
		try {
			String url = String.format(
					"%s/oauth/access_token?grant_type=client_credentials&client_id=%s&client_secret=%s&redirect_uri=default",
					baseUrl, clientId, clientSecret);
			HttpGet httpget = new HttpGet(url);

			// Execute HTTP request
			logger.trace("Finding the access token using [" + url + "]");

			HttpResponse response;

			response = httpclient.execute(httpget);
			HttpEntity entity = response.getEntity();

			int statusCode = response.getStatusLine().getStatusCode();

			String respStr = EntityUtils.toString(entity);
			if (statusCode == HttpStatus.SC_OK) {
				JSONObject jresponse = new JSONObject(new JSONTokener(respStr));
				if (!jresponse.has("access_token")) {
					String msg = String.format("FAILURE:  Request for access_token didn't return expected JSON key: %s",
							respStr);
					logger.error(msg);
					throw new Exception(msg);
				} else {
					accessToken = jresponse.getString("access_token");
				}
			} else {
				String msg = String.format("FAILURE:  Request for access_token failed with [%s]: %s", statusCode,
						respStr);
				logger.error(msg);
				throw new Exception(msg);
			}

		} finally {
			httpclient.close();
		}
		logger.info("Access token gathered successfully.");
	}

	public void seedTestUserUuid() {
		this.testUserUuid = UUID.randomUUID().toString();
		this.testUserName = "temp-api-tester-"+System.currentTimeMillis();
	}

	public void getUser(boolean shouldExist) throws Exception {
		if (shouldExist) {
			logger.trace("Confirming user %s exists.");
		} else {
			logger.trace("Confirming user %s does not exists.");
		}
		CloseableHttpClient httpclient = HttpClients.createDefault();
		try {
			String url = baseUrl + "/api/usermanagement/local/user/" + testUserUuid;
			HttpGet http = new HttpGet(url);

			http.addHeader("X-Authorization", "access_token=" + accessToken);
			HttpResponse response = httpclient.execute(http);
			HttpEntity entity = response.getEntity();

			int statusCode = response.getStatusLine().getStatusCode();

			String respStr = EntityUtils.toString(entity);
			logger.trace(statusCode);
			logger.trace(respStr);
			if (shouldExist) {
				if (statusCode != 200) {
					String msg = String.format("FAILURE confirming user exists.  Unexpected http code:  %s",
							statusCode);
					logger.error(msg);
					throw new Exception(msg);
				}
				
				JSONObject userObj = new JSONObject(respStr);
				if(!testUserUuid.equals(userObj.get("id"))) {
					String msg = String.format("FAILURE confirming user exists.  Unexpected id of:  %s.  Should be %s",
							userObj.get("id"), testUserUuid);
					logger.error(msg);
					throw new Exception(msg);
				}
				
				if(!testUserName.equals(userObj.get("username"))) {
					String msg = String.format("FAILURE confirming user exists.  Unexpected username of:  %s.  Should be %s",
							userObj.get("username"), testUserName);
					logger.error(msg);
					throw new Exception(msg);
				}
				
				if(!testFirstName.equals(userObj.get("firstName"))) {
					String msg = String.format("FAILURE confirming user exists.  Unexpected firstName of:  %s.  Should be %s",
							userObj.get("firstName"), testFirstName);
					logger.error(msg);
					throw new Exception(msg);
				}

				if(!testLastName.equals(userObj.get("lastName"))) {
					String msg = String.format("FAILURE confirming user exists.  Unexpected id of:  %s.  Should be %s",
							userObj.get("lastName"), testLastName);
					logger.error(msg);
					throw new Exception(msg);
				}
			
				if(!testUserEmail.equals(userObj.get("emailAddress"))) {
					String msg = String.format("FAILURE confirming user exists.  Unexpected email of:  %s.  Should be %s",
							userObj.get("emailAddress"), testUserEmail);
					logger.error(msg);
					throw new Exception(msg);
				}
				logger.info("Confirmed user exists successfully.");
			} else {
				if (statusCode != 404) {
					String msg = String.format("FAILURE confirming user does not exist.  Unexpected http code:  %s",
							statusCode);
					logger.error(msg);
					throw new Exception(msg);
				}

				JSONObject jresponse = new JSONObject(new JSONTokener(respStr));
				if ("HTTP 404 Not Found".equals(jresponse.getString("error_description"))) {
					logger.info("Confirmed user does not exist successfully");
				} else {
					String msg = String.format("FAILURE confirming user does not exist from the output:  %s",
							jresponse.toString(2));
					logger.error(msg);
					throw new Exception(msg);
				}
			}
		} catch (ClientProtocolException e) {
			throw new Exception("FAILURE checking getUser: " + e.getMessage());
		} catch (IOException e) {
			throw new Exception("FAILURE checking getUser: " + e.getMessage());
		} finally {
			httpclient.close();
		}
	}

	public void createUser() throws Exception {
		CloseableHttpClient httpclient = HttpClients.createDefault();
		try {
			String url = baseUrl + "/api/usermanagement/local/user/";
			HttpPost http = new HttpPost(url);
			logger.trace("User UUID: "+ testUserUuid);
			JSONObject userObj = new JSONObject();
			userObj.put("id", testUserUuid);
			userObj.put("username", testUserName);
			userObj.put("firstName", testFirstName);
			userObj.put("lastName", testLastName);
			userObj.put("emailAddress", testUserEmail);
			JSONObject export = new JSONObject();
			export.put("passwordHash", "asdf1234");
			userObj.put("_export", export);
			
			StringEntity input = new StringEntity(userObj.toString());
			input.setContentType("application/json");
			http.setEntity(input);
			
			http.addHeader("X-Authorization", "access_token=" + accessToken);
			HttpResponse response = httpclient.execute(http);
			HttpEntity entity = response.getEntity();

			int statusCode = response.getStatusLine().getStatusCode();

			String respStr = EntityUtils.toString(entity);
			logger.trace(statusCode);
			logger.trace(respStr);
			if (statusCode != 201) {
				String msg = String.format("FAILURE creating user.  Unexpected http code:  %s",
						statusCode);
				logger.error(msg);
				throw new Exception(msg);
			} else {
				logger.info("Created user successfully.");
			}
		} catch (ClientProtocolException e) {
			throw new Exception("FAILURE creating user: " + e.getMessage());
		} catch (IOException e) {
			throw new Exception("FAILURE creating user: " + e.getMessage());
		} finally {
			httpclient.close();
		}
	}

	public void updateUser() throws Exception {
		CloseableHttpClient httpclient = HttpClients.createDefault();
		try {
			String url = baseUrl + "/api/usermanagement/local/user/"+testUserUuid;
			HttpPut http = new HttpPut(url);
			
			testFirstName = testFirstName+"UPDATED";
			testLastName = testLastName+"UPDATED";
			
			JSONObject userObj = new JSONObject();
			userObj.put("username", testUserName);
			userObj.put("firstName", testFirstName);
			userObj.put("lastName", testLastName);
			userObj.put("emailAddress", testUserEmail);
			
			StringEntity input = new StringEntity(userObj.toString());
			input.setContentType("application/json");
			http.setEntity(input);
			
			http.addHeader("X-Authorization", "access_token=" + accessToken);
			HttpResponse response = httpclient.execute(http);
			HttpEntity entity = response.getEntity();

			int statusCode = response.getStatusLine().getStatusCode();

			String respStr = EntityUtils.toString(entity);
			logger.trace(statusCode);
			logger.trace(respStr);
			if (statusCode != 200) {
				String msg = String.format("FAILURE updating user.  Unexpected http code:  %s",
						statusCode);
				logger.error(msg);
				throw new Exception(msg);
			} else {
				logger.info("Updated user successfully.");
			}
			
			if(!testUserUuid.equals(respStr)) {
				String msg = String.format("FAILURE updating user.  Unexpected response of:  %s, expected %s",
						respStr, testUserUuid);
				logger.error(msg);
				throw new Exception(msg);
			}
		} catch (ClientProtocolException e) {
			throw new Exception("FAILURE updating user: " + e.getMessage());
		} catch (IOException e) {
			throw new Exception("FAILURE updating user: " + e.getMessage());
		} finally {
			httpclient.close();
		}
	}

	public void deleteUser() throws Exception {
		CloseableHttpClient httpclient = HttpClients.createDefault();
		try {
			String url = baseUrl + "/api/usermanagement/local/user/"+testUserUuid;
			HttpDelete http = new HttpDelete(url);
			
			http.addHeader("X-Authorization", "access_token=" + accessToken);
			HttpResponse response = httpclient.execute(http);
			
			int statusCode = response.getStatusLine().getStatusCode();

			logger.trace(statusCode);
			if (statusCode != 204) {
				String msg = String.format("FAILURE deleting user.  Unexpected http code:  %s",
						statusCode);
				logger.error(msg);
				throw new Exception(msg);
			} else {
				logger.info("Deleted user successfully.");
			}
		} catch (ClientProtocolException e) {
			throw new Exception("FAILURE deleting user: " + e.getMessage());
		} catch (IOException e) {
			throw new Exception("FAILURE deleting user: " + e.getMessage());
		} finally {
			httpclient.close();
		}
	}
}
