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

package org.apereo.openequella.tools.toolbox.api;

import java.io.File;
import java.util.UUID;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apereo.openequella.tools.toolbox.Config;
import org.apereo.openequella.tools.toolbox.utils.EquellaItem;

import com.kaltura.client.APIOkRequestsExecutor;
import com.kaltura.client.Client;
import com.kaltura.client.Configuration;
import com.kaltura.client.enums.EntryStatus;
import com.kaltura.client.enums.MediaType;
import com.kaltura.client.enums.SessionType;
import com.kaltura.client.services.MediaService;
import com.kaltura.client.services.MediaService.AddContentMediaBuilder;
import com.kaltura.client.services.MediaService.AddMediaBuilder;
import com.kaltura.client.services.MediaService.GetMediaBuilder;
import com.kaltura.client.services.UploadTokenService;
import com.kaltura.client.services.UploadTokenService.AddUploadTokenBuilder;
import com.kaltura.client.services.UploadTokenService.UploadUploadTokenBuilder;
import com.kaltura.client.types.MediaEntry;
import com.kaltura.client.types.UploadToken;
import com.kaltura.client.types.UploadedFileTokenResource;
import com.kaltura.client.utils.request.RequestElement;
import com.kaltura.client.utils.response.OnCompletion;
import com.kaltura.client.utils.response.base.Response;

public class KalturaUtils {
	protected static Logger LOGGER = LogManager.getLogger(KalturaUtils.class);
	protected static int KALTURA_SESSION_EXPIRY_IN_SECS = 86400;
	protected Configuration kalturaConfig = new Configuration();
	protected Client client;
	private Config appConfig;
	private int confirmationCounter = 0;
	
	
	public boolean obtainSession(Config c) {
		try {
			appConfig = c;
			this.kalturaConfig.setEndpoint(appConfig.getConfig(Config.KAL_SERVICE_URL));
			this.client = new Client(this.kalturaConfig);
			this.client.setSessionId(this.client.generateSessionV2(
					appConfig.getConfig(Config.KAL_ADMIN_SECRET), 
					appConfig.getConfig(Config.KAL_USER_ID), 
					SessionType.USER, 
					Integer.parseInt(appConfig.getConfig(Config.KAL_PARTNER_ID)), 
					KALTURA_SESSION_EXPIRY_IN_SECS, ""));
			LOGGER.info("Kaltura session ID gathered: " + this.client.getSessionId());
			return true;
		} catch (Exception e) {
			LOGGER.error("FAILURE:  Request for Kaltura session ID failed with: {}", e.getMessage());
			LOGGER.error(e);
			return false;
		}	
	}
	
