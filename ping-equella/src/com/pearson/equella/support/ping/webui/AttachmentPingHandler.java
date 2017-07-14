package com.pearson.equella.support.ping.webui;

import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.http.Header;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import com.pearson.equella.support.ping.direct.ResultsRow;
import com.pearson.equella.support.ping.report.ReportManager;
import com.pearson.equella.support.ping.utils.Config;

public class AttachmentPingHandler {
	public static int TIMEOUT_MS = 1;

	private static final Logger logger = LogManager
			.getLogger(AttachmentPingHandler.class);

	String accessToken = "";

	public boolean execute() {
		accessToken = findAccessToken();
		if (ReportManager.getInstance().hasFatalErrors())
			return false;
		logger.debug("Found access token [{}].  Beginning search...",
				accessToken);

		if (!ReportManager.getInstance().setupReportDetails())
			return false;

		pullItemAttachments();
		if (ReportManager.getInstance().hasFatalErrors())
			return false;

		logger.info("Search completed.");
		return true;
	}

	private void pullItemAttachments() {
		int start = 0;
		int length = 10;
		boolean moreAvailable = true;
		while (moreAvailable && !ReportManager.getInstance().hasFatalErrors()) {
			moreAvailable = pullBlockOfItemXmls(start, length);
			start = start + length;
		}
		if (ReportManager.getInstance().hasFatalErrors()) {
			String msg = "Unable to continue pulling items and attachments to review.";
			logger.error(msg);
			ReportManager.getInstance().addFatalError(msg);
		}
	}

	private void processAttachments(JSONArray results, int searchStart,
			int searchLength, int searchAvailable) {
		logger.info("Result stats: [{}-{}]/[{}].  Pinging attachments...",
				searchStart, searchLength, searchAvailable);

		for (int i = 0; i < results.length(); i++) {
			ReportManager.getInstance().getStats().incNumTotalItems();
			String pre = String.format("[%d-%d]/[%d]:", (searchStart + i + 1),
					searchLength, searchAvailable);
			ResultsRow itemRow = new ResultsRow();
			itemRow.setItemUuid(results.getJSONObject(i).getString("uuid"));
			itemRow.setItemVersion(""
					+ results.getJSONObject(i).getInt("version"));
			if (results.getJSONObject(i).has("name")) {
				itemRow.setItemName(results.getJSONObject(i).getString("name"));
			}
			itemRow.setItemStatus(results.getJSONObject(i).getString("status"));
			itemRow.setCollectionUuid(results.getJSONObject(i)
					.getJSONObject("collection").getString("uuid"));
			itemRow.setInstitutionShortname(Config.getInstance().getAttachmentsInstitutionShortname());
			logger.trace("{}Item URL: {}", pre, results.getJSONObject(i)
					.getJSONObject("links").getString("view"));

			JSONArray atts = results.getJSONObject(i).getJSONArray(
					"attachments");
			logger.trace("{}  Num of Attachments: {}", pre, atts.length());
			if (atts.length() == 0) {
				itemRow.setAttStatus(ResultsRow.NOATT);
				try {
					ReportManager.getInstance().stdOutWriteln(itemRow.toString());
				} catch (IOException e) {
					String msg = String.format("Error writing the reports - %s.", e.getMessage());
					logger.fatal(msg, e);
					ReportManager.getInstance().addFatalError(msg);
				}
			}
			long checkpointMissingAtts = ReportManager.getInstance().getStats()
					.getNumTotalAttachmentsMissing();
			for (int j = 0; (j < atts.length()) && !ReportManager.getInstance().hasFatalErrors(); j++) {
				ResultsRow attRow = ResultsRow.buildItemFrame(itemRow);
				attRow.setAttType(atts.getJSONObject(j).getString("type"));
				attRow.setAttUrl(atts.getJSONObject(j).getJSONObject("links")
						.getString("view"));
				int breakpoint = attRow.getAttUrl().indexOf("attachment.uuid=");
				attRow.setAttUuid(attRow.getAttUrl().substring(breakpoint + 16));
				ReportManager.getInstance().getStats().incNumTotalAttachments();
				if (attRow.getAttType().equals("file")
						|| attRow.getAttType().equals("htmlpage")) {
					attRow.setAttFilePath(atts.getJSONObject(j).getString(
							"filename"));
					logger.trace("{}  File attachment filename: {}, type: {}",
							pre, attRow.getAttFilePath(), attRow.getAttType());
					pingFile(pre, attRow);
				} else if (attRow.getAttType().equals("zip")) {
					attRow.setAttFilePath(atts.getJSONObject(j).getString(
							"folder"));
					logger.trace("{}  Zip attachment filename: {}, type: {}",
							pre, attRow.getAttFilePath(), attRow.getAttType());
					pingFile(pre, attRow);
				} else {
					attRow.setAttStatus(ResultsRow.IGNORED);
					ReportManager.getInstance().getStats()
							.incNumTotalAttachmentsIgnored();
					logger.trace("{}  Other attachment type: {}", pre,
							attRow.getAttType());
					try {
						ReportManager.getInstance()
								.stdOutWriteln(attRow.toString());
					} catch (IOException e) {
						String msg = String.format("Error writing the reports - %s.", e.getMessage());
						logger.fatal(msg, e);
						ReportManager.getInstance().addFatalError(msg);
					}
				}
			}
			
			// See if there were any missing attachments for this item.
			if (checkpointMissingAtts != ReportManager.getInstance().getStats()
					.getNumTotalAttachmentsMissing()) {
				ReportManager.getInstance().getStats()
						.incNumTotalItemsAffected();
			}
		}
	}

