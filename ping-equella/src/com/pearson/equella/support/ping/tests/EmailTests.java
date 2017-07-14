package com.pearson.equella.support.ping.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.util.Date;

import org.junit.Test;

import com.pearson.equella.support.ping.PingEquellaDriver;
import com.pearson.equella.support.ping.report.Email;
import com.pearson.equella.support.ping.report.ReportManager;
import com.pearson.equella.support.ping.utils.Config;

public class EmailTests {
	@Test
	public void testEmailSettingsE2E() {
		File props = TestUtils.createTempProperties();
		TestUtils.buildBasicDirectProps(props);
		TestUtils.addProp(props, "email.report", "NORMAL");
		TestUtils.addProp(props, "email.smtp.server.port", "587");
		TestUtils.addProp(props, "email.smtp.server", "smtp.gmail.com");
		TestUtils.addProp(props, "email.sender.display.name",
				"testEmailSettingsE2E");
		TestUtils.addProp(props, "email.sender.username",
				System.getProperty("JUNIT_EMAIL_USERNAME"));
		TestUtils.addProp(props, "email.sender.password",
				System.getProperty("JUNIT_EMAIL_PASSWORD"));
		TestUtils.addProp(props, "email.recipients ",
				System.getProperty("JUNIT_EMAIL_MULTI_TO_LIST"));

		assertTrue(PingEquellaDriver.setup(props.getAbsolutePath()));
		Email em = new Email();
		em.send("Resultant testEmailSettingsE2E email - " + (new Date()),
				Config.getInstance().getEmailRecipients(),
				"This is a test message for the JUnit test testEmailSettingsE2E");
		if (ReportManager.getInstance().hasFatalErrors()) {
			fail(ReportManager.getInstance().getLastFatalError());
		}
	}

	@Test
	public void testEmailSettingsE2EonlyChangedAtts() {
		File props = TestUtils.createTempProperties();
		TestUtils.buildBasicDirectProps(props);
		TestUtils.addProp(props, "email.report",
				"ONLY_NEW_MISSING_ATTACHMENTS_OR_ERRORS");
		TestUtils.addProp(props, "email.smtp.server.port", "587");
		TestUtils.addProp(props, "email.smtp.server", "smtp.gmail.com");
		TestUtils.addProp(props, "email.sender.display.name",
				"testEmailSettingsE2EonlyChangedAtts");
		TestUtils.addProp(props, "email.sender.username",
				System.getProperty("JUNIT_EMAIL_USERNAME"));
		TestUtils.addProp(props, "email.sender.password",
				System.getProperty("JUNIT_EMAIL_PASSWORD"));
		TestUtils.addProp(props, "email.recipients ",
				System.getProperty("JUNIT_EMAIL_SINGLE_TO_LIST"));

		assertTrue(PingEquellaDriver.setup(props.getAbsolutePath()));
		Email em = new Email();
		em.send("Resultant testEmailSettingsE2EonlyChangedAtts email - "
				+ (new Date()), Config.getInstance().getEmailRecipients(),
				"This is a test message for the JUnit test testEmailSettingsE2E");
		if (ReportManager.getInstance().hasFatalErrors()) {
			fail(ReportManager.getInstance().getLastFatalError());
		}
	}

