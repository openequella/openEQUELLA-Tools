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
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apereo.openequella.tools.toolbox.api.OpenEquellaRestUtils;
import org.apereo.openequella.tools.toolbox.utils.EquellaItem;
import org.apereo.openequella.tools.toolbox.utils.sorts.SortOpenEquellaItemByName;
import org.json.JSONArray;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

public class ExportItemsDriver {
	private static Logger LOGGER = LogManager.getLogger(ExportItemsDriver.class);
	
	private Map<String, EquellaItem> cache = new HashMap<>();
	public void execute(Config config) {
		Long start = System.currentTimeMillis();
		// Ensure openEQUELLA is accessible
		OpenEquellaRestUtils oeru = new OpenEquellaRestUtils(config);
		if(!oeru.gatherAccessToken()) {
			LOGGER.error("Ending openEQUELLA run - unable to access {}", config.getConfig(Config.OEQ_URL));
			return;
		}
		
		LOGGER.info("Duration to obtain access token: {}ms",  (System.currentTimeMillis()-start));
		
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
					start = System.currentTimeMillis();
					
					List<EquellaItem> nextBatch = oeru.gatherItemsGeneral();
					LOGGER.info("Duration to obtain batch of items: {}ms",  (System.currentTimeMillis()-start));
					start = System.currentTimeMillis();
					
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
					LOGGER.info("Duration to cache batch of items: {}ms",  (System.currentTimeMillis()-start));
				} catch (Exception e) {
					LOGGER.error("Ending openEQUELLA run - error caching output file was not able to be created: {} - {}", output.getAbsolutePath(), e.getMessage());
					return;
				}		
			}
			LOGGER.info("All items gathered.  Printing out to CSV file [{}].", config.getConfig(Config.EXPORT_ITEMS_OUTPUT_FILE));
			
			start = System.currentTimeMillis();
			
			List<EquellaItem> records = new ArrayList<>(cache.values());
			Collections.sort(records, new SortOpenEquellaItemByName());
			LOGGER.info("Duration to prep cached items for printing: {}ms",  (System.currentTimeMillis()-start));
			start = System.currentTimeMillis();
			
			int counter = 1;
			for(EquellaItem ei : records) {
				List<String> record = buildRecord(headers, ei, config);
				LOGGER.debug("Duration to build csv record: {}ms - {}",  (System.currentTimeMillis()-start), ei.getSignature());
				start = System.currentTimeMillis();
				
				csvPrinter.printRecord(record);
				
				LOGGER.debug("Duration to print record: {}ms",  (System.currentTimeMillis()-start));
				start = System.currentTimeMillis();
				
				if(counter % 10 == 0) {
					LOGGER.debug("Printing status:  {} items complete.", counter);
				}
				counter++;
			}
			csvPrinter.flush();
			
			csvPrinter.flush();
			csvPrinter.close();
			LOGGER.info("Duration to flush printer: {}ms",  (System.currentTimeMillis()-start));
			
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
	
	// While MigrationUtils.findFirstOccurrenceInXml() is similar, this combines the 'reserved
	// keywords' with a single invocation of parsing the XML.
	public List<String> buildRecord(List<String> headers, EquellaItem ei, Config config) throws Exception {
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
		Document doc = dBuilder.parse(new InputSource(new StringReader(ei.getMetadata())));
		XPathFactory xPathfactory = XPathFactory.newInstance();
		XPath xpath = xPathfactory.newXPath();
		
		List<String> record = new ArrayList<>();
		for(String header : headers) {
			if(header.equalsIgnoreCase("uuid")) {
				record.add(ei.getUuid());
			} else if(header.equalsIgnoreCase("version")) {
				record.add(ei.getVersion()+"");
			} else if(header.equalsIgnoreCase("attachment_names")) {
				record.add(parseAttachmentFilenames(ei, config));
			} else if(header.equalsIgnoreCase("name")) {
				record.add(ei.getName());
			} else if(header.equalsIgnoreCase("description")) {
				record.add(ei.getDescription());
			} else if(header.equalsIgnoreCase("kaltura_id")) {
				record.add(findFirstKalturaIdInAttachments(ei));
			} else {
				//Assume it's a metadata path
				try {
					XPathExpression expr = xpath.compile("/xml/" + header);
					List<String> results = new ArrayList<>();
					Object xmlResults = expr.evaluate(doc, XPathConstants.NODESET);
					NodeList nodeResults = (NodeList) xmlResults;
					for(int i = 0; i < nodeResults.getLength(); i++) {
						results.add(nodeResults.item(i).getTextContent());
					}
					record.add(String.join(",", results)); 
				} catch (Exception e) {
					e.printStackTrace();
					throw new Exception("Unable to parse column format xpath "+header); 
				}
			}
		}
		return record;
	}

	public String parseAttachmentFilenames(EquellaItem ei, Config config) {
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
					sb.append(constructAttachmentPath(ei, att.getString(OpenEquellaRestUtils.KEY_ATT_FILENAME), config));
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
	
	public String findFirstKalturaIdInAttachments(EquellaItem ei) {
		JSONObject json = ei.getJson();
		if(json.has(OpenEquellaRestUtils.KEY_ATTS)) {
			JSONArray atts = json.getJSONArray(OpenEquellaRestUtils.KEY_ATTS);
			for(int i = 0; i < atts.length(); i++) {
				JSONObject att = atts.getJSONObject(i);
				if(att.getString(OpenEquellaRestUtils.KEY_ATT_TYPE).equals(OpenEquellaRestUtils.VAL_ATT_TYPE_KALTURA)) {
					return att.getString(OpenEquellaRestUtils.KEY_ATT_MEDIA_ID);
				}
			}
		} else {
			return "";
		}
		return "";
	}

	private List<String> parseCSV(String csv) {
		List<String> tokens = new ArrayList<>();
		String[] rawTokens = csv.split(",");
		for(String rawToken : rawTokens) {
			tokens.add(rawToken.trim());
		}
		return tokens;
	}
	
	public String constructAttachmentPath(EquellaItem ei, String attName, Config config) {
		String template = config.getConfig(Config.EXPORT_ITEMS_ATTACHMENT_PATH_TEMPLATE);
		if(template.contains("@HASH")) {
			template = template.replaceFirst("@HASH", (ei.getUuid().hashCode() & 127)+"");
		}
				
		if(template.contains("@UUID")) {
			template = template.replaceFirst("@UUID", ei.getUuid());
		}
		
		if(template.contains("@VERSION")) {
			template = template.replaceFirst("@VERSION", (ei.getVersion())+"");
		}
		
		if(template.contains("@FILENAME")) {
			template = template.replaceFirst("@FILENAME", attName);
		}
		
		return template;
	}
	
}
