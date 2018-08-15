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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Properties;

import org.junit.Test;

public class ConfigTest {
	@Test
	public void testConfigDateFormatOeqApi() {
		String date = "2013-01-18T11:39:07.290-07:00";
		try {
			Config.DATE_FORMAT_OEQ_API.parse(date);
		} catch (Exception e) {
			fail(e.getMessage());
		}
		
	}
	
	@Test
	public void testConfigDateFormatConfigFile() {
		String date = "2013-01-18";
		try {
			Config.DATE_FORMAT_CONFIG_FILE.parse(date);
		} catch (Exception e) {
			fail(e.getMessage());
		}
		
	}
	
	@Test
	public void testConfigMigrateToKalturaAttachmentSuffixes() {
		Properties props = new Properties();
		props.put(Config.OEQ_SEARCH_ATT_SUFFIXES_AUDIO, "");
		props.put(Config.OEQ_SEARCH_ATT_SUFFIXES_VIDEO, "");
		try {
			Config c = new Config(props);
			c.checkMigrateToKalturaAttachmentSuffixes();
			assertTrue("Config is expected to be valid, but is not.", c.isValidConfig());
		} catch (Exception e) {
			fail(e.getMessage());
		}
		
	}
	
	@Test
	public void testConfigMigrateToKalturaAttachmentSuffixesReqVideo() {
		Properties props = new Properties();
		props.put(Config.OEQ_SEARCH_ATT_SUFFIXES_AUDIO, "");
		try {
			Config c = new Config(props);
			c.checkMigrateToKalturaAttachmentSuffixes();
			assertFalse("Config is expected to be invalid, but was valid.", c.isValidConfig());
		} catch (Exception e) {
			fail(e.getMessage());
		}
		
	}
	
	@Test
	public void testConfigMigrateToKalturaAttachmentSuffixesReqAudio() {
		Properties props = new Properties();
		props.put(Config.OEQ_SEARCH_ATT_SUFFIXES_VIDEO, "");
		try {
			Config c = new Config(props);
			c.checkMigrateToKalturaAttachmentSuffixes();
			assertFalse("Config is expected to be invalid, but was valid.", c.isValidConfig());
		} catch (Exception e) {
			fail(e.getMessage());
		}
		
	}
	
	@Test
	public void testConfigMigrateToKalturaAttachmentSuffixesUniqueNoAudio() {
		Properties props = new Properties();
		props.put(Config.OEQ_SEARCH_ATT_SUFFIXES_AUDIO, "");
		props.put(Config.OEQ_SEARCH_ATT_SUFFIXES_VIDEO, ".mp4");
		try {
			Config c = new Config(props);
			c.checkMigrateToKalturaAttachmentSuffixes();
			assertTrue("Config is expected to be valid, but is not.", c.isValidConfig());
		} catch (Exception e) {
			fail(e.getMessage());
		}
		
	}
	
	@Test
	public void testConfigMigrateToKalturaAttachmentSuffixesUniqueNoVideo() {
		Properties props = new Properties();
		props.put(Config.OEQ_SEARCH_ATT_SUFFIXES_AUDIO, ".mp3");
		props.put(Config.OEQ_SEARCH_ATT_SUFFIXES_VIDEO, "");
		try {
			Config c = new Config(props);
			c.checkMigrateToKalturaAttachmentSuffixes();
			assertTrue("Config is expected to be valid, but is not.", c.isValidConfig());
		} catch (Exception e) {
			fail(e.getMessage());
		}
		
	}
	
	@Test
	public void testConfigMigrateToKalturaAttachmentSuffixesUniqueFailedSingle() {
		Properties props = new Properties();
		props.put(Config.OEQ_SEARCH_ATT_SUFFIXES_AUDIO, ".mp4");
		props.put(Config.OEQ_SEARCH_ATT_SUFFIXES_VIDEO, ".mp4");
		try {
			Config c = new Config(props);
			c.checkMigrateToKalturaAttachmentSuffixes();
			assertFalse("Config is expected to be invalid, but is valid.", c.isValidConfig());
		} catch (Exception e) {
			fail(e.getMessage());
		}
		
	}
	
	@Test
	public void testConfigMigrateToKalturaAttachmentSuffixesUniqueFailedMultiple() {
		Properties props = new Properties();
		props.put(Config.OEQ_SEARCH_ATT_SUFFIXES_AUDIO, ".wma,.mp4");
		props.put(Config.OEQ_SEARCH_ATT_SUFFIXES_VIDEO, " .mp4, .mov");
		try {
			Config c = new Config(props);
			c.checkMigrateToKalturaAttachmentSuffixes();
			assertFalse("Config is expected to be invalid, but is valid.", c.isValidConfig());
		} catch (Exception e) {
			fail(e.getMessage());
		}
		
	}
	
	@Test
	public void testConfigMigrateToKalturaAttachmentSuffixesUniqueGeneral() {
		Properties props = new Properties();
		props.put(Config.OEQ_SEARCH_ATT_SUFFIXES_AUDIO, ".mp3, .wma, .wav");
		props.put(Config.OEQ_SEARCH_ATT_SUFFIXES_VIDEO, ".mp4,.mov");
		try {
			Config c = new Config(props);
			c.checkMigrateToKalturaAttachmentSuffixes();
			assertTrue("Config is expected to be valid, but is not.", c.isValidConfig());
		} catch (Exception e) {
			fail(e.getMessage());
		}
		
	}
}
