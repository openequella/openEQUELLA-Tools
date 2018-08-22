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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apereo.openequella.tools.toolbox.utils.EquellaItem;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;

public class ExportItemsDriverTest {
	
	@Test 
	public void testParseAttachmentFilenames() {
		JSONObject json = new JSONObject();
		JSONObject att1 = new JSONObject();
		att1.put("type", "kaltura");
		att1.put("description", "This is an interesting kaltura link description");
		att1.put("title", "A title of a kaltura link");
		JSONObject att2 = new JSONObject();
		att2.put("type", "file");
		att2.put("description", "This is an interesting file description");
		att2.put("filename", "myfile.pdf");
		JSONObject att3 = new JSONObject();
		att3.put("type", "custom");
		att3.put("description", "This is an interesting custom description");
		att3.put("filename", "not real");
		JSONArray atts = new JSONArray();
		atts.put(att1);
		atts.put(att2);
		atts.put(att3);
		json.put("attachments", atts);
		EquellaItem ei = new EquellaItem();
		ei.setJson(json);
		ei.setUuid("ec48a0e1-9643-4d50-840a-db26fd9fa15a");
		ei.setVersion(1);
		ExportItemsDriver eid = new ExportItemsDriver();
		Properties props = new Properties();
		props.put(Config.EXPORT_ITEMS_ATTACHMENT_PATH_TEMPLATE, "/Attachments/@HASH/@UUID/@VERSION/@FILENAME");
		assertEquals("/Attachments/40/ec48a0e1-9643-4d50-840a-db26fd9fa15a/1/myfile.pdf", eid.parseAttachmentFilenames(ei, new Config(props)));
		
	}
	
	@Test 
	public void testFindFirstKalturaIdInAttachments() {
		JSONObject json = new JSONObject();
		JSONObject att1 = new JSONObject();
		att1.put("type", "kaltura");
		att1.put("description", "This is an interesting kaltura link description");
		att1.put("mediaId", "0_12345");
		JSONObject att2 = new JSONObject();
		att2.put("type", "file");
		att2.put("description", "This is an interesting file description");
		att2.put("filename", "myfile.pdf");
		JSONObject att3 = new JSONObject();
		att3.put("type", "kaltura");
		att3.put("description", "This is an interesting kaltura link description");
		att3.put("mediaId", "0_98765");
		JSONArray atts = new JSONArray();
		atts.put(att1);
		atts.put(att2);
		atts.put(att3);
		json.put("attachments", atts);
		EquellaItem ei = new EquellaItem();
		ei.setJson(json);
		ExportItemsDriver eid = new ExportItemsDriver();
		assertEquals("0_12345", eid.findFirstKalturaIdInAttachments(ei));
		
	}
	
	@Test 
	public void testBuildRecordMultipleValuesTogether() {
		EquellaItem ei = new EquellaItem();
		ei.setMetadata("<xml><metadata><general>asdf</general><keywords><keyword>k1</keyword><keyword>k2</keyword></keywords></metadata></xml>");
		
		List<String> headers = new ArrayList<>();
		headers.add("metadata/keywords/keyword");
		
		ExportItemsDriver eid = new ExportItemsDriver();
		try {
			List<String> result = eid.buildRecord(headers, ei, null);
			assertEquals(1, result.size());
			assertEquals("k1,k2", result.get(0));
		} catch (Exception e) {
			fail("buildRecord failed with: " + e.getMessage());
		}
		
	}
	
	@Test 
	public void testBuildRecordMultipleValuesApart() {
		EquellaItem ei = new EquellaItem();
		ei.setMetadata("<xml><metadata><keywords><keyword>k1</keyword></keywords><general>asdf</general><keywords><keyword>k2</keyword><keyword>k3</keyword></keywords></metadata></xml>");
		
		List<String> headers = new ArrayList<>();
		headers.add("metadata/keywords/keyword");
		
		ExportItemsDriver eid = new ExportItemsDriver();
		try {
			List<String> result = eid.buildRecord(headers, ei, null);
			assertEquals(1, result.size());
			assertEquals("k1,k2,k3", result.get(0));
		} catch (Exception e) {
			fail("buildRecord failed with: " + e.getMessage());
		}
		
	}
}
