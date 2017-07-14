package com.pearson.equella.support.ping.tests;

import static org.junit.Assert.*;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;

import org.junit.Test;

import com.pearson.equella.support.ping.PingEquellaDriver;
import com.pearson.equella.support.ping.report.ReportManager;
import com.pearson.equella.support.ping.utils.Config;
import com.pearson.equella.support.ping.utils.PingUtils;

public class AttachmentPingTests {
	@Test
	public void testRun1() throws IOException {
		try {
			assertTrue(PingEquellaDriver.setup("testData/props/e2e-run1-attachments.properties")); // Confirm no issues
			assertTrue(PingEquellaDriver.run());
			assertTrue(PingEquellaDriver.finalizeRun());
			
			//ALL results
			File allResults = new File(ReportManager.getInstance().getStdOutFilename());
			String filename = Config.getInstance().getTestDataDir()+"/outputSamplesToCompare/ping_attachments_junitTestsAttachmentRun1_all_stats_GOLD.csv";
			File allResultsToCompare = new File(filename);
			assertTrue(allResults.exists());
			assertTrue(allResultsToCompare.exists());
			assertTrue(PingUtils.mediumStrictCompare(allResults, allResultsToCompare));
			
			//ERROR results
			File errResults = new File(ReportManager.getInstance().getErrOutFilename());
			String errFilename = Config.getInstance().getTestDataDir()+"/outputSamplesToCompare/ping_attachments_junitTestsAttachmentRun1_error_stats_GOLD.csv";
			File errResultsToCompare = new File(errFilename);
			assertTrue(errResults.exists());
			assertTrue(errResultsToCompare.exists());
			assertTrue(PingUtils.mediumStrictCompare(errResults, errResultsToCompare));
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}
	
	@Test
	public void testRun2Vanilla15() throws IOException {
		try {
			assertTrue(PingEquellaDriver.setup("testData/props/e2e-run2-vanilla15-attachments.properties")); // Confirm no issues
			assertTrue(PingEquellaDriver.run());
			assertTrue(PingEquellaDriver.finalizeRun());
			
			//ALL results
			File allResults = new File(ReportManager.getInstance().getStdOutFilename());
			String filename = Config.getInstance().getTestDataDir()+"/outputSamplesToCompare/ping_attachments_junitTestsAttachmentRun2_vanilla15_all_stats_GOLD.csv";
			File allResultsToCompare = new File(filename);
			assertTrue(allResults.exists());
			assertTrue(allResultsToCompare.exists());
			assertTrue(PingUtils.mediumStrictCompare(allResults, allResultsToCompare));
			
			//ERROR results
			File errResults = new File(ReportManager.getInstance().getErrOutFilename());
			String errFilename = Config.getInstance().getTestDataDir()+"/outputSamplesToCompare/ping_attachments_junitTestsAttachmentRun2_vanilla15_error_stats_GOLD.csv";
			File errResultsToCompare = new File(errFilename);
			assertTrue(errResults.exists());
			assertTrue(errResultsToCompare.exists());
			assertTrue(PingUtils.mediumStrictCompare(errResults, errResultsToCompare));
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}
	
	@Test
	public void testRun1FilterByCollection() throws IOException {
		try {
			File props = TestUtils.createTempProperties();
			TestUtils.addProp(props, "compare.missing.attachments", "false");
			TestUtils.addProp(props, "ping.type", "attachments");
			TestUtils.addProp(props, "output.folder", "...../ping-equella/testData/output");
			TestUtils.addProp(props, "client.name", "junitTestsAttachmentRun1FilterByCollection");
			TestUtils.addProp(props, "institution.url", "http://eq-test-url:8611/vanilla");
			TestUtils.addProp(props, "client.id", "uuid......");
			TestUtils.addProp(props, "client.secret", "uuid....");
			TestUtils.addProp(props, "ping.max.tries", "3");
			TestUtils.addProp(props, "ping.test.times.to.inject.timeout", "0");
			TestUtils.addProp(props, "attachments.filter.by.collection.uuid", ".......");
			TestUtils.addProp(props, "test.data.directory", "...../ping-equella/testData/");
			TestUtils.addProp(props, "email.report", "NORMAL");
			TestUtils.addProp(props, "email.smtp.server.port", "587");
			TestUtils.addProp(props, "email.smtp.server", "smtp.gmail.com");
			TestUtils.addProp(props, "email.sender.display.name", "attachment-testRun1FilterByCollection");
			TestUtils.addProp(props, "email.sender.username", System.getProperty("JUNIT_EMAIL_USERNAME"));
			TestUtils.addProp(props, "email.sender.password", System.getProperty("JUNIT_EMAIL_PASSWORD"));
			TestUtils.addProp(props, "email.recipients ", System.getProperty("JUNIT_EMAIL_MULTI_TO_LIST"));
			
			assertTrue(PingEquellaDriver
					.setup(props.getAbsolutePath()));
			
			assertTrue(PingEquellaDriver.run());
			assertTrue(PingEquellaDriver.finalizeRun());
			
			//ALL results
			File allResults = new File(ReportManager.getInstance().getStdOutFilename());
			String filename = Config.getInstance().getTestDataDir()+"/outputSamplesToCompare/ping_attachments_junitTestsAttachmentRun1FilterByCollection_all_stats_GOLD.csv";
			File allResultsToCompare = new File(filename);
			assertTrue(allResults.exists());
			assertTrue(allResultsToCompare.exists());
			assertTrue(PingUtils.mediumStrictCompare(allResults, allResultsToCompare));
			
			//ERROR results
			File errResults = new File(ReportManager.getInstance().getErrOutFilename());
			String errFilename = Config.getInstance().getTestDataDir()+"/outputSamplesToCompare/ping_attachments_junitTestsAttachmentRun1FilterByCollection_error_stats_GOLD.csv";
			File errResultsToCompare = new File(errFilename);
			assertTrue(errResults.exists());
			assertTrue(errResultsToCompare.exists());
			assertTrue(PingUtils.mediumStrictCompare(errResults, errResultsToCompare));
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}
	
	@Test
	public void testFilterByCollectionBad() throws IOException {
		try {
			assertTrue(PingEquellaDriver.setup("testData/props/attachments-filter-by-collection-bad.properties")); // Confirm no issues
			assertTrue(PingEquellaDriver.run());
			assertTrue(PingEquellaDriver.finalizeRun());
			
			//ALL results
			File allResults = new File(ReportManager.getInstance().getStdOutFilename());
			String filename = Config.getInstance().getTestDataDir()+"/outputSamplesToCompare/ping_attachments_junitTestsAttachmentRun1FilterByCollectionBad_all_stats_GOLD.csv";
			File allResultsToCompare = new File(filename);
			assertTrue(allResults.exists());
			assertTrue(allResultsToCompare.exists());
			assertTrue(PingUtils.mediumStrictCompare(allResults, allResultsToCompare));
			
			//ERROR results
			File errResults = new File(ReportManager.getInstance().getErrOutFilename());
			String errFilename = Config.getInstance().getTestDataDir()+"/outputSamplesToCompare/ping_attachments_junitTestsAttachmentRun1FilterByCollectionBad_error_stats_GOLD.csv";
			File errResultsToCompare = new File(errFilename);
			assertTrue(errResults.exists());
			assertTrue(errResultsToCompare.exists());
			assertTrue(PingUtils.mediumStrictCompare(errResults, errResultsToCompare));
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}
	
	/**
	 * An empty filter is a non-existant filter.
	 * @throws IOException
	 */
	@Test
	public void testFilterByCollectionEmpty() throws IOException {
		try {
			assertTrue(PingEquellaDriver.setup("testData/props/attachments-filter-by-collection-empty.properties")); // Confirm no issues
			assertTrue(PingEquellaDriver.run());
			assertTrue(PingEquellaDriver.finalizeRun());
			
			//ALL results
			File allResults = new File(ReportManager.getInstance().getStdOutFilename());
			String filename = Config.getInstance().getTestDataDir()+"/outputSamplesToCompare/ping_attachments_junitTestsAttachmentRun1_all_stats_GOLD.csv";
			File allResultsToCompare = new File(filename);
			assertTrue(allResults.exists());
			assertTrue(allResultsToCompare.exists());
			assertTrue(PingUtils.mediumStrictCompare(allResults, allResultsToCompare));
			
			//ERROR results
			File errResults = new File(ReportManager.getInstance().getErrOutFilename());
			String errFilename = Config.getInstance().getTestDataDir()+"/outputSamplesToCompare/ping_attachments_junitTestsAttachmentRun1_error_stats_GOLD.csv";
			File errResultsToCompare = new File(errFilename);
			assertTrue(errResults.exists());
			assertTrue(errResultsToCompare.exists());
			assertTrue(PingUtils.mediumStrictCompare(errResults, errResultsToCompare));
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}
}
