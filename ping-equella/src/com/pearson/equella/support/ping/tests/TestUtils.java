package com.pearson.equella.support.ping.tests;

import static org.junit.Assert.fail;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class TestUtils {
	
	public static File createTempProperties() {
		try {
			return File.createTempFile("junitTest"+System.currentTimeMillis(), ".properties");
		} catch (IOException e) {
			fail(e.getMessage());
			return null;
		}
	}
	
	public static void addProp(File props, String key, String value) {
		try {
			BufferedWriter bw = new BufferedWriter(
					new FileWriter(props, true));
			bw.write(key+"="+value);
			bw.newLine();
			bw.flush();
			bw.close();
		} catch (Exception e) {
			fail(e.getMessage());
		}
	}
	
	public static void buildBasicDirectProps(File props) {
		TestUtils.addProp(props, "output.folder", "...../ping-equella/testData/output");
		TestUtils.addProp(props, "client.name", "fish");
		TestUtils.addProp(props, "ping.type", "direct-query-all-items-all-attachments");
		TestUtils.addProp(props, "direct.db.url", "//i/do/not/exist");
		TestUtils.addProp(props, "direct.db.username", "asdf");
		TestUtils.addProp(props, "direct.db.password", "asdf1234");
		TestUtils.addProp(props, "direct.db.type", "SQLSERVER");
		TestUtils.addProp(props, "direct.filestore.dir", "..../ping-equella/testData/filestores/direct-run1/Institutions");
		
	}

}
