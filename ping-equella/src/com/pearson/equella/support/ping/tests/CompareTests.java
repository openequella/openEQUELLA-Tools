package com.pearson.equella.support.ping.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.junit.Test;

import com.pearson.equella.support.ping.PingEquellaDriver;
import com.pearson.equella.support.ping.report.ReportManager;
import com.pearson.equella.support.ping.report.ResultComparison;
import com.pearson.equella.support.ping.utils.Config;
import com.pearson.equella.support.ping.utils.PingUtils;

public class CompareTests {
	@Test
	public void testFindOnlyInFirst() {
		Set<String> s1 = new HashSet<String>();
		s1.add("a");
		s1.add("b");
		s1.add("c");
		s1.add("f");
		Set<String> s2 = new HashSet<String>();
		s2.add("c");
		s2.add("d");
		s2.add("e");
		s2.add("f");
		Collection<String> onlyInS1 = PingUtils.findOnlyInFirst(s1, s2);
		assertTrue(onlyInS1.size() == 2);
		assertTrue(onlyInS1.contains("a"));
		assertTrue(onlyInS1.contains("b"));
		Collection<String> onlyInS2 = PingUtils.findOnlyInFirst(s2, s1);
		assertTrue(onlyInS2.size() == 2);
		assertTrue(onlyInS2.contains("d"));
		assertTrue(onlyInS2.contains("e"));
	}