	/**
	 * Should result in a 'Critical Failure' email about the DB connection.
	 */
	@Test
	public void testEmailSettingsFailFastWithOnlyMissingAttsEmailSpecified() {
		File props = TestUtils.createTempProperties();
		TestUtils.buildBasicDirectProps(props);
		TestUtils.addProp(props, "ping.type",
				"direct-query-batched-items-attachments-per-item");
		TestUtils
				.addProp(
						props,
						"output.folder",
						"..../ping-equella/testData/output");
		TestUtils.addProp(props, "client.name", "compareTests");
		TestUtils.addProp(props, "direct.db.url",
				"//...2301;databaseName=eq;instanceName=EQDB");
		TestUtils.addProp(props, "direct.db.username", "....");
		TestUtils.addProp(props, "direct.db.password", "....");
		TestUtils.addProp(props, "direct.db.type", "SQLSERVER");
		TestUtils
				.addProp(
						props,
						"direct.filestore.dir",
						"...../ping-equella/testData/filestores/direct-run1/Institutions");
		TestUtils.addProp(props, "direct.num.items.per.query", "200");
		TestUtils.addProp(props, "direct.filter.by.collection.id", "5");
		TestUtils.addProp(props, "filter.by.institution.shortname", "vanilla");
		TestUtils
				.addProp(
						props,
						"test.data.directory",
						"..../ping-equella/testData/");
		TestUtils.addProp(props, "email.report",
				"ONLY_NEW_MISSING_ATTACHMENTS_OR_ERRORS");
		TestUtils.addProp(props, "email.smtp.server.port", "587");
		TestUtils.addProp(props, "email.smtp.server", "smtp.gmail.com");
		TestUtils
				.addProp(props, "email.sender.display.name",
						"ChrisB testEmailSettingsFailFastWithOnlyMissingAttsEmailSpecified");
		TestUtils.addProp(props, "email.sender.username",
				System.getProperty("JUNIT_EMAIL_USERNAME"));
		TestUtils.addProp(props, "email.sender.password",
				System.getProperty("JUNIT_EMAIL_PASSWORD"));
		TestUtils.addProp(props, "email.recipients ",
				System.getProperty("JUNIT_EMAIL_SINGLE_TO_LIST"));

		assertTrue(PingEquellaDriver.setup(props.getAbsolutePath()));
		assertFalse(PingEquellaDriver.run());
		ReportManager.getInstance().failFast();
	}

	@Test
	public void testEmailSettingsFailFastWithNormalEmailSpecified() {
		File props = TestUtils.createTempProperties();
		TestUtils.buildBasicDirectProps(props);
		TestUtils.addProp(props, "ping.type",
				"direct-query-batched-items-attachments-per-item");
		TestUtils
				.addProp(
						props,
						"output.folder",
						"...../ping-equella/testData/output");
		TestUtils.addProp(props, "client.name", "compareTests");
		TestUtils.addProp(props, "direct.db.url",
				"//....:2301;databaseName=eq;instanceName=EQDB");
		TestUtils.addProp(props, "direct.db.username", "....");
		TestUtils.addProp(props, "direct.db.password", "....");
		TestUtils.addProp(props, "direct.db.type", "SQLSERVER");
		TestUtils
				.addProp(
						props,
						"direct.filestore.dir",
						"..../ping-equella/testData/filestores/direct-run1/Institutions");
		TestUtils.addProp(props, "direct.num.items.per.query", "200");
		TestUtils.addProp(props, "direct.filter.by.collection.id", "523423432");
		TestUtils.addProp(props, "filter.by.institution.shortname", "vanilla");
		TestUtils
				.addProp(
						props,
						"test.data.directory",
						"..../ping-equella/testData/");
		TestUtils.addProp(props, "email.report",
				"ONLY_NEW_MISSING_ATTACHMENTS_OR_ERRORS");
		TestUtils.addProp(props, "email.smtp.server.port", "587");
		TestUtils.addProp(props, "email.smtp.server", "smtp.gmail.com");
		TestUtils.addProp(props, "email.sender.display.name",
				"ChrisB testEmailSettingsFailFastWithNormalEmailSpecified");
		TestUtils.addProp(props, "email.sender.username",
				System.getProperty("JUNIT_EMAIL_USERNAME"));
		TestUtils.addProp(props, "email.sender.password",
				System.getProperty("JUNIT_EMAIL_PASSWORD"));
		TestUtils.addProp(props, "email.recipients ",
				System.getProperty("JUNIT_EMAIL_SINGLE_TO_LIST"));

		assertTrue(PingEquellaDriver.setup(props.getAbsolutePath()));
		assertFalse(PingEquellaDriver.run());
		ReportManager.getInstance().failFast();
	}

