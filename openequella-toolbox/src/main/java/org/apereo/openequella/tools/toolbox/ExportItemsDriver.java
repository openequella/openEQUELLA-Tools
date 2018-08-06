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

package org.apereo.openequella.tools.toolbox;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apereo.openequella.tools.toolbox.api.OpenEquellaRestUtils;
import org.apereo.openequella.tools.toolbox.utils.EquellaItem;
import org.apereo.openequella.tools.toolbox.utils.MigrationUtils;
import org.apereo.openequella.tools.toolbox.utils.sorts.SortOpenEquellaItemByName;
import org.json.JSONArray;
import org.json.JSONObject;

public class ExportItemsDriver {
	private static Logger LOGGER = LogManager.getLogger(ExportItemsDriver.class);
	
	private Map<String, EquellaItem> cache = new HashMap<>();
	public void execute(Config config) {
		// Ensure openEQUELLA is accessible
		OpenEquellaRestUtils oeru = new OpenEquellaRestUtils(config);
		if(!oeru.gatherAccessToken()) {
			LOGGER.error("Ending openEQUELLA run - unable to access {}", config.getConfig(Config.OEQ_URL));
			return;
		}
		
		// Create output file
		File output = new File(config.getConfig(Config.EXPORT_ITEMS_OUTPUT_FILE));
		if(output.exists()) {
			LOGGER.error("Ending openEQUELLA run - output file already exists: {}", output.getAbsolutePath());
			return;
		}
		CSVPrinter csvPrinter = null;
		try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(config.getConfig(Config.EXPORT_ITEMS_OUTPUT_FILE)))) {
			LOGGER.info("Using output file {} for export", output.getAbsolutePath());
			
			//Setup CSV writer and 'heading'
			csvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT);
			List<String> headers = parseCSV(config.getConfig(Config.EXPORT_ITEMS_COLUMN_FORMAT));
			csvPrinter.printRecord(headers);
			
			// Setup filter by date created (optional)
			Date filterByDateCreated = null;
			if(config.hasConfig(Config.EXPORT_ITEMS_FILTER_DATE_CREATED)) {
				filterByDateCreated = Config.DATE_FORMAT_CONFIG_FILE.parse(config.getConfig(Config.EXPORT_ITEMS_FILTER_DATE_CREATED));
			}
			
			// Loop through search results and save to the output file
			while(oeru.hasMoreResourcesToCache()) {
				try {
					List<EquellaItem> nextBatch = oeru.gatherItemsGeneral();
					for(EquellaItem ei : nextBatch) {
						// filter by date created if the filter was specified
						if(filterByDateCreated == null || !ei.getCreatedDate().before(filterByDateCreated)) {
							if(cache.containsKey(ei.getUuid())) {
								// Ensure the latest live version is exported
								if(cache.get(ei.getUuid()).getVersion() < ei.getVersion()) {
									LOGGER.debug("{} - Filtering out since there is a later LIVE version {}.", cache.get(ei.getUuid()).getSignature(), ei.getSignature());
									cache.put(ei.getUuid(), ei);
								}
							} else {
								cache.put(ei.getUuid(), ei);
							}
						} else {
							LOGGER.debug("{} - Filtering out since it's dateCreated ({}) is before the specified date {}", ei.getSignature(), Config.DATE_FORMAT_OEQ_API.format(ei.getCreatedDate()), config.getConfig(Config.EXPORT_ITEMS_FILTER_DATE_CREATED));
						}
					}
				} catch (Exception e) {
					LOGGER.error("Ending openEQUELLA run - error caching output file was not able to be created: {} - {}", output.getAbsolutePath(), e.getMessage());
					return;
				}		
			}
			LOGGER.info("All items gathered.  Printing out to CSV file [{}].", config.getConfig(Config.EXPORT_ITEMS_OUTPUT_FILE));
			List<EquellaItem> records = new ArrayList<>(cache.values());
			Collections.sort(records, new SortOpenEquellaItemByName());
			int counter = 0;
			for(EquellaItem ei : records) {
				csvPrinter.printRecord(buildRecord(headers, ei));
				if(counter++ % 10 == 0) {
					LOGGER.info("Printing status:  {} items complete.", counter);
				}
			}
			csvPrinter.flush();
			
			csvPrinter.flush();
			csvPrinter.close();
			if(counter % 10 != 0) {
				LOGGER.info("Printing status:  {} items complete.", counter);
			}
			LOGGER.info("Export complete.");
			
		} catch (Exception e) {
			if(csvPrinter != null) {
				try {
					csvPrinter.flush();
					csvPrinter.close();
				} catch (IOException e1) {
					LOGGER.error("Issue forcing a close of the CSV Printer - {}", e.getMessage());
				}
			}
			LOGGER.error("Ending openEQUELLA run - {}", e.getMessage());
			return;
		}
	}
	
	private List<String> buildRecord(List<String> headers, EquellaItem ei) throws Exception {
		List<String> record = new ArrayList<>();
		for(String header : headers) {
			if(header.equalsIgnoreCase("uuid")) {
				record.add(ei.getUuid());
			} else if(header.equalsIgnoreCase("version")) {
				record.add(ei.getVersion()+"");
			} else if(header.equalsIgnoreCase("attachment_names")) {
				record.add(parseAttachmentFilenames(ei));
			} else if(header.equalsIgnoreCase("name")) {
				record.add(ei.getName());
			} else if(header.equalsIgnoreCase("description")) {
				record.add(ei.getDescription());
			} else {
				//Assume it's a metadata path
				try {
					record.add(MigrationUtils.findFirstOccurrenceInXml(ei.getMetadata(), "/xml/" + header + "/text()"));
				} catch (Exception e) {
					e.printStackTrace();
					throw new Exception("Unable to parse column format xpath "+header); 
				}
			}
		}
		return record;
	}

	public String parseAttachmentFilenames(EquellaItem ei) {
		JSONObject json = ei.getJson();
		if(json.has(OpenEquellaRestUtils.KEY_ATTS)) {
			StringBuilder sb = new StringBuilder();
			
			JSONArray atts = json.getJSONArray(OpenEquellaRestUtils.KEY_ATTS);
			for(int i = 0; i < atts.length(); i++) {
				JSONObject att = atts.getJSONObject(i);
				switch (att.getString(OpenEquellaRestUtils.KEY_ATT_TYPE)) {
				case OpenEquellaRestUtils.VAL_ATT_TYPE_FILE: {
					if(sb.length() != 0) {
						sb.append(",");
					}
					sb.append(att.getString(OpenEquellaRestUtils.KEY_ATT_FILENAME));
					break;
				}
				case OpenEquellaRestUtils.VAL_ATT_TYPE_KALTURA: {
					if(sb.length() != 0) {
						sb.append(",");
					}
					sb.append(att.getString(OpenEquellaRestUtils.KEY_ATT_TITLE));
					break;
				}
				default: {
					//Ignore other attachment types
				}
				}
			}
			
			return sb.toString();
		} else {
			return "";
		}
	}

	private List<String> parseCSV(String csv) {
		List<String> tokens = new ArrayList<>();
		String[] rawTokens = csv.split(",");
		for(String rawToken : rawTokens) {
			tokens.add(rawToken.trim());
		}
		return tokens;
	}
	
	
}