	@Test
	public void testLooseCompare() {
		try {
			assertTrue(PingEquellaDriver
					.setup("testData/props/e2e-run1-attachments.properties"));

			File directReport = new File(
					Config.getInstance().getTestDataDir()
							+ "/outputSamplesToCompare/ping_direct_junitTestsDirectRun1_ItemsInSingleQuery_all_stats_GOLD.csv");
			File attachmentReport = new File(
					Config.getInstance().getTestDataDir()
							+ "/outputSamplesToCompare/ping_attachments_junitTestsAttachmentRun1_all_stats_GOLD.csv");
			assertTrue(directReport.exists());
			assertTrue(attachmentReport.exists());

			ResultComparison cr = PingUtils.looseCompare(directReport,
					attachmentReport);
			assertNotNull(cr);
			assertTrue(!cr.areReportsEqual());
			assertEquals(cr.getOnlyInFirst().size(), 2);
			assertEquals(cr.getOnlyInSecond().size(), 2);
			// This is due to an issue with the attachments ping utility - the
			// html page returned is a 200 resp code, even though it's not the
			// attachment. Creates a good, valid, test.
			String first67 = cr.getOnlyInFirst().get(0).getPrimaryKey();
			String first55 = cr.getOnlyInFirst().get(1).getPrimaryKey();
			if (!first67
					.startsWith("vanilla-......")) {
				String temp = first55;
				first55 = first67;
				first67 = temp;
			}
			String second67 = cr.getOnlyInSecond().get(0).getPrimaryKey();
			String second55 = cr.getOnlyInSecond().get(1).getPrimaryKey();
			if (!second67
					.startsWith("vanilla-.........")) {
				String temp = second55;
				second55 = second67;
				second67 = temp;
			}
			assertEquals(
					first67,
					"vanilla-....-Missing");
			assertEquals(
					first55,
					"vanilla-........-Missing");
			assertEquals(second55,
					"vanilla-....-Present");
			assertEquals(second67,
					"vanilla-....-Present");
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}
	
	@Test
	public void testCompareMissingAttachmentsNoCompare() {
		try {
			assertTrue(PingEquellaDriver
					.setup("testData/props/e2e-run1-direct-items-in-single-query.properties"));
			assertTrue(PingEquellaDriver.run());
			assertTrue(PingEquellaDriver.finalizeRun());

			assertTrue(PingEquellaDriver
					.setup("testData/props/e2e-run1-direct-items-in-single-query.properties"));
			assertTrue(PingEquellaDriver.run());
			assertTrue(PingEquellaDriver.finalizeRun());

			
			ResultComparison cr = ReportManager.getInstance().getComparison();
			assertNull(cr);
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}
	
	@Test
	public void testCompareMissingAttachmentsCompareShouldBeDifferentFirst() {
		try {
			File props = TestUtils.createTempProperties();
			TestUtils.addProp(props, "ping.type", "direct-query-batched-items-attachments-per-item");
			TestUtils.addProp(props, "output.folder", "...../ping-equella/testData/output");
			TestUtils.addProp(props, "client.name", "compareTests");
			TestUtils.addProp(props, "direct.db.url", "//...:2301;databaseName=eq;instanceName=EQDB");
			TestUtils.addProp(props, "direct.db.username", "....");
			TestUtils.addProp(props, "direct.db.password", "....");
			TestUtils.addProp(props, "direct.db.type", "SQLSERVER");
			TestUtils.addProp(props, "direct.filestore.dir", ".../ping-equella/testData/filestores/direct-run1/Institutions");
			TestUtils.addProp(props, "direct.num.items.per.query", "200");
			//TestUtils.addProp(props, "direct.filter.by.collection.id", "5");
			TestUtils.addProp(props, "filter.by.institution.shortname", "vanilla");
			TestUtils.addProp(props, "test.data.directory", ".../ping-equella/testData/");
			
			assertTrue(PingEquellaDriver
					.setup(props.getAbsolutePath()));
			
			assertTrue(PingEquellaDriver.run());
			assertTrue(PingEquellaDriver.finalizeRun());

			props = TestUtils.createTempProperties();
			TestUtils.addProp(props, "compare.missing.attachments", "TrUe");
			TestUtils.addProp(props, "ping.type", "direct-query-batched-items-attachments-per-item");
			TestUtils.addProp(props, "output.folder", "..../ping-equella/testData/output");
			TestUtils.addProp(props, "client.name", "compareTests");
			TestUtils.addProp(props, "direct.db.url", "//...:2301;databaseName=eq;instanceName=EQDB");
			TestUtils.addProp(props, "direct.db.username", "...");
			TestUtils.addProp(props, "direct.db.password", "...");
			TestUtils.addProp(props, "direct.db.type", "SQLSERVER");
			TestUtils.addProp(props, "direct.filestore.dir", ".../ping-equella/testData/filestores/direct-run1/Institutions");
			TestUtils.addProp(props, "direct.num.items.per.query", "200");
			TestUtils.addProp(props, "direct.filter.by.collection.id", "5");
			TestUtils.addProp(props, "filter.by.institution.shortname", "vanilla");
			TestUtils.addProp(props, "test.data.directory", "..../ping-equella/testData/");
			TestUtils.addProp(props, "email.report", "NORMAL");
			TestUtils.addProp(props, "email.smtp.server.port", "587");
			TestUtils.addProp(props, "email.smtp.server", "smtp.gmail.com");
			TestUtils.addProp(props, "email.sender.display.name", "testCompareMissingAttachmentsCompareShouldBeDifferentFirst");
			TestUtils.addProp(props, "email.sender.username", System.getProperty("JUNIT_EMAIL_USERNAME"));
			TestUtils.addProp(props, "email.sender.password", System.getProperty("JUNIT_EMAIL_PASSWORD"));
			TestUtils.addProp(props, "email.recipients ", System.getProperty("JUNIT_EMAIL_SINGLE_TO_LIST"));
			
			assertTrue(PingEquellaDriver
					.setup(props.getAbsolutePath()));
			assertTrue(PingEquellaDriver.run());
			assertTrue(PingEquellaDriver.finalizeRun());

			assertFalse(ReportManager.getInstance().hasFatalErrors());
			
			ResultComparison cr = ReportManager.getInstance().getComparison();
			assertNotNull(cr);
			assertFalse(cr.areReportsEqual());
			assertEquals(cr.getOnlyInFirst().size(), 3);
			assertEquals(cr.getOnlyInSecond().size(), 0);
			assertEquals(cr.getOnlyInFirst().get(0).getPrimaryKey(),
					"vanilla-....-Missing");
			assertEquals(cr.getOnlyInFirst().get(1).getPrimaryKey(),
					"vanilla-....-Missing");
			assertEquals(cr.getOnlyInFirst().get(2).getPrimaryKey(),
					"vanilla-....-1-[[Attachment UUID not set]]-No Att");
			
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}

	@Test
	public void testCompareMissingAttachmentsCompareShouldBeDifferentSecond() {
		try {
			File props = TestUtils.createTempProperties();
			TestUtils.addProp(props, "ping.type", "direct-query-batched-items-attachments-per-item");
			TestUtils.addProp(props, "output.folder", "..../ping-equella/testData/output");
			TestUtils.addProp(props, "client.name", "compareTests");
			TestUtils.addProp(props, "direct.db.url", "//....:2301;databaseName=eq;instanceName=EQDB");
			TestUtils.addProp(props, "direct.db.username", "...");
			TestUtils.addProp(props, "direct.db.password", "...");
			TestUtils.addProp(props, "direct.db.type", "SQLSERVER");
			TestUtils.addProp(props, "direct.filestore.dir", "...../ping-equella/testData/filestores/direct-run1/Institutions");
			TestUtils.addProp(props, "direct.num.items.per.query", "200");
			TestUtils.addProp(props, "direct.filter.by.collection.id", "5");
			TestUtils.addProp(props, "filter.by.institution.shortname", "vanilla");
			TestUtils.addProp(props, "test.data.directory", "...../ping-equella/testData/");
			
			assertTrue(PingEquellaDriver
					.setup(props.getAbsolutePath()));
			
			assertTrue(PingEquellaDriver.run());
			assertTrue(PingEquellaDriver.finalizeRun());

			props = TestUtils.createTempProperties();
			
			TestUtils.addProp(props, "compare.missing.attachments", "TrUe");
			TestUtils.addProp(props, "ping.type", "direct-query-batched-items-attachments-per-item");
			TestUtils.addProp(props, "output.folder", "..../ping-equella/testData/output");
			TestUtils.addProp(props, "client.name", "compareTests");
			TestUtils.addProp(props, "direct.db.url", "//...:2301;databaseName=eq;instanceName=EQDB");
			TestUtils.addProp(props, "direct.db.username", "....");
			TestUtils.addProp(props, "direct.db.password", "....");
			TestUtils.addProp(props, "direct.db.type", "SQLSERVER");
			TestUtils.addProp(props, "direct.filestore.dir", "..../ping-equella/testData/filestores/direct-run1/Institutions");
			TestUtils.addProp(props, "direct.num.items.per.query", "200");
			//TestUtils.addProp(props, "direct.filter.by.collection.id", "5");
			TestUtils.addProp(props, "filter.by.institution.shortname", "vanilla");
			TestUtils.addProp(props, "test.data.directory", "..../ping-equella/testData/");
			TestUtils.addProp(props, "email.report", "ONLY_NEW_MISSING_ATTACHMENTS_OR_ERRORS");
			TestUtils.addProp(props, "email.smtp.server.port", "587");
			TestUtils.addProp(props, "email.smtp.server", "smtp.gmail.com");
			TestUtils.addProp(props, "email.sender.display.name", "testCompareMissingAttachmentsCompareShouldBeDifferentSecond");
			TestUtils.addProp(props, "email.sender.username", System.getProperty("JUNIT_EMAIL_USERNAME"));
			TestUtils.addProp(props, "email.sender.password", System.getProperty("JUNIT_EMAIL_PASSWORD"));
			TestUtils.addProp(props, "email.recipients ", System.getProperty("JUNIT_EMAIL_SINGLE_TO_LIST"));
			
			assertTrue(PingEquellaDriver
					.setup(props.getAbsolutePath()));
			assertTrue(PingEquellaDriver.run());
			assertTrue(PingEquellaDriver.finalizeRun());

			assertFalse(ReportManager.getInstance().hasFatalErrors());
			ResultComparison cr = ReportManager.getInstance().getComparison();
			assertNotNull(cr);
			assertFalse(cr.areReportsEqual());
			assertEquals(cr.getOnlyInFirst().size(), 0);
			assertEquals(cr.getOnlyInSecond().size(), 3);
			assertEquals(cr.getOnlyInSecond().get(0).getPrimaryKey(),
					"vanilla-.....-Missing");
			assertEquals(cr.getOnlyInSecond().get(1).getPrimaryKey(),
					"vanilla-.....-Missing");
			assertEquals(cr.getOnlyInSecond().get(2).getPrimaryKey(),
					"vanilla-....-1-[[Attachment UUID not set]]-No Att");
			
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}

	@Test
	public void testCompareMissingAttachmentsCompareShouldBeEqual() {
		try {
			assertTrue(PingEquellaDriver
					.setup("testData/props/e2e-run1-direct-items-in-single-query.properties"));
			assertTrue(PingEquellaDriver.run());
			assertTrue(PingEquellaDriver.finalizeRun());

			File props = TestUtils.createTempProperties();
			TestUtils.addProp(props, "compare.missing.attachments", "TrUe");
			TestUtils.addProp(props, "ping.type", "direct-query-all-items-all-attachments");
			TestUtils.addProp(props, "output.folder", "...../ping-equella/testData/output");
			TestUtils.addProp(props, "client.name", "junitTestsDirectRun1");
			TestUtils.addProp(props, "direct.db.url", "//...:2301;databaseName=eq;instanceName=EQDB");
			TestUtils.addProp(props, "direct.db.username", "....");
			TestUtils.addProp(props, "direct.db.password", "....");
			TestUtils.addProp(props, "direct.db.type", "SQLSERVER");
			TestUtils.addProp(props, "direct.filestore.dir", "..../ping-equella/testData/filestores/direct-run1/Institutions");
			//TestUtils.addProp(props, "direct.num.items.per.query", "100");
			TestUtils.addProp(props, "filter.by.institution.shortname", "vanilla");
			TestUtils.addProp(props, "test.data.directory", "..../ping-equella/testData/");
			TestUtils.addProp(props, "email.report", "ONLY_NEW_MISSING_ATTACHMENTS_OR_ERRORS");
			TestUtils.addProp(props, "email.smtp.server.port", "587");
			TestUtils.addProp(props, "email.smtp.server", "smtp.gmail.com");
			TestUtils.addProp(props, "email.sender.display.name", "testCompareMissingAttachmentsCompareShouldBeEqual");
			TestUtils.addProp(props, "email.sender.username", System.getProperty("JUNIT_EMAIL_USERNAME"));
			TestUtils.addProp(props, "email.sender.password", System.getProperty("JUNIT_EMAIL_PASSWORD"));
			TestUtils.addProp(props, "email.recipients ", System.getProperty("JUNIT_EMAIL_SINGLE_TO_LIST"));
			
			assertTrue(PingEquellaDriver
					.setup(props.getAbsolutePath()));
			assertTrue(PingEquellaDriver.run());
			assertTrue(PingEquellaDriver.finalizeRun());

			
			ResultComparison cr = ReportManager.getInstance().getComparison();
			assertNotNull(cr);
			assertTrue(cr.areReportsEqual());
			
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}
	
	@Test
	public void testCompareMissingAttachmentsCompareShouldBeEqualWithNormalEmail() {
		try {
			assertTrue(PingEquellaDriver
					.setup("testData/props/e2e-run1-direct-items-in-single-query.properties"));
			assertTrue(PingEquellaDriver.run());
			assertTrue(PingEquellaDriver.finalizeRun());

			File props = TestUtils.createTempProperties();
			TestUtils.addProp(props, "compare.missing.attachments", "TrUe");
			TestUtils.addProp(props, "ping.type", "direct-query-batched-items-attachments-per-item");
			TestUtils.addProp(props, "output.folder", "..../ping-equella/testData/output");
			TestUtils.addProp(props, "client.name", "junitTestsDirectRun1");
			TestUtils.addProp(props, "direct.db.url", "//....:2301;databaseName=eq;instanceName=EQDB");
			TestUtils.addProp(props, "direct.db.username", "....");
			TestUtils.addProp(props, "direct.db.password", "....");
			TestUtils.addProp(props, "direct.db.type", "SQLSERVER");
			TestUtils.addProp(props, "direct.filestore.dir", "..../ping-equella/testData/filestores/direct-run1/Institutions");
			TestUtils.addProp(props, "direct.num.items.per.query", "100");
			TestUtils.addProp(props, "filter.by.institution.shortname", "vanilla");
			TestUtils.addProp(props, "test.data.directory", "..../ping-equella/testData/");
			TestUtils.addProp(props, "email.report", "NORMAL");
			TestUtils.addProp(props, "email.smtp.server.port", "587");
			TestUtils.addProp(props, "email.smtp.server", "smtp.gmail.com");
			TestUtils.addProp(props, "email.sender.display.name", "testCompareMissingAttachmentsCompareShouldBeEqualWithNormalEmail");
			TestUtils.addProp(props, "email.sender.username", System.getProperty("JUNIT_EMAIL_USERNAME"));
			TestUtils.addProp(props, "email.sender.password", System.getProperty("JUNIT_EMAIL_PASSWORD"));
			TestUtils.addProp(props, "email.recipients ", System.getProperty("JUNIT_EMAIL_SINGLE_TO_LIST"));
			
			assertTrue(PingEquellaDriver
					.setup(props.getAbsolutePath()));
			assertTrue(PingEquellaDriver.run());
			assertTrue(PingEquellaDriver.finalizeRun());

			
			ResultComparison cr = ReportManager.getInstance().getComparison();
			assertNotNull(cr);
			assertTrue(cr.areReportsEqual());
			
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}
}
