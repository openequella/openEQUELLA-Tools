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
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Properties;

import org.apereo.openequella.tools.toolbox.utils.FileUtils;
import org.junit.Test;

public class FileUtilsTest {
	
	@Test
	public void testExtractSuffixAudio() {
		Properties props = new Properties();
		props.put(Config.OEQ_SEARCH_ATT_SUFFIXES_AUDIO, ".Mp3 , .wma, .wav");
		props.put(Config.OEQ_SEARCH_ATT_SUFFIXES_VIDEO, ".mp4,.mov");
		try {
			Config c = new Config(props);
			c.checkMigrateToKalturaAttachmentSuffixes();
			assertTrue("Config is expected to be valid, but is not.", c.isValidConfig());
			assertEquals(".MP3", FileUtils.extractSuffix(c, "myfilename.mP3"));
		} catch (Exception e) {
			fail(e.getMessage());
		}
		
	}
	
	@Test
	public void testExtractSuffixVideo() {
		Properties props = new Properties();
		props.put(Config.OEQ_SEARCH_ATT_SUFFIXES_AUDIO, ".Mp3, .wma, .wav");
		props.put(Config.OEQ_SEARCH_ATT_SUFFIXES_VIDEO, ".mp4,.mov");
		try {
			Config c = new Config(props);
			c.checkMigrateToKalturaAttachmentSuffixes();
			assertTrue("Config is expected to be valid, but is not.", c.isValidConfig());
			assertEquals(".MOV", FileUtils.extractSuffix(c, "myfilename.moV"));
		} catch (Exception e) {
			fail(e.getMessage());
		}
		
	}
	
	@Test
	public void testExtractSuffixNoSuffix() {
		Properties props = new Properties();
		props.put(Config.OEQ_SEARCH_ATT_SUFFIXES_AUDIO, ".mp3, .wma, .wav");
		props.put(Config.OEQ_SEARCH_ATT_SUFFIXES_VIDEO, ".mp4,.mov");
		try {
			Config c = new Config(props);
			c.checkMigrateToKalturaAttachmentSuffixes();
			assertTrue("Config is expected to be valid, but is not.", c.isValidConfig());
			assertNull(FileUtils.extractSuffix(c, "myfilename"));
		} catch (Exception e) {
			fail(e.getMessage());
		}
		
	}
}