	@Test
	public void testEmailReportMissing() {
		File props = TestUtils.createTempProperties();
		TestUtils.buildBasicDirectProps(props);
		TestUtils.addProp(props, "email.smtp.server.port", "587");
		TestUtils.addProp(props, "email.smtp.server", "smtp.gmail.com");
		TestUtils.addProp(props, "email.sender.display.name",
				"testEmailSettingsE2EonlyChangedAtts");
		TestUtils.addProp(props, "email.sender.username",
				System.getProperty("JUNIT_EMAIL_USERNAME"));
		TestUtils.addProp(props, "email.sender.password",
				System.getProperty("JUNIT_EMAIL_PASSWORD"));
		TestUtils.addProp(props, "email.recipients ",
				System.getProperty("JUNIT_EMAIL_SINGLE_TO_LIST"));

		assertTrue(PingEquellaDriver.setup(props.getAbsolutePath()));
		Email em = new Email();
		em.send("This", "should", "not work");
		if (ReportManager.getInstance().hasFatalErrors()) {
			assertEquals(
					"Unable to send email.  Email settings not configured.",
					ReportManager.getInstance().getLastFatalError());
		}
	}

	@Test
	public void testEmailReportBlank() {
		File props = TestUtils.createTempProperties();
		TestUtils.buildBasicDirectProps(props);
		TestUtils.addProp(props, "email.report", "");
		TestUtils.addProp(props, "email.smtp.server.port", "587");
		TestUtils.addProp(props, "email.smtp.server", "smtp.gmail.com");
		TestUtils.addProp(props, "email.sender.display.name",
				"testEmailSettingsE2EonlyChangedAtts");
		TestUtils.addProp(props, "email.sender.username",
				System.getProperty("JUNIT_EMAIL_USERNAME"));
		TestUtils.addProp(props, "email.sender.password",
				System.getProperty("JUNIT_EMAIL_PASSWORD"));
		TestUtils.addProp(props, "email.recipients ",
				System.getProperty("JUNIT_EMAIL_SINGLE_TO_LIST"));

		assertTrue(PingEquellaDriver.setup(props.getAbsolutePath()));
		Email em = new Email();
		em.send("This", "should", "not work");
		if (ReportManager.getInstance().hasFatalErrors()) {
			assertEquals(
					"Unable to send email.  Email settings not configured.",
					ReportManager.getInstance().getLastFatalError());
		}
	}

	@Test
	public void testEmailReportBadValue() {
		File props = TestUtils.createTempProperties();
		TestUtils.buildBasicDirectProps(props);
		TestUtils.addProp(props, "email.report", "bugger");
		TestUtils.addProp(props, "email.smtp.server.port", "587");
		TestUtils.addProp(props, "email.smtp.server", "smtp.gmail.com");
		TestUtils.addProp(props, "email.sender.display.name",
				"testEmailSettingsE2EonlyChangedAtts");
		TestUtils.addProp(props, "email.sender.username",
				System.getProperty("JUNIT_EMAIL_USERNAME"));
		TestUtils.addProp(props, "email.sender.password",
				System.getProperty("JUNIT_EMAIL_PASSWORD"));
		TestUtils.addProp(props, "email.recipients ",
				System.getProperty("JUNIT_EMAIL_SINGLE_TO_LIST"));

		assertFalse(PingEquellaDriver.setup(props.getAbsolutePath()));
		assertFalse(Config.getInstance().isSetupValid());
		assertEquals(
				"Config init should have failed due to bad email.report",
				Config.getInstance().getSetupInvalidReason(),
				String.format(
						"Unable to use %s: email.report must be 'NONE', 'NORMAL', 'ONLY_NEW_MISSING_ATTACHMENTS_OR_ERRORS', or unspecified / empty.",
						props));
	}

