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
		ExportItemsDriver eid = new ExportItemsDriver();
		assertEquals("A title of a kaltura link,myfile.pdf", eid.parseAttachmentFilenames(ei));
		
	}
}