	public void addVideo(EquellaItem eItem, final OnCompletion<MediaEntry> onCompletion)
	{
		LOGGER.info("{} - Processing resource:  ", eItem.getSignature());
		MediaEntry entry = new MediaEntry();
		entry.setName(eItem.getName());
		entry.setDescription(eItem.getDescription());
		entry.setTags(eItem.getKalturaTags());
		entry.setCategories(appConfig.getConfig(Config.KAL_CATEGORIES));
		entry.setMediaType(MediaType.VIDEO);
		entry.setReferenceId(getUniqueString());
		
		File fileToUpload = new File(eItem.getFilepath());
		
		final long fileSize = fileToUpload.length();
		final String fileName = fileToUpload.getName();
		
		AddMediaBuilder requestBuilder = MediaService.add(entry)
		.setCompletion(new OnCompletion<Response<MediaEntry>>() {

			@Override
			public void onComplete(Response<MediaEntry> result) {
				if(result.error != null) {
					LOGGER.info("ERROR from Kaltura MediaService.add:  " + result.error.getMessage());
					result.error.printStackTrace();
					return;
				}
				final MediaEntry entry = result.results;
				
				// Upload token
				UploadToken uploadToken = new UploadToken();
				uploadToken.setFileName(fileName);
				uploadToken.setFileSize((double) fileSize);
				AddUploadTokenBuilder requestBuilder = UploadTokenService.add(uploadToken)
				.setCompletion(new OnCompletion<Response<UploadToken>>() {

					@Override
					public void onComplete(Response<UploadToken> result) {
						if(result.error != null) {
							LOGGER.info("ERROR from Kaltura UploadTokenService.add:  " + result.error.getMessage());
							result.error.printStackTrace();
							return;
						}
						
						final UploadToken token = result.results;
						if(token == null) {
							LOGGER.info("ERROR UploadToken is null.");
							return;
						}
						
						// Define content
						UploadedFileTokenResource resource = new UploadedFileTokenResource();
						resource.setToken(token.getId());
						AddContentMediaBuilder requestBuilder = MediaService.addContent(entry.getId(), resource)
						.setCompletion(new OnCompletion<Response<MediaEntry>>() {

							@Override
							public void onComplete(Response<MediaEntry> result) {
								if(result.error != null) {
									LOGGER.info("ERROR from Kaltura MediaService.addContent:  " + result.error.getMessage());
									result.error.printStackTrace();
									return;
								}
								
//								if(entry == null) {
//									LOGGER.info("ERROR entry is null.");
//									return;
//								}
								
								// upload
								UploadUploadTokenBuilder requestBuilder = UploadTokenService.upload(token.getId(), fileToUpload, false)
								.setCompletion(new OnCompletion<Response<UploadToken>>() {

									@Override
									public void onComplete(Response<UploadToken> result) {
										if(result.error != null) {
											LOGGER.info("ERROR from Kaltura UploadTokenService.upload:  " + result.error.getMessage());
											result.error.printStackTrace();
											return;
										}
										
//										if(token == null) {
//											LOGGER.info("ERROR upload token is null.");
//											return;
//										}
												
										MediaService.get(entry.getId());
										
										// TODO - would be good to verify details of the Kaltura entry here.
										eItem.setKalturaMediaId(entry.getId());
										LOGGER.info("{} - Finished uploading resource's content object to Kaltura.  Media ID in Kaltura:  {}.", eItem.getSignature(), entry.getId());
										onCompletion.onComplete(entry);
									}
								});
								APIOkRequestsExecutor.getExecutor().queue(requestBuilder.build(client));
							}
						});
						APIOkRequestsExecutor.getExecutor().queue(requestBuilder.build(client));
					}
				});
				APIOkRequestsExecutor.getExecutor().queue(requestBuilder.build(client));
			}
		});
		APIOkRequestsExecutor.getExecutor().queue(requestBuilder.build(client));
	}
	
	public void confirmProcessedEntry(final EquellaItem item, final Boolean checkReady, final OnCompletion<MediaEntry> onCompletion) {
		final int maxTries = 50;
		final int sleepInterval = 30 * 1000;
		final String kId = item.getKalturaMediaId();
		
		LOGGER.info("{} - Confirming openEQUELLA resource content object in Kaltura:  ", item.getSignature(), kId);
		
		confirmationCounter = 0;

		GetMediaBuilder requestBuilder = MediaService.get(kId);
		final RequestElement<MediaEntry> requestElement = requestBuilder.build(client);

		requestBuilder.setCompletion(new OnCompletion<Response<MediaEntry>>() {
			
			@Override
			public void onComplete(Response<MediaEntry> result) {
				if(result.error != null) {
					LOGGER.info("{} - ERROR from Kaltura MediaService.get ({}):  {}", item.getSignature(), kId, result.error.getMessage());
					result.error.printStackTrace();
					return;
				}
				MediaEntry retrievedEntry = result.results;
				
				if(checkReady && retrievedEntry.getStatus() != EntryStatus.READY) {

					confirmationCounter++;

					if (confirmationCounter >= maxTries) {
						throw new RuntimeException(String.format("%s - Hit max retries (%s) when retrieving entry: %s", item.getSignature(), maxTries, kId));
					} else {
						LOGGER.info("{} - On try: {}, clip not ready (status = {}. waiting {} seconds...", confirmationCounter, item.getSignature(), retrievedEntry.getStatus().getValue(), (sleepInterval / 1000));
						try {
							Thread.sleep(sleepInterval);
						} catch (InterruptedException ie) {
							throw new RuntimeException("Failed while waiting for entry processing.");
						}
					}

					APIOkRequestsExecutor.getExecutor().queue(requestElement);
				}
				else {
					LOGGER.info("{} - Confirmed openEQUELLA resource content object in Kaltura looks good.  Kaltura Media ID:  {}", item.getSignature(), kId);
					onCompletion.onComplete(retrievedEntry);
				}
			}
		});
		
		APIOkRequestsExecutor.getExecutor().queue(requestElement);
	}
	
	private String getUniqueString() {
		return UUID.randomUUID().toString();
	}
}
