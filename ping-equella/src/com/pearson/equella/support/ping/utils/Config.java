package com.pearson.equella.support.ping.utils;

import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Config {
	public static final String PING_TYPE_ATTS = "attachments";
	public static final String PING_TYPE_DIRECT_ALL_ITEMS_ALL_ATTS = "direct-query-all-items-all-attachments";
	public static final String PING_TYPE_DIRECT_BATCH_ITEMS_PERITEM_ATTS = "direct-query-batched-items-attachments-per-item";
	public static final String DIRECT_DB_TYPE_SQLSERVER = "SQLSERVER";

	public enum EmailReport {
		NONE, NORMAL, ONLY_NEW_MISSING_ATTACHMENTS_OR_ERRORS
	}

	private static final Logger logger = LogManager.getLogger(Config.class);

	private Properties props;
	private boolean setupValid = false;
	private String setupInvalidReason = "";
	private static Config config = null;

	// Required
	private String outputFolder;
	private String pingType;
	private String clientName;

	// Required if ping type is attachments
	private String institutionUrl;
	private String clientId;
	private String clientSecret;
	private int maxTries;
	private int testTimesToInjectTimeout;

	// Required if ping type is direct*
	private String filestoreDir;
	private String databaseUrl;
	private String databaseUsername;
	private String databasePassword;
	private String databaseType;
	private int numItemsPerQuery = Integer.MIN_VALUE;

	// Optional
	private int directFilterByCollectionId = Integer.MIN_VALUE;
	private String attachmentsFilterByCollectionUuid = "";
	private String attachmentsInstitutionShortname;

	// Default is false
	private boolean compareMissingAttachmentsEnabled = false;

	// Email configs
	EmailReport emailReport = EmailReport.NONE;
	// Optional, and only works for direct* ping.types
	private String filterByInstitutionShortname = null;

	private String testDataDir = "";
	private String emailSenderUsername;
	private String emailSenderPassword;
	private String emailSenderDisplayName;
	private String emailSmtpServer;
	private String emailSmtpServerPort;
	private String emailRecipients;

	private Config() {

	}

	public static void reset() {
		config = null;
	}

	public static Config getInstance() {
		if (config == null) {
			config = new Config();
		}
		return config;
	}

	private String getProp(String key, boolean required) throws Exception {
		String val = props.getProperty(key);
		if (required && ((val == null) || val.isEmpty())) {
			throw new Exception(
					String.format(
							"Property [%s] is required and was either not found or empty.",
							key));
		}

		if (val == null) {
			val = "";
		}
		if (key.contains("password")) {
			logger.info(String.format("Using config %s with length of [%d]",
					key, val.length()));
		} else {
			logger.info(String.format("Using config %s as [%s]", key, val));
		}
		return val;
	}

	public boolean isSetupValid() {
		return setupValid;
	}

	public String getSetupInvalidReason() {
		return setupInvalidReason;
	}

	public void setup(String propertiesFilename) {
		props = new Properties();
		try {
			props.load(new FileInputStream(propertiesFilename));

			outputFolder = getProp("output.folder", true);
			clientName = getProp("client.name", true);
			pingType = getProp("ping.type", true);
			if (!pingType.equals(PING_TYPE_ATTS)
					&& !pingType.equals(PING_TYPE_DIRECT_ALL_ITEMS_ALL_ATTS)
					&& !pingType
							.equals(PING_TYPE_DIRECT_BATCH_ITEMS_PERITEM_ATTS)) {
				throw new Exception(String.format(
						"ping.type must be '%s', '%s', or '%s'.",
						PING_TYPE_ATTS, PING_TYPE_DIRECT_ALL_ITEMS_ALL_ATTS,
						PING_TYPE_DIRECT_BATCH_ITEMS_PERITEM_ATTS));
			}

			if (pingType.equals(PING_TYPE_DIRECT_ALL_ITEMS_ALL_ATTS)
					|| pingType
							.equals(PING_TYPE_DIRECT_BATCH_ITEMS_PERITEM_ATTS)) {
				String filterByCollIdStr = getProp(
						"direct.filter.by.collection.id", false);
				if (!filterByCollIdStr.isEmpty()) {
					try {
						directFilterByCollectionId = Integer
								.parseInt(filterByCollIdStr);
					} catch (NumberFormatException e) {
						String msg = String
								.format("direct.filter.by.collection.id parsed as [%s], but should be a number.",
										filterByCollIdStr);
						logger.error(msg);
						throw new Exception(msg);
					}

					if (directFilterByCollectionId < 1) {
						String msg = String
								.format("direct.filter.by.collection.id parsed as [%d], but should be greater than 0.",
										directFilterByCollectionId);
						logger.error(msg);
						throw new Exception(msg);
					}
				}
			}

			attachmentsFilterByCollectionUuid = getProp(
					"attachments.filter.by.collection.uuid", false);

			filterByInstitutionShortname = getProp(
					"filter.by.institution.shortname", false);
			testDataDir = getProp("test.data.directory", false);

			if (pingType.equals(PING_TYPE_ATTS)) {
				institutionUrl = getProp("institution.url", true);
				clientId = getProp("client.id", true);
				clientSecret = getProp("client.secret", true);
				maxTries = Integer.parseInt(getProp("ping.max.tries", true));
				testTimesToInjectTimeout = Integer.parseInt(getProp(
						"ping.test.times.to.inject.timeout", true));
				attachmentsInstitutionShortname = getProp(
						"attachments.institution.shortname", false);
				if (attachmentsInstitutionShortname.isEmpty()) {
					attachmentsInstitutionShortname = "DEFAULT";
				}

			} else if (pingType.equals(PING_TYPE_DIRECT_ALL_ITEMS_ALL_ATTS)
					|| pingType
							.equals(PING_TYPE_DIRECT_BATCH_ITEMS_PERITEM_ATTS)) {
				databaseUrl = getProp("direct.db.url", true);
				databaseUsername = getProp("direct.db.username", true);
				databasePassword = getProp("direct.db.password", true);
				databaseType = getProp("direct.db.type", true);

				if (!databaseType.equals(DIRECT_DB_TYPE_SQLSERVER)) {
					throw new Exception(String.format(
							"direct.db.type must be '%s'.",
							DIRECT_DB_TYPE_SQLSERVER));
				}

				filestoreDir = getProp("direct.filestore.dir", true);
				File dir = new File(filestoreDir);
				if (!filestoreDir.endsWith("Institutions")) {
					String msg = String
							.format("Filestore directory %s must end in 'Institutions'",
									filestoreDir);
					logger.error(msg);
					throw new Exception(msg);
				} else if (!dir.exists()) {
					String msg = String.format(
							"Filestore Institutions directory %s must exist",
							filestoreDir);
					logger.error(msg);
					throw new Exception(msg);
				} else if (!dir.isDirectory()) {
					String msg = String
							.format("Filestore Institutions 'directory' %s must be a directory",
									filestoreDir);
					logger.error(msg);
					throw new Exception(msg);
				}

				if (pingType.equals(PING_TYPE_DIRECT_BATCH_ITEMS_PERITEM_ATTS)) {
					try {
						numItemsPerQuery = Integer.parseInt(getProp(
								"direct.num.items.per.query", true));
						if (numItemsPerQuery < 2) {
							throw new Exception(
									"Property [direct.num.items.per.query] should be above 1.");
						}
					} catch (NumberFormatException e) {
						logger.error(
								"direct.num.items.per.query parsed as {}, but should be a number.",
								getProp("direct.num.items.per.query", true));
						throw new Exception(
								"Property [direct.num.items.per.query] should be a number.");
					}
				}
			}

			// Compare last report
			String compareStr = getProp("compare.missing.attachments", false);
			if ((compareStr != null) && compareStr.equalsIgnoreCase("true")) {
				compareMissingAttachmentsEnabled = true;
			}

			// Email
			String emailReportStr = getProp("email.report", false);
			if (emailReportStr == null || emailReportStr.trim().isEmpty()) {
				// email.report wasn't specified. Default to NONE
				emailReport = EmailReport.NONE;
			} else {
				try {
					emailReport = EmailReport.valueOf(emailReportStr);
				} catch (IllegalArgumentException iae) {
					throw new Exception(
							String.format(
									"email.report must be '%s', '%s', '%s', or unspecified / empty.",
									EmailReport.NONE,
									EmailReport.NORMAL,
									EmailReport.ONLY_NEW_MISSING_ATTACHMENTS_OR_ERRORS));
				}
			}

			if (emailReport == EmailReport.NORMAL
					|| emailReport == EmailReport.ONLY_NEW_MISSING_ATTACHMENTS_OR_ERRORS) {
				emailSenderUsername = getProp("email.sender.username", true);
				emailSenderPassword = getProp("email.sender.password", true);
				emailSenderDisplayName = getProp("email.sender.display.name",
						true);
				emailSmtpServer = getProp("email.smtp.server", true);
				emailSmtpServerPort = getProp("email.smtp.server.port", true);
				emailRecipients = getProp("email.recipients", true);
			}

			setupValid = true;

		} catch (Exception e) {
			setupInvalidReason = String.format("Unable to use %s: %s",
					propertiesFilename, e.getMessage());
			logger.error(setupInvalidReason, e);
			setupValid = false;
		}
	}

	public String getInstitutionUrl() {
		return institutionUrl;
	}

	public String getOutputFolder() {
		return outputFolder;
	}

	public String getPingType() {
		return pingType;
	}

	public int getFilterByCollectionDirect() {
		return directFilterByCollectionId;
	}

	public String getFilterByCollectionForAttachments() {
		return attachmentsFilterByCollectionUuid;
	}

	public String getClientId() {
		return clientId;
	}

	public String getClientSecret() {
		return clientSecret;
	}

	public String getClientName() {
		return clientName;
	}

	public String getVersion() {
		return "1.6";
	}

	public int getMaxTries() {
		return maxTries;
	}

	public int getTestTimesToInjectTimeout() {
		return testTimesToInjectTimeout;
	}

	public String getDatabaseUrl() {
		return databaseUrl;
	}

	public String getDatabaseUsername() {
		return databaseUsername;
	}

	public String getDatabasePassword() {
		return databasePassword;
	}

	public String getDatabaseType() {
		return databaseType;
	}

	public String getFilestoreDir() {
		return filestoreDir;
	}

	public String getTestDataDir() {
		return testDataDir;
	}

	public String getEmailSenderUsername() {
		return emailSenderUsername;
	}

	public String getEmailRecipients() {
		return emailRecipients;
	}

	public String getEmailSenderPassword() {
		return emailSenderPassword;
	}

	public String getEmailSenderDisplayName() {
		return emailSenderDisplayName;
	}

	public String getEmailSmtpServer() {
		return emailSmtpServer;
	}

	public String getEmailSmtpServerPort() {
		return emailSmtpServerPort;
	}

	public EmailReport getEmailReport() {
		return emailReport;
	}

	public boolean isCompareMissingAttachmentsEnabled() {
		return compareMissingAttachmentsEnabled;
	}

	public boolean isEmailReportEnabled() {
		return false;
	}

	public int getNumItemsPerQuery() {
		return numItemsPerQuery;
	}

	public String getFilestoreHandle(String shortname) {
		return props.getProperty(String.format(
				"direct.filestore.institution.handle.%s", shortname));
	}

	public String getFilterByInstitutionShortname() {
		return filterByInstitutionShortname;
	}

	public String getAttachmentsInstitutionShortname() {
		return attachmentsInstitutionShortname;
	}
}
