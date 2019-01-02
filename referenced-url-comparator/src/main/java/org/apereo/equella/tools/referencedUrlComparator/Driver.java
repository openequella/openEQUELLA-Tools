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

package org.apereo.equella.tools.referencedUrlComparator;

import java.io.FileReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Driver {

	private Logger logger = LogManager.getLogger(Driver.class);

	private Map<String, List<RefUrl>> urlsFirst = new HashMap<>();
	private Map<String, List<RefUrl>> urlsSecond = new HashMap<>();
	
	private String base;
	private String firstFile;
	private String secondFile;

	public static void main(String[] args) throws Exception {
		Driver d = new Driver("/my/base/dir", "first.csv",
				"second.csv");
		d.execute();
	}
	
	public Driver(String b, String fn1, String fn2) {
		this.base = b;
		this.firstFile = fn1;
		this.secondFile = fn2;
	}

	public void execute() throws Exception {
		parse(urlsFirst, firstFile);
    	parse(urlsSecond, secondFile);
    	reportOnDuplicates(urlsFirst, firstFile, urlsSecond, secondFile);
    	reportOnComparison(urlsFirst,firstFile,urlsSecond, secondFile);
    }

	/**
	 * Assumes map1 and map2 only have single entry lists (ie no duplicates)
	 * @param map1
	 * @param file1
	 * @param map2
	 * @param file2
	 */
	private void reportOnComparison(Map<String, List<RefUrl>> map1, String file1, Map<String, List<RefUrl>> map2,
			String file2) {
		logger.info("\n\n");
		logger.info("=================================================================");
		logger.info("= Comparing the referenced URLs against the 'success' attribute =");
		logger.info("=================================================================");
		String[] keys = new String[map1.size()];
		map1.keySet().toArray(keys);
		for(String key : keys) {
			if(map2.containsKey(key)) {
				//URL is in both maps.  Remove and compare.
				RefUrl ru1 = map1.remove(key).get(0);
				RefUrl ru2 = map2.remove(key).get(0);
				if(!ru1.getSuccess().equals(ru2.getSuccess())) {
					logger.info("\nURL Mismatch found:");
					logger.info("\t{} from {}", ru1, file1);
					logger.info("\t{} from {}", ru2, file2);
				}
			}
		}
	}

	public void parse(Map<String, List<RefUrl>> map, String filename) throws Exception {
		Reader in = new FileReader(base+filename);
		Iterable<CSVRecord> records = CSVFormat.EXCEL.withHeader("url", "status", "success", "tries", "message")
				.parse(in);
		for (CSVRecord record : records) {
			RefUrl ru = new RefUrl(record.get("url"), record.get("status"), record.get("success"), record.get("tries"),
					record.get("message"));
			if (!ru.isHeader()) {
				if (!map.containsKey(ru.getUrl())) {
					map.put(ru.getUrl(), new ArrayList<RefUrl>());
				}
				map.get(ru.getUrl()).add(ru);
			}
		}
		logger.info("Found {} urls from {}", map.size(), filename);
	}
	
	public void reportOnDuplicates(Map<String, List<RefUrl>> m1, String name1, Map<String, List<RefUrl>> m2, String name2) throws Exception {
		logger.info("\n\n");
		logger.info("=====================================================================================");
		logger.info("= Checking for duplicates.  These will not be checked in the comparison (ambiguous) =");
		logger.info("=====================================================================================");
		innerDuplicatePass(m1,"FIRST",name1,m2,"SECOND",name2);
		innerDuplicatePass(m2,"SECOND",name2,m1,"FIRST",name1);
	}
	
	private void innerDuplicatePass(Map<String, List<RefUrl>> m1, String id1, String name1, Map<String, List<RefUrl>> m2, String id2, String name2) {
		String[] keys = new String[m1.size()];
		m1.keySet().toArray(keys);
		for(String key : keys) {
			if(m1.get(key).size() > 1) {
				logger.info("\nDuplicates on [{}]:", key);
				for(RefUrl ru : m1.remove(key)) {
					logger.info("\t{}: {} [{}]", id1, ru.toStringNoUrl(), name1);
				}
				if(m2.containsKey(key)) {
					for(RefUrl ru : m2.remove(key)) {
						logger.info("\t{}: {} [{}]", id2, ru.toStringNoUrl(), name2);
					}	
				}
			}
		}
	}
}