	@Test
	public void testEmailReportNone() {
		File props = TestUtils.createTempProperties();
		TestUtils.buildBasicDirectProps(props);
		TestUtils.addProp(props, "email.report", "NONE");
		TestUtils.addProp(props, "email.smtp.server.port", "587");
		TestUtils.addProp(props, "email.smtp.server", "smtp.gmail.com");
		TestUtils.addProp(props, "email.sender.display.name",
				"testEmailSettingsE2EonlyChangedAtts");
		TestUtils.addProp(props, "email.sender.username",
				System.getProperty("JUNIT_EMAIL_USERNAME"));
		TestUtils.addProp(props, "email.sender.password",
				System.getProperty("JUNIT_EMAIL_PASSWORD"));
		TestUtils.addProp(props, "email.recipients ",
				System.getProperty("JUNIT_EMAIL_SINGLE_TO_LIST"));

		assertTrue(PingEquellaDriver.setup(props.getAbsolutePath()));
		Email em = new Email();
		em.send("This", "should", "not work");
		if (ReportManager.getInstance().hasFatalErrors()) {
			assertEquals(
					"Unable to send email.  Email settings not configured.",
					ReportManager.getInstance().getLastFatalError());
		}
	}

	@Test
	public void testEmailSenderUsernameMissing() {
		File props = TestUtils.createTempProperties();
		TestUtils.buildBasicDirectProps(props);
		TestUtils.addProp(props, "email.report", "NORMAL");
		TestUtils.addProp(props, "email.smtp.server.port", "587");
		TestUtils.addProp(props, "email.smtp.server", "smtp.gmail.com");
		TestUtils.addProp(props, "email.sender.display.name",
				"testEmailSettingsE2EonlyChangedAtts");
		TestUtils.addProp(props, "email.sender.password",
				System.getProperty("JUNIT_EMAIL_PASSWORD"));
		TestUtils.addProp(props, "email.recipients ",
				System.getProperty("JUNIT_EMAIL_SINGLE_TO_LIST"));

		assertFalse(PingEquellaDriver.setup(props.getAbsolutePath()));
		assertFalse(Config.getInstance().isSetupValid());
		assertEquals(
				"Config init should have failed",
				Config.getInstance().getSetupInvalidReason(),
				String.format(
						"Unable to use %s: Property [email.sender.username] is required and was either not found or empty.",
						props));
	}

	@Test
	public void testEmailSenderUsernameEmpty() {
		File props = TestUtils.createTempProperties();
		TestUtils.buildBasicDirectProps(props);
		TestUtils.addProp(props, "email.report", "NORMAL");
		TestUtils.addProp(props, "email.smtp.server.port", "587");
		TestUtils.addProp(props, "email.smtp.server", "smtp.gmail.com");
		TestUtils.addProp(props, "email.sender.display.name",
				"testEmailSettingsE2EonlyChangedAtts");
		TestUtils.addProp(props, "email.sender.username", "");
		TestUtils.addProp(props, "email.sender.password",
				System.getProperty("JUNIT_EMAIL_PASSWORD"));
		TestUtils.addProp(props, "email.recipients ",
				System.getProperty("JUNIT_EMAIL_SINGLE_TO_LIST"));

		assertFalse(PingEquellaDriver.setup(props.getAbsolutePath()));
		assertFalse(Config.getInstance().isSetupValid());
		assertEquals(
				"Config init should have failed",
				Config.getInstance().getSetupInvalidReason(),
				String.format(
						"Unable to use %s: Property [email.sender.username] is required and was either not found or empty.",
						props));
	}

