/*
 * Copyright 2019 Apereo
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

package org.apereo.openequella.tools.toolbox;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apereo.openequella.tools.toolbox.api.KalturaUtils;
import org.apereo.openequella.tools.toolbox.api.OpenEquellaRestUtils;
import org.apereo.openequella.tools.toolbox.utils.EquellaItem;

import com.kaltura.client.types.MediaEntry;
import com.kaltura.client.utils.response.OnCompletion;

public class MigrateItemsToKalturaDriver {
	private static Logger LOGGER = LogManager.getLogger(MigrateItemsToKalturaDriver.class);
	
private static List<EquellaItem> itemsToProcess;
	
	private static int totalNumOfItemsMigrated = 0;
	
	private static boolean capItemsToMigrate = false;
	private static int maxNumOfItemsToMigrate = 0;
	
	private static boolean kalturaUploadInProgress = false;
	
	
	public void execute(Config config) throws Exception {
		capItemsToMigrate = config.hasConfig(Config.GENERAL_MAX_ITEMS_TO_MIGRATE);
		
		if(capItemsToMigrate) {
			maxNumOfItemsToMigrate = config.getConfigAsInt(Config.GENERAL_MAX_ITEMS_TO_MIGRATE);
		}
		
		LOGGER.info("Starting openEQUELLA Migration run");
		OpenEquellaRestUtils oeru = new OpenEquellaRestUtils(config);
		if(!oeru.gatherAccessToken()) {
			LOGGER.info("Ending openEQUELLA migration run - no changes made.");
			return;
		}
		
		KalturaUtils ku = new KalturaUtils();
		if(!ku.obtainSession(config)) {
			LOGGER.info("Ending openEQUELLA migration run - no changes made.");
			return;
		}
		
		while(processNextInCache(ku, oeru)) {
			while (kalturaUploadInProgress) {
				LOGGER.info("Kaltura migration for a given item still in progress.  Waiting on further work.");
				Thread.sleep(10000);
			}
		}
	}
	
	private boolean cacheResourceBatch(OpenEquellaRestUtils oeru) {
		try {
			itemsToProcess = oeru.gatherItems();
			return true;
		} catch (Exception e) {
			LOGGER.error("Encountered a fatal error [{}].  Exiting...", e.getMessage());
			return false;
		}
	}
	
	private void processItem(KalturaUtils ku, OpenEquellaRestUtils oeru, EquellaItem eqResource) {
		kalturaUploadInProgress = true;
		if(oeru.downloadAttachmentForKaltura(eqResource)) {	
			// Add the video to Kaltura
			ku.addMedia(eqResource, new OnCompletion<MediaEntry>() {
				
				@Override
				public void onComplete(MediaEntry result) {
					// Confirm the video, and wait for it to finish processing.
					ku.confirmProcessedEntry(eqResource, true, new OnCompletion<MediaEntry>() {
						
						@Override
						public void onComplete(MediaEntry result) {
							oeru.newVersionEquellaResourceWithKalturaAttachment(result, eqResource);
							totalNumOfItemsMigrated++;
							String cap = "";
							if(capItemsToMigrate) {
								cap = ", capping migrated items at " + maxNumOfItemsToMigrate;
							}
							LOGGER.info("Migration stats: total # of items migrated = {}, capping # of items to migrate = {}{}", totalNumOfItemsMigrated, capItemsToMigrate, cap);
							kalturaUploadInProgress = false;
						}
					});			
				}
			});
		} else {
			//Unable to download attachment for Kaltura.  Moving on.
			kalturaUploadInProgress = false;
		}
	}
	
	private boolean underMigrationCap() {
		if(!capItemsToMigrate) {
			return true;
		}
		return totalNumOfItemsMigrated < maxNumOfItemsToMigrate;
	}
	
	
	/**
	 * 
	 * @param ku
	 * @param oeru
	 * @return true if there is more work to do, false if the migration should be done.
	 */
	private boolean processNextInCache(KalturaUtils ku, OpenEquellaRestUtils oeru) {
		if (underMigrationCap()) {	
			if((itemsToProcess == null) || (itemsToProcess.size() == 0)) {
				if(oeru.hasMoreResourcesToCache()) {
					if(itemsToProcess != null) LOGGER.info("Cached openEQUELLA resources reviewed.  Pulling next batch.");
					if(cacheResourceBatch(oeru)) {
						return processNextInCache(ku, oeru);
					} else {
						LOGGER.info("Something unexpected happened.  Ending migration run.");
						return false;
					}
				} else {
					LOGGER.info("All openEQUELLA resources reviewed.");
					LOGGER.info("Completed openEQUELLA Migration run");
					return false;
				}
			} else {
				processItem(ku, oeru, itemsToProcess.remove(0));
				return true;
			}
		} else {
			LOGGER.info("Migration cap hit ({}).", maxNumOfItemsToMigrate);
			LOGGER.info("Completed openEQUELLA Migration run");
			return false;
		}
	}
}