	/**
	 * 
	 * @param accessToken
	 * @param start
	 * @param length
	 * @return true if there are more items available to ping
	 */
	private boolean pullBlockOfItemXmls(int start, int length) {
		int searchStart = -1;
		int searchLength = -1;
		int searchAvailable = -1;
		JSONArray results = null;

		// Build the URL
		String coll = "";
		if (!Config.getInstance().getFilterByCollectionForAttachments().isEmpty()) {
			coll = String.format("&collections=%s", Config.getInstance()
					.getFilterByCollectionForAttachments());
		}
		String url = String
				.format("%s/api/search?start=%d&length=%d&order=name&reverse=false&info=all&showall=true%s",
						Config.getInstance().getInstitutionUrl(), start,
						length, coll);
		logger.info("Making call: [{}]", url);

		loopForSearch: for (int tries = 1; (tries <= Config.getInstance()
				.getMaxTries()) && (results == null); tries++) {
			// Placeholders
			CloseableHttpClient httpclient = null;
			CloseableHttpResponse response = null;
			searchItems: {
				httpclient = HttpClients.createDefault();

				HttpGet httpget = new HttpGet(url);
				// For testing - inject a timeout for certain amount of
				// times.
				injectTimeout(httpget, tries);

				// Execute HTTP request
				httpget.addHeader("X-Authorization",
						String.format("access_token=%s", accessToken));
				try {
					response = httpclient.execute(httpget);
				} catch (Exception e) {
					logger.error(
							"Unable to process GET request for item search: {}",
							e.getMessage(), e);
					break searchItems;
				}
				traceHeaders(response, "");

				if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
					// The HTTP response code wasn't a 200 - try again.
					logger.warn(
							"Search REST API failed start=[{}], length=[{}], status=[{}].  Retrying...",
							start, length, response.getStatusLine()
									.getStatusCode());
					break searchItems;
				}

				// Parse response data
				JSONObject jresponse = null;
				try {
					jresponse = new JSONObject(new JSONTokener(
							new InputStreamReader(response.getEntity()
									.getContent())));
					logger.trace("Query response: {}", jresponse.toString(2));
				} catch (Exception e) {
					logger.error(
							"Error reading the response data - {}. Retrying...",
							e.getMessage(), e);
					break searchItems;
				}

				searchStart = jresponse.getInt("start");
				searchLength = jresponse.getInt("length");
				searchAvailable = jresponse.getInt("available");
				results = jresponse.getJSONArray("results");
			} // end searchItems
			closeOutCurrentNetworkTry(httpclient, response);
			if (!conditionalSleep(results == null, tries))
				break loopForSearch;
		} // end loopForSearch