	@Test
	public void testEmailSenderPasswordMissing() {
		File props = TestUtils.createTempProperties();
		TestUtils.buildBasicDirectProps(props);
		TestUtils.addProp(props, "email.report", "NORMAL");
		TestUtils.addProp(props, "email.smtp.server.port", "587");
		TestUtils.addProp(props, "email.smtp.server", "smtp.gmail.com");
		TestUtils.addProp(props, "email.sender.display.name",
				"testEmailSettingsE2EonlyChangedAtts");
		TestUtils.addProp(props, "email.sender.username",
				System.getProperty("JUNIT_EMAIL_USERNAME"));
		TestUtils.addProp(props, "email.recipients ",
				System.getProperty("JUNIT_EMAIL_SINGLE_TO_LIST"));

		assertFalse(PingEquellaDriver.setup(props.getAbsolutePath()));
		assertFalse(Config.getInstance().isSetupValid());
		assertEquals(
				"Config init should have failed",
				Config.getInstance().getSetupInvalidReason(),
				String.format(
						"Unable to use %s: Property [email.sender.password] is required and was either not found or empty.",
						props));
	}

	@Test
	public void testEmailSenderPasswordEmpty() {
		File props = TestUtils.createTempProperties();
		TestUtils.buildBasicDirectProps(props);
		TestUtils.addProp(props, "email.report", "NORMAL");
		TestUtils.addProp(props, "email.smtp.server.port", "587");
		TestUtils.addProp(props, "email.smtp.server", "smtp.gmail.com");
		TestUtils.addProp(props, "email.sender.display.name",
				"testEmailSettingsE2EonlyChangedAtts");
		TestUtils.addProp(props, "email.sender.username",
				System.getProperty("JUNIT_EMAIL_USERNAME"));
		TestUtils.addProp(props, "email.sender.password", "");
		TestUtils.addProp(props, "email.recipients ",
				System.getProperty("JUNIT_EMAIL_SINGLE_TO_LIST"));

		assertFalse(PingEquellaDriver.setup(props.getAbsolutePath()));
		assertFalse(Config.getInstance().isSetupValid());
		assertEquals(
				"Config init should have failed",
				Config.getInstance().getSetupInvalidReason(),
				String.format(
						"Unable to use %s: Property [email.sender.password] is required and was either not found or empty.",
						props));
	}

	@Test
	public void testEmailSenderDisplayNameMissing() {
		File props = TestUtils.createTempProperties();
		TestUtils.buildBasicDirectProps(props);
		TestUtils.addProp(props, "email.report", "NORMAL");
		TestUtils.addProp(props, "email.smtp.server.port", "587");
		TestUtils.addProp(props, "email.smtp.server", "smtp.gmail.com");
		TestUtils.addProp(props, "email.sender.username",
				System.getProperty("JUNIT_EMAIL_USERNAME"));
		TestUtils.addProp(props, "email.sender.password",
				System.getProperty("JUNIT_EMAIL_PASSWORD"));
		TestUtils.addProp(props, "email.recipients ",
				System.getProperty("JUNIT_EMAIL_SINGLE_TO_LIST"));

		assertFalse(PingEquellaDriver.setup(props.getAbsolutePath()));
		assertFalse(Config.getInstance().isSetupValid());
		assertEquals(
				"Config init should have failed",
				Config.getInstance().getSetupInvalidReason(),
				String.format(
						"Unable to use %s: Property [email.sender.display.name] is required and was either not found or empty.",
						props));
	}

	@Test
	public void testEmailSenderDisplayNameEmpty() {
		File props = TestUtils.createTempProperties();
		TestUtils.buildBasicDirectProps(props);
		TestUtils.addProp(props, "email.report", "NORMAL");
		TestUtils.addProp(props, "email.smtp.server.port", "587");
		TestUtils.addProp(props, "email.smtp.server", "smtp.gmail.com");
		TestUtils.addProp(props, "email.sender.display.name", "");
		TestUtils.addProp(props, "email.sender.username",
				System.getProperty("JUNIT_EMAIL_USERNAME"));
		TestUtils.addProp(props, "email.sender.password",
				System.getProperty("JUNIT_EMAIL_PASSWORD"));
		TestUtils.addProp(props, "email.recipients ",
				System.getProperty("JUNIT_EMAIL_SINGLE_TO_LIST"));

		assertFalse(PingEquellaDriver.setup(props.getAbsolutePath()));
		assertFalse(Config.getInstance().isSetupValid());
		assertEquals(
				"Config init should have failed",
				Config.getInstance().getSetupInvalidReason(),
				String.format(
						"Unable to use %s: Property [email.sender.display.name] is required and was either not found or empty.",
						props));
	}

