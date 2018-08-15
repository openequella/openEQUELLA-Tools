/*
 * Copyright 2018 Apereo
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

package org.apereo.openequella.tools.toolbox.utils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apereo.openequella.tools.toolbox.Config;

public class FileUtils {
	private static Logger LOGGER = LogManager.getLogger(FileUtils.class);

	public static boolean downloadWithProgress(File targetFile, String bestFitAttLink, String accessToken,
			int verboseLevel, Long expectedSize) {
		LOGGER.info("Downloading attachment to: [" + targetFile.getAbsolutePath() + "]");
		try {
			CloseableHttpClient httpclient = HttpClients.createDefault();
			HttpGet http = new HttpGet(bestFitAttLink);
			http.addHeader("X-Authorization", "access_token=" + accessToken);
			Long totalSize = -1L;
			try (CloseableHttpResponse httpResponse = httpclient.execute(http);
					FileOutputStream fos = new java.io.FileOutputStream(targetFile);
					BufferedOutputStream bout = new BufferedOutputStream(fos, 1024);) {
				HttpEntity entity = httpResponse.getEntity();
				if (entity != null) {
					InputStream eis = entity.getContent();
					BufferedInputStream beis = new BufferedInputStream(eis);
					totalSize = entity.getContentLength();
					Long readLength = 0L;
					int toggle = 0;
					byte[] chunk = new byte[1024];
					int currentLength = 0;
					while ((currentLength = beis.read(chunk, 0, 1024)) >= 0) {
						bout.write(chunk, 0, currentLength);
						readLength += currentLength;
						if (toggle++ > verboseLevel) {
							LOGGER.info("Downloaded (" + (readLength * 100 / totalSize) + "%) - " + readLength + "B / "
									+ totalSize + "B");
							toggle = 0;
						}
					}
					LOGGER.info(
							"Downloaded (" + readLength * 100 / totalSize + "%) - " + readLength + "B / " + totalSize + "B");
					bout.flush();
					bout.close();
					fos.flush();
					fos.close();
					beis.close();
					eis.close();

					// If the download method with progress doesn't work, this is a simple alternative (no progress) 
					// try (FileOutputStream outstream = new FileOutputStream(targetFile)) {
					//
					// entity.writeTo(outstream);
					// }
				}
			}

			// TODO might cause issues to do a blocking check, but we'll handle that if it occurs.
			if (totalSize != expectedSize) {
				LOGGER.info("Download complete.  Total filesize: [" + totalSize + "B], expected filesize: ["
						+ expectedSize + "B]");
				return true;
			} else {
				LOGGER.info("Download complete, but filesize from Equella [" + expectedSize
						+ "B] did not match download filesize [" + totalSize + "B]");
				return false;
			}

		} catch (MalformedURLException e) {
			LOGGER.info("Download failed: " + e.getMessage(), e);
		} catch (IOException e) {
			LOGGER.info("Download failed: " + e.getMessage(), e);
		}
		return false;
	}
	
	
	public static void removeFileAndParent(String parentname, String filename) {
		LOGGER.info("Removing downloaded file and parent directory: {}, {}", parentname, filename);
		if(removeFile(filename)) {
			removeFile(parentname);
		}
	}
	
	private static boolean removeFile(String filename) {
		File f = new File(filename);
		if(f.exists()) {
			if(f.delete()) {
				LOGGER.info("Deleted: {}", filename);
				return true;
			} else {
				LOGGER.warn("Unable to delete: {}", filename);
			}
		} else {
			LOGGER.warn("No file/directory exists at location: {}", filename);
		}
		return false;
	}
	
	
	/**
	 * Checks if the filename ends with a known suffix, and returns the suffix (in uppercase) 
	 * 
	 * Currently supports the Audio and Video suffix configs
	 * 
	 * @param filename
	 * @return the suffix in uppercase, null if not a known suffix.
	 * @throws Exception
	 */
	public static String extractSuffix(Config config, String filename) {
		List<String> audioSuffixes = config.getConfigAsList(Config.OEQ_SEARCH_ATT_SUFFIXES_AUDIO);
		for(String validSuffix : audioSuffixes) {
			if(filename.toUpperCase().endsWith(validSuffix.trim())) {
				return validSuffix.trim();
			}
		}
		
		List<String> videoSuffixes = config.getConfigAsList(Config.OEQ_SEARCH_ATT_SUFFIXES_VIDEO);
		for(String validSuffix : videoSuffixes) {
			if(filename.toUpperCase().endsWith(validSuffix.trim())) {
				return validSuffix.trim();
			}
		}	
		return null;
	}
}