		if (results != null) {
			processAttachments(results, searchStart, searchLength,
					searchAvailable);
		}
		return (searchStart + searchLength) < searchAvailable;
	}

	/**
	 * 
	 * @param pre
	 * @param accessToken
	 * @param attRow
	 * @throws ClientProtocolException
	 * @throws IOException
	 * @throws InterruptedException
	 */
	private void pingFile(String pre, ResultsRow attRow) {
		int statusCode = Integer.MIN_VALUE;
		logger.debug("{} Calling : {}", pre, attRow.getAttUrl());

		loopForFile: for (int tries = 1; (tries <= Config.getInstance()
				.getMaxTries()) && (statusCode == Integer.MIN_VALUE); tries++) {
			// Placeholders
			CloseableHttpClient httpclient = null;
			CloseableHttpResponse response = null;
			pingFile: {
				try {
					Thread.sleep(10);
				} catch (InterruptedException e) {
					// Fail completely.
					String msg = String.format(
							"Unable to sleep for a brief time - %s",
							e.getMessage());
					logger.error(msg, e);
					ReportManager.getInstance().addFatalError(msg);
					break loopForFile;
				}
				httpclient = HttpClients.createDefault();
				HttpGet httpget = new HttpGet(attRow.getAttUrl());

				// For testing - inject a timeout for certain amount of times.
				injectTimeout(httpget, tries);

				// Execute HTTP request
				logger.trace("{}executing request {}", pre, httpget.getURI());
				// httpget.addHeader("Cache-Control", "no-cache");
				httpget.addHeader("X-Authorization",
						String.format("access_token=%s", accessToken));
				try {
					response = httpclient.execute(httpget);
				} catch (Exception e) {
					logger.error(
							"Unable to process GET request for file ping: {}",
							e.getMessage(), e);
					break pingFile;
				}

				traceHeaders(response, pre);
				statusCode = response.getStatusLine().getStatusCode();
				// At this point, you're done looping since the statusCode is
				// set.
				attRow.setAttRespCode("" + statusCode);
				logger.info("Ping file {} response code: {}", attRow.getAttFilePath(), statusCode);
				try {
					if (statusCode == HttpStatus.SC_OK) {
						attRow.setAttStatus(ResultsRow.PRESENT);
						ReportManager.getInstance().stdOutWriteln(
								attRow.toString());
					} else {
						ReportManager.getInstance().getStats()
								.incNumTotalAttachmentsMissing();
						if (statusCode == HttpStatus.SC_NOT_FOUND) {
							attRow.setAttStatus(ResultsRow.MISSING);
							ReportManager.getInstance().dualWriteLn(
									attRow.toString());
						} else {
							attRow.setAttStatus(ResultsRow.UNKNOWN);
							ReportManager.getInstance().dualWriteLn(
									attRow.toString());
						}
					}
				} catch (IOException e) {
					String msg = String
							.format("Unrecoverable error while reporting attachment - {}",
									e.getMessage());
					logger.fatal(msg, e);
					ReportManager.getInstance().addFatalError(msg);
					break pingFile;
				}
			} // end pingFile
			closeOutCurrentNetworkTry(httpclient, response);
			if (!conditionalSleep((statusCode == Integer.MIN_VALUE), tries))
				break loopForFile;
		} // end loopForFile

		if (statusCode == Integer.MIN_VALUE) {
			String msg = "Failed due to too many errors.";
			logger.error(msg);
			ReportManager.getInstance().addFatalError(msg);
		}
	}

	/**
	 * Will retry to find the access token for a max of config>maxTries.
	 * 
	 * @return the accessToken for Equella, null if there were too many errors.
	 */
	public String findAccessToken() {
		String accessToken = null;
		String url = Config.getInstance().getInstitutionUrl()
				+ "/oauth/access_token?grant_type=client_credentials&client_id="
				+ Config.getInstance().getClientId() + "&client_secret="
				+ Config.getInstance().getClientSecret()
				+ "&redirect_uri=default";
		logger.info("Finding the access token using [{}]", url);

		loopForToken: for (int tries = 1; (tries <= Config.getInstance()
				.getMaxTries()) && (accessToken == null); tries++) {
			// Placeholders
			CloseableHttpClient httpclient = null;
			CloseableHttpResponse response = null;
			int statusCode = Integer.MIN_VALUE;
			findToken: {
				httpclient = HttpClients.createDefault();
				HttpGet httpget = new HttpGet(url);

				// For testing - inject a timeout for certain amount of times.
				injectTimeout(httpget, tries);

				// Execute HTTP request
				try {
					response = httpclient.execute(httpget);
				} catch (Exception e) {
					logger.error(
							"Unable to process GET request for access token: {}",
							e.getMessage(), e);
					break findToken;
				}

				traceHeaders(response, "Looking for access token header: ");

				// Retrieve status code
				statusCode = response.getStatusLine().getStatusCode();

				// Parse response data
				JSONObject jresponse = null;
				try {
					jresponse = new JSONObject(new JSONTokener(
							new InputStreamReader(response.getEntity()
									.getContent())));
				} catch (Exception e) {
					logger.error("Error reading the response data - {}.",
							e.getMessage(), e);
					break findToken;
				}

				// Confirm response
				if (statusCode == HttpStatus.SC_OK) {
					logger.trace("Query response: {}", jresponse.toString(2));
					if (!jresponse.has("access_token")) {
						// If EQUELLA gives a 200, but fails to provide a token,
						// completely fail.
						logger.error("Request for access_token didn't return expected JSON key");
						break loopForToken;
					}
					accessToken = jresponse.getString("access_token");

				} else {
					logger.error("Request for access_token failed with a [{}]",
							statusCode);
					break findToken;
				}
			} // end findToken

			closeOutCurrentNetworkTry(httpclient, response);
			if (!conditionalSleep((accessToken == null), tries))
				break loopForToken;
		}// end loopForToken
		if (accessToken == null) {
			String msg = "Failed due to too many errors.";
			logger.error(msg);
			ReportManager.getInstance().addFatalError(msg);
		}

		return accessToken;
	}

	private void closeOutCurrentNetworkTry(CloseableHttpClient httpclient,
			CloseableHttpResponse response) {
		// Close out current try for access token.
		if (response != null) {
			try {
				response.close();
			} catch (IOException e) {
				logger.error("Unable to close the response", e);
			}
		}
		if (httpclient != null) {
			try {
				httpclient.close();
			} catch (IOException e) {
				logger.error("Unable to close the httpclient", e);
			}
		}
	}

	private void traceHeaders(CloseableHttpResponse response, String pre) {
		if (logger.isTraceEnabled()) {
			logger.trace("Headers:");
			Header[] hdrs = response.getAllHeaders();
			for (int i = 0; i < hdrs.length; i++) {
				logger.trace("{}{}:::{}", pre, hdrs[i].getName(),
						hdrs[i].getValue());
			}
			logger.trace(response.getFirstHeader("Content-Type").getValue());
		}
	}

	private boolean conditionalSleep(boolean cond, int tries) {
		if (cond) {
			long sleep = (tries * 1000);
			logger.warn(
					"Unable to handle network request.  Sleeping for {} ms and retrying.",
					sleep);
			try {
				Thread.sleep(sleep);
			} catch (InterruptedException e) {
				// Fail completely.
				logger.error("Unable to sleep for the specified time - {}",
						e.getMessage(), e);
				return false;
			}
		}
		return true;
	}

	public void injectTimeout(HttpGet httpget, int tries) {
		if (tries <= Config.getInstance().getTestTimesToInjectTimeout()) {
			RequestConfig requestConfig = RequestConfig.custom()
					.setSocketTimeout(TIMEOUT_MS).setConnectTimeout(TIMEOUT_MS)
					.setConnectionRequestTimeout(TIMEOUT_MS).build();
			httpget.setConfig(requestConfig);
		}
	}
}