	@Test
	public void testEmailSmtpServerMissing() {
		File props = TestUtils.createTempProperties();
		TestUtils.buildBasicDirectProps(props);
		TestUtils.addProp(props, "email.report", "NORMAL");
		TestUtils.addProp(props, "email.smtp.server.port", "587");
		TestUtils.addProp(props, "email.sender.display.name",
				"testEmailSettingsE2EonlyChangedAtts");
		TestUtils.addProp(props, "email.sender.username",
				System.getProperty("JUNIT_EMAIL_USERNAME"));
		TestUtils.addProp(props, "email.sender.password",
				System.getProperty("JUNIT_EMAIL_PASSWORD"));
		TestUtils.addProp(props, "email.recipients ",
				System.getProperty("JUNIT_EMAIL_SINGLE_TO_LIST"));

		assertFalse(PingEquellaDriver.setup(props.getAbsolutePath()));
		assertFalse(Config.getInstance().isSetupValid());
		assertEquals(
				"Config init should have failed",
				Config.getInstance().getSetupInvalidReason(),
				String.format(
						"Unable to use %s: Property [email.smtp.server] is required and was either not found or empty.",
						props));
	}

	@Test
	public void testEmailSmtpServerEmpty() {
		File props = TestUtils.createTempProperties();
		TestUtils.buildBasicDirectProps(props);
		TestUtils.addProp(props, "email.report", "NORMAL");
		TestUtils.addProp(props, "email.smtp.server.port", "587");
		TestUtils.addProp(props, "email.smtp.server", "");
		TestUtils.addProp(props, "email.sender.display.name",
				"testEmailSettingsE2EonlyChangedAtts");
		TestUtils.addProp(props, "email.sender.username",
				System.getProperty("JUNIT_EMAIL_USERNAME"));
		TestUtils.addProp(props, "email.sender.password",
				System.getProperty("JUNIT_EMAIL_PASSWORD"));
		TestUtils.addProp(props, "email.recipients ",
				System.getProperty("JUNIT_EMAIL_SINGLE_TO_LIST"));

		assertFalse(PingEquellaDriver.setup(props.getAbsolutePath()));
		assertFalse(Config.getInstance().isSetupValid());
		assertEquals(
				"Config init should have failed",
				Config.getInstance().getSetupInvalidReason(),
				String.format(
						"Unable to use %s: Property [email.smtp.server] is required and was either not found or empty.",
						props));
	}

	@Test
	public void testEmailSmtpServerPortMissing() {
		File props = TestUtils.createTempProperties();
		TestUtils.buildBasicDirectProps(props);
		TestUtils.addProp(props, "email.report", "NORMAL");
		TestUtils.addProp(props, "email.smtp.server", "smtp.gmail.com");
		TestUtils.addProp(props, "email.sender.display.name",
				"testEmailSettingsE2EonlyChangedAtts");
		TestUtils.addProp(props, "email.sender.username",
				System.getProperty("JUNIT_EMAIL_USERNAME"));
		TestUtils.addProp(props, "email.sender.password",
				System.getProperty("JUNIT_EMAIL_PASSWORD"));
		TestUtils.addProp(props, "email.recipients ",
				System.getProperty("JUNIT_EMAIL_SINGLE_TO_LIST"));

		assertFalse(PingEquellaDriver.setup(props.getAbsolutePath()));
		assertFalse(Config.getInstance().isSetupValid());
		assertEquals(
				"Config init should have failed",
				Config.getInstance().getSetupInvalidReason(),
				String.format(
						"Unable to use %s: Property [email.smtp.server.port] is required and was either not found or empty.",
						props));
	}

