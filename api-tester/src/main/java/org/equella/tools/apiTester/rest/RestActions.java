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

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.util.Date;
import java.util.UUID;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
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

	public JSONObject getItem(String uuid, String version) throws Exception {
		CloseableHttpClient httpclient = HttpClients.createDefault();
		String url = baseUrl + "/api/item/" + uuid + "/" + version;
		String purpose = "getting item with URL: " + url;

		try {
			logger.trace(purpose);
			HttpGet http = new HttpGet(url);

			http.addHeader("X-Authorization", "access_token=" + accessToken);
			HttpResponse response = httpclient.execute(http);
			HttpEntity entity = response.getEntity();

			int statusCode = response.getStatusLine().getStatusCode();

			String respStr = EntityUtils.toString(entity);
			logger.trace(statusCode);
			logger.trace(respStr);
			if (statusCode != 200) {
				String msg = String.format("FAILURE %s.  Unexpected http code:  %s",
						purpose, statusCode);
				logger.error(msg);
				throw new Exception(msg);
			}

			JSONObject jObj = new JSONObject(respStr);
			logger.info("The item JSON is: " + jObj.toString(4));
			return jObj;
		} catch (ClientProtocolException e) {
			throw new Exception("FAILURE " + purpose + ": " + e.getMessage());
		} catch (IOException e) {
			throw new Exception("FAILURE " + purpose + ": " + e.getMessage());
		} finally {
			httpclient.close();
		}

	}

	public void deleteItem(String uuid, String version) throws Exception {
		CloseableHttpClient httpclient = HttpClients.createDefault();
		String url = baseUrl + "/api/item/" + uuid + "/" + version;
		String purpose = "deleting item with URL: " + url;

		try {
			logger.trace(purpose);
			HttpDelete http = new HttpDelete(url);

			http.addHeader("X-Authorization", "access_token=" + accessToken);
			HttpResponse response = httpclient.execute(http);

			int statusCode = response.getStatusLine().getStatusCode();

			logger.trace(statusCode);
			if (statusCode != 204) {
				String msg = String.format("FAILURE %s.  Unexpected http code:  %s",
						purpose, statusCode);
				logger.error(msg);
				throw new Exception(msg);
			}

			logger.info("Item was deleted.");
		} catch (ClientProtocolException e) {
			throw new Exception("FAILURE " + purpose + ": " + e.getMessage());
		} catch (IOException e) {
			throw new Exception("FAILURE " + purpose + ": " + e.getMessage());
		} finally {
			httpclient.close();
		}

	}

	public void createItemNoFiles(String collectionUuid) throws Exception {
		CloseableHttpClient httpclient = HttpClients.createDefault();
		String url = baseUrl + "/api/item/?draft=false&waitforindex=true";
		String purpose = "create item with URL: " + url;
		try {
			String title = "Item created from REST API - " + (new Date());
			String metadata = String.format("<xml><metadata><title>%s</title><description>%s</description></metadata></xml>", title, "Testing the REST API!");


			HttpPost http = new HttpPost(url);
			JSONObject jObj = new JSONObject();
			jObj.put("name", title);
			jObj.put("metadata", metadata);
			jObj.put("status", "live");

			JSONObject collObj = new JSONObject();
			collObj.put("uuid", collectionUuid);
			jObj.put("collection", collObj);

			StringEntity input = new StringEntity(jObj.toString());
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
				String msg = String.format("FAILURE %s.  Unexpected http code:  %s",
						purpose, statusCode);
				logger.error(msg);
				throw new Exception(msg);
			} else {
				String location = response.getHeaders("Location")[0].getValue();
				logger.info("Ran " + purpose + " successfully - " + location);
			}
		} catch (ClientProtocolException e) {
			throw new Exception("FAILURE " + purpose + ": " + e.getMessage());
		} catch (IOException e) {
			throw new Exception("FAILURE " + purpose + ": " + e.getMessage());
		} finally {
			httpclient.close();
		}
	}

	public String createStagingArea() throws Exception {
		CloseableHttpClient httpclient = HttpClients.createDefault();
		String url = baseUrl + "/api/staging";
		String purpose = "create staging area with URL: " + url;
		try {
			HttpPost http = new HttpPost(url);

			http.addHeader("X-Authorization", "access_token=" + accessToken);
			HttpResponse response = httpclient.execute(http);
			HttpEntity entity = response.getEntity();

			int statusCode = response.getStatusLine().getStatusCode();
			String respStr = EntityUtils.toString(entity);
			logger.trace(statusCode);
			logger.trace(respStr);
			if (statusCode != 201) {
				String msg = String.format("FAILURE %s.  Unexpected http code:  %s",
						purpose, statusCode);
				logger.error(msg);
				throw new Exception(msg);
			} else {
				String[] locationUrl = response.getHeaders("Location")[0].getValue().split("/");
				logger.trace("Staging location URL: " + response.getHeaders("Location")[0].getValue());
				String location = locationUrl[locationUrl.length-1];
				logger.info("Ran " + purpose + " successfully - " + location);
				return location;
			}
		} catch (ClientProtocolException e) {
			throw new Exception("FAILURE " + purpose + ": " + e.getMessage());
		} catch (IOException e) {
			throw new Exception("FAILURE " + purpose + ": " + e.getMessage());
		} finally {
			httpclient.close();
		}
	}

	public void uploadAttachment(String filearea, String srcDirectory, String filename) throws Exception {
		logger.info("Uploading attachment {}", filename);

		File toUpload = new File(srcDirectory, filename);
		logger.trace("Filename to upload:  "+toUpload.getAbsolutePath());
		String size = ""+toUpload.length();
		logger.trace("File size:  ["+size+"]");
		String url = String.format("%s/api/staging/%s/%s", baseUrl, filearea, filename.replaceAll(" ", "%20"));//filename.replaceAll("", "%20")
		HttpURLConnection connection = ((HttpURLConnection)new URL(String.format("%s/api/staging/%s/%s", baseUrl, filearea, "cat.jpg")).openConnection());
		connection.setRequestMethod("PUT");
		connection.addRequestProperty("Content-Type", "image/jpeg");
		connection.addRequestProperty("Content-Length", size);
		connection.addRequestProperty("X-Authorization", "access_token=" + accessToken);

		connection.setUseCaches(false);
		connection.setDoOutput(true);

		connection.connect();

		OutputStream outputStream = connection.getOutputStream();

		MessageDigest md = MessageDigest.getInstance("MD5");
		InputStream is = Files.newInputStream(Paths.get(srcDirectory, filename));
		DigestInputStream dis = new DigestInputStream(is, md);
		{
			/* Read decorated stream (dis) to EOF as normal... */
		}
		//FileInputStream streamFileInputStream = new FileInputStream(toUpload);
		BufferedInputStream streamFileBufferedInputStream = new BufferedInputStream(dis);

		byte[] streamFileBytes = new byte[1024];
		int bytesRead = 0;
		int totalBytesRead = 0;
		long counter = 0;
		while ((bytesRead = streamFileBufferedInputStream.read(streamFileBytes)) > 0) {
			outputStream.write(streamFileBytes, 0, bytesRead);
			outputStream.flush();

			totalBytesRead += bytesRead;
			counter++;
			if(counter % 5000 == 0) {
				System.out.println(".");
			} else if(counter % 1000 == 0) {
				System.out.print(".");
			}
		}
		logger.info("File upload response code: {}",connection.getResponseCode());
		outputStream.close();
		String checksum = "";
		for(byte b : md.digest())
			checksum += String.format("%02x", Byte.toUnsignedInt(b));
		if(totalBytesRead != toUpload.length()) {
			throw new Exception("Total Bytes ("+totalBytesRead+") read is NOT equal to file size ("+toUpload.length());
		}
		//return checksum;
	}

	public void createItemWithAttachments(String collectionUuid, String stagingUuid, String attUrl, String attFilename) throws Exception {
		CloseableHttpClient httpclient = HttpClients.createDefault();
		String url = baseUrl + "/api/item/?draft=false&waitforindex=true&file="+stagingUuid;
		String purpose = "create item + atts with URL: " + url;
		try {
			String title = "Item created from REST API - " + (new Date());
			String metadata = String.format(
					"<xml>" +
							"<metadata>" +
								"<title>%s</title>" +
							"<description>%s</description>" +
							"<contentObjects>" +
								"<contentObject>uuid:attUuid1</contentObject>" +
								"<contentObject>uuid:attUuid2</contentObject>" +
							"</contentObjects>" +
						"</metadata>" +
					"</xml>",
					title,
					"Testing the REST API!");


			HttpPost http = new HttpPost(url);
			JSONObject jObj = new JSONObject();
			jObj.put("name", title);
			jObj.put("metadata", metadata);
			jObj.put("status", "live");

			JSONObject collObj = new JSONObject();
			collObj.put("uuid", collectionUuid);
			jObj.put("collection", collObj);

			JSONObject attObj = new JSONObject();
			attObj.put("uuid", "uuid:attUuid1");
			attObj.put("description", "A Fancy URL Attachment");
			attObj.put("type", "url");
			attObj.put("url", attUrl);

			JSONObject attObj2 = new JSONObject();
			attObj2.put("uuid", "uuid:attUuid2");
			attObj2.put("description", "A test file upload");
			attObj2.put("type", "file");
			attObj2.put("filename", "cat.jpg");

			JSONArray attsArr = new JSONArray();
			attsArr.put(attObj);
			attsArr.put(attObj2);
			jObj.put("attachments", attsArr);

			StringEntity input = new StringEntity(jObj.toString());
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
				String msg = String.format("FAILURE %s.  Unexpected http code:  %s",
						purpose, statusCode);
				logger.error(msg);
				throw new Exception(msg);
			} else {
				String location = response.getHeaders("Location")[0].getValue();
				logger.info("Ran " + purpose + " successfully - " + location);
			}
		} catch (ClientProtocolException e) {
			throw new Exception("FAILURE " + purpose + ": " + e.getMessage());
		} catch (IOException e) {
			throw new Exception("FAILURE " + purpose + ": " + e.getMessage());
		} finally {
			httpclient.close();
		}
	}

	public void updateItem(String uuid, String version, String newTitle) throws Exception {
		String url = baseUrl + "/api/item/"+uuid + "/" + version+"/?waitforindex=true";
		String purpose = "update item + atts with URL: " + url;

		CloseableHttpClient httpclient = HttpClients.createDefault();
		try {
			HttpPut http = new HttpPut(url);
			JSONObject item = this.getItem(uuid, version);
			String metadata = item.getString("metadata");
			item.put("metadata", metadata.replaceFirst("<title>.*</title>", "<title>"+newTitle+"</title>"));
			StringEntity input = new StringEntity(item.toString());
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
				String msg = String.format("FAILURE %s.  Unexpected http code:  %s",
						purpose, statusCode);
				logger.error(msg);
				throw new Exception(msg);
			} else {
				logger.info(purpose + " successfully.");
			}

//			if(!testUserUuid.equals(respStr)) {
//				String msg = String.format("FAILURE updating user.  Unexpected response of:  %s, expected %s",
//						respStr, testUserUuid);
//				logger.error(msg);
//				throw new Exception(msg);
//			}
		} catch (ClientProtocolException e) {
			throw new Exception("FAILURE "+purpose+": " + e.getMessage());
		} catch (IOException e) {
			throw new Exception("FAILURE "+purpose+": " + e.getMessage());
		} finally {
			httpclient.close();
		}
	}

}