	@Test
	public void testEmailSmtpServerPortEmpty() {
		File props = TestUtils.createTempProperties();
		TestUtils.buildBasicDirectProps(props);
		TestUtils.addProp(props, "email.report", "NORMAL");
		TestUtils.addProp(props, "email.smtp.server.port", "");
		TestUtils.addProp(props, "email.smtp.server", "smtp.gmail.com");
		TestUtils.addProp(props, "email.sender.display.name",
				"testEmailSettingsE2EonlyChangedAtts");
		TestUtils.addProp(props, "email.sender.username",
				System.getProperty("JUNIT_EMAIL_USERNAME"));
		TestUtils.addProp(props, "email.sender.password",
				System.getProperty("JUNIT_EMAIL_PASSWORD"));
		TestUtils.addProp(props, "email.recipients ",
				System.getProperty("JUNIT_EMAIL_SINGLE_TO_LIST"));

		assertFalse(PingEquellaDriver.setup(props.getAbsolutePath()));
		assertFalse(Config.getInstance().isSetupValid());
		assertEquals(
				"Config init should have failed",
				Config.getInstance().getSetupInvalidReason(),
				String.format(
						"Unable to use %s: Property [email.smtp.server.port] is required and was either not found or empty.",
						props));
	}

	@Test
	public void testEmailRecipientsMissing() {
		File props = TestUtils.createTempProperties();
		TestUtils.buildBasicDirectProps(props);
		TestUtils.addProp(props, "email.report", "NORMAL");
		TestUtils.addProp(props, "email.smtp.server.port", "587");
		TestUtils.addProp(props, "email.smtp.server", "smtp.gmail.com");
		TestUtils.addProp(props, "email.sender.display.name",
				"testEmailSettingsE2EonlyChangedAtts");
		TestUtils.addProp(props, "email.sender.username",
				System.getProperty("JUNIT_EMAIL_USERNAME"));
		TestUtils.addProp(props, "email.sender.password",
				System.getProperty("JUNIT_EMAIL_PASSWORD"));

		assertFalse(PingEquellaDriver.setup(props.getAbsolutePath()));
		assertFalse(Config.getInstance().isSetupValid());
		assertEquals(
				"Config init should have failed",
				Config.getInstance().getSetupInvalidReason(),
				String.format(
						"Unable to use %s: Property [email.recipients] is required and was either not found or empty.",
						props));
	}

	@Test
	public void testEmailRecipientsEmpty() {
		File props = TestUtils.createTempProperties();
		TestUtils.buildBasicDirectProps(props);
		TestUtils.addProp(props, "email.report", "NORMAL");
		TestUtils.addProp(props, "email.smtp.server.port", "587");
		TestUtils.addProp(props, "email.smtp.server", "smtp.gmail.com");
		TestUtils.addProp(props, "email.sender.display.name",
				"testEmailSettingsE2EonlyChangedAtts");
		TestUtils.addProp(props, "email.sender.username",
				System.getProperty("JUNIT_EMAIL_USERNAME"));
		TestUtils.addProp(props, "email.sender.password",
				System.getProperty("JUNIT_EMAIL_PASSWORD"));
		TestUtils.addProp(props, "email.recipients ", "");

		assertFalse(PingEquellaDriver.setup(props.getAbsolutePath()));
		assertFalse(Config.getInstance().isSetupValid());
		assertEquals(
				"Config init should have failed",
				Config.getInstance().getSetupInvalidReason(),
				String.format(
						"Unable to use %s: Property [email.recipients] is required and was either not found or empty.",
						props));
	}
}
