package com.pearson.equella.support.ping.tests;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;

import org.junit.Test;

import com.pearson.equella.support.ping.PingEquellaDriver;
import com.pearson.equella.support.ping.report.ReportManager;
import com.pearson.equella.support.ping.utils.Config;
import com.pearson.equella.support.ping.utils.PingUtils;

public class ConfigTests {
	@Test
	public void testOutputFolderNeeded() {
		String props = "testData/props/empty-props.properties";
		assertFalse(PingEquellaDriver.setup(props));
		assertFalse(Config.getInstance().isSetupValid());
		assertEquals(
				"Config init should have failed due to missing output.folder",
				Config.getInstance().getSetupInvalidReason(),
				String.format(
						"Unable to use %s: Property [output.folder] is required and was either not found or empty.",
						props));
	}

	@Test
	public void testClientNameNeeded() {
		String props = "testData/props/missing-client-name.properties";
		assertFalse(PingEquellaDriver.setup(props));
		assertFalse(Config.getInstance().isSetupValid());
		assertEquals(
				"Config init should have failed due to missing client.name",
				Config.getInstance().getSetupInvalidReason(),
				String.format(
						"Unable to use %s: Property [client.name] is required and was either not found or empty.",
						props));
	}

	@Test
	public void testPingTypeNeeded() {
		String props = "testData/props/missing-ping-type.properties";
		assertFalse(PingEquellaDriver.setup(props));
		assertFalse(Config.getInstance().isSetupValid());
		assertEquals(
				"Config init should have failed due to missing ping.type",
				Config.getInstance().getSetupInvalidReason(),
				String.format(
						"Unable to use %s: Property [ping.type] is required and was either not found or empty.",
						props));
	}

	@Test
	public void testBadPingType() {
		String props = "testData/props/bad-ping-type.properties";
		assertFalse(PingEquellaDriver.setup(props));
		assertFalse(Config.getInstance().isSetupValid());
		assertEquals(
				"Config init should have failed due to bad ping.type",
				Config.getInstance().getSetupInvalidReason(),
				String.format(
						"Unable to use %s: ping.type must be 'attachments', 'direct-query-all-items-all-attachments', or 'direct-query-batched-items-attachments-per-item'.",
						props));
	}

	@Test
	public void testHappyPathAttachments() {
		String props = "testData/props/happy-path-attachments.properties";
		assertTrue(PingEquellaDriver.setup(props));
		assertTrue(Config.getInstance().isSetupValid());
		assertEquals("Config init should have passed", Config.getInstance()
				.getSetupInvalidReason(), "");
	}

	@Test
	public void testHappyPathDirect() {
		String props = "testData/props/happy-path-direct.properties";
		assertTrue(PingEquellaDriver.setup(props));
		assertTrue(Config.getInstance().isSetupValid());
		assertTrue("Config init should have passed", Config.getInstance()
				.getSetupInvalidReason().isEmpty());
	}

	@Test
	public void testMissingDirectDbUrl() {
		String props = "testData/props/missing-direct-db-url.properties";
		assertFalse(PingEquellaDriver.setup(props));
		assertFalse(Config.getInstance().isSetupValid());
		assertEquals(
				"Config init should have failed due to missing direct.db.url",
				Config.getInstance().getSetupInvalidReason(),
				String.format(
						"Unable to use %s: Property [direct.db.url] is required and was either not found or empty.",
						props));
	}

	@Test
	public void testFilterByCollectionIdAlpha() {
		String props = "testData/props/direct-filter-by-collection-id-alpha.properties";
		assertFalse(PingEquellaDriver.setup(props));
		assertFalse(Config.getInstance().isSetupValid());
		assertEquals(
				"Config init should have failed due to alpha direct.filter.by.collection.id",
				Config.getInstance().getSetupInvalidReason(),
				String.format(
						"Unable to use %s: direct.filter.by.collection.id parsed as [five], but should be a number.",
						props));
	}

	@Test
	public void testFilterByCollectionIdLT1() {
		String props = "testData/props/direct-filter-by-collection-id-lt-1.properties";
		assertFalse(PingEquellaDriver.setup(props));
		assertFalse(Config.getInstance().isSetupValid());
		assertEquals(
				"Config init should have failed due to less than 1 direct.filter.by.collection.id",
				Config.getInstance().getSetupInvalidReason(),
				String.format(
						"Unable to use %s: direct.filter.by.collection.id parsed as [0], but should be greater than 0.",
						props));
	}

	@Test
	public void testMissingDirectDbUsername() {
		String props = "testData/props/missing-direct-db-username.properties";
		assertFalse(PingEquellaDriver.setup(props));
		assertFalse(Config.getInstance().isSetupValid());
		assertEquals(
				"Config init should have failed due to missing direct.db.username",
				Config.getInstance().getSetupInvalidReason(),
				String.format(
						"Unable to use %s: Property [direct.db.username] is required and was either not found or empty.",
						props));
	}

	@Test
	public void testMissingDirectDbPassword() {
		String props = "testData/props/missing-direct-db-password.properties";
		assertFalse(PingEquellaDriver.setup(props));
		assertFalse(Config.getInstance().isSetupValid());
		assertEquals(
				"Config init should have failed due to missing direct.db.password",
				Config.getInstance().getSetupInvalidReason(),
				String.format(
						"Unable to use %s: Property [direct.db.password] is required and was either not found or empty.",
						props));
	}

	@Test
	public void testMissingDirectDbType() {
		String props = "testData/props/missing-direct-db-type.properties";
		assertFalse(PingEquellaDriver.setup(props));
		assertFalse(Config.getInstance().isSetupValid());
		assertEquals(
				"Config init should have failed due to missing direct.db.type",
				Config.getInstance().getSetupInvalidReason(),
				String.format(
						"Unable to use %s: Property [direct.db.type] is required and was either not found or empty.",
						props));
	}

	@Test
	public void testDirectFilestoreDirMissing() {
		String props = "testData/props/direct-filestore-dir-missing.properties";
		assertFalse(PingEquellaDriver.setup(props));
		assertFalse(Config.getInstance().isSetupValid());
		assertEquals(
				"Config init should have failed due to missing direct.filestore.dir",
				Config.getInstance().getSetupInvalidReason(),
				String.format(
						"Unable to use %s: Property [direct.filestore.dir] is required and was either not found or empty.",
						props));
	}

	@Test
	public void testDirectFilestoreDirNotInstitutionsEndPoint() {
		String props = "testData/props/direct-filestore-dir-not-institutions-endpoint.properties";
		assertFalse(PingEquellaDriver.setup(props));
		assertFalse(Config.getInstance().isSetupValid());
		assertEquals(
				"Config init should have failed due to bad direct.filestore.dir",
				Config.getInstance().getSetupInvalidReason(),
				String.format(
						"Unable to use %s: Filestore directory /asdf/wef/qwef/as must end in 'Institutions'",
						props));
	}

	@Test
	public void testDirectFilestoreDirDoesNotExist() {
		String props = "testData/props/direct-filestore-dir-does-not-exist.properties";
		assertFalse(PingEquellaDriver.setup(props));
		assertFalse(Config.getInstance().isSetupValid());
		assertEquals(
				"Config init should have failed due to non-existant direct.filestore.dir",
				Config.getInstance().getSetupInvalidReason(),
				String.format(
						"Unable to use %s: Filestore Institutions directory /asdf/wef/qwef/as/Institutions must exist",
						props));
	}

	@Test
	public void testDirectFilestoreDirMustBeADirectory() {
		String props = "testData/props/direct-filestore-dir-must-be-a-directory.properties";
		assertFalse(PingEquellaDriver.setup(props));
		assertFalse(Config.getInstance().isSetupValid());
		assertEquals(
				"Config init should have failed due to file specified for direct.filestore.dir",
				Config.getInstance().getSetupInvalidReason(),
				String.format(
						"Unable to use %s: Filestore Institutions 'directory' ...../ping-equella/testData/misc/Institutions must be a directory",
						props));
	}

	@Test
	public void testMissingClientId() {
		String props = "testData/props/missing-client-id.properties";
		assertFalse(PingEquellaDriver.setup(props));
		assertFalse(Config.getInstance().isSetupValid());
		assertEquals(
				"Config init should have failed due to missing client.id",
				Config.getInstance().getSetupInvalidReason(),
				String.format(
						"Unable to use %s: Property [client.id] is required and was either not found or empty.",
						props));
	}

	@Test
	public void testMissingClientSecret() {
		String props = "testData/props/missing-client-secret.properties";
		assertFalse(PingEquellaDriver.setup(props));
		assertFalse(Config.getInstance().isSetupValid());
		assertEquals(
				"Config init should have failed due to missing client.secret",
				Config.getInstance().getSetupInvalidReason(),
				String.format(
						"Unable to use %s: Property [client.secret] is required and was either not found or empty.",
						props));
	}

	@Test
	public void testMissingPingMaxTries() {
		String props = "testData/props/missing-ping-max-tries.properties";
		assertFalse(PingEquellaDriver.setup(props));
		assertFalse(Config.getInstance().isSetupValid());
		assertEquals(
				"Config init should have failed due to missing ping.max.tries",
				Config.getInstance().getSetupInvalidReason(),
				String.format(
						"Unable to use %s: Property [ping.max.tries] is required and was either not found or empty.",
						props));
	}

	@Test
	public void testMissingpingTestTimesToInjectTimeout() {
		String props = "testData/props/missing-test-times-to-inject-timeout.properties";
		assertFalse(PingEquellaDriver.setup(props));
		assertFalse(Config.getInstance().isSetupValid());
		assertEquals(
				"Config init should have failed due to missing ping.test.times.to.inject.timeout",
				Config.getInstance().getSetupInvalidReason(),
				String.format(
						"Unable to use %s: Property [ping.test.times.to.inject.timeout] is required and was either not found or empty.",
						props));
	}

	@Test
	public void testBadDirectDbType() {
		String props = "testData/props/bad-direct-db-type.properties";
		assertFalse(PingEquellaDriver.setup(props));
		assertFalse(Config.getInstance().isSetupValid());
		assertEquals(
				"Config init should have failed due to bad direct.db.type",
				Config.getInstance().getSetupInvalidReason(),
				String.format(
						"Unable to use %s: direct.db.type must be 'SQLSERVER'.",
						props));
	}

	@Test
	public void testDirectNumItemsInQueryConfirmIgnoreMissingWhenAllItemsAreQueried() {
		String props = "testData/props/direct-num-items-in-query-confirm-ignore-when-all-items-are-queried.properties";
		assertTrue(PingEquellaDriver.setup(props));
		assertTrue(Config.getInstance().isSetupValid());
		assertEquals("Config init should be valid", Config.getInstance()
				.getSetupInvalidReason(), "");
	}

	@Test
	public void testDirectNumItemsInQueryMissingWhenItemsAreBatched() {
		String props = "testData/props/direct-num-items-in-query-missing-when-items-are-batched.properties";
		assertFalse(PingEquellaDriver.setup(props));
		assertFalse(Config.getInstance().isSetupValid());
		assertEquals(
				"Config init should have failed due to missing direct.num.items.per.query",
				Config.getInstance().getSetupInvalidReason(),
				String.format(
						"Unable to use %s: Property [direct.num.items.per.query] is required and was either not found or empty.",
						props));
	}

	@Test
	public void testDirectNumItemsInQueryAlphaWhenItemsAreBatched() {
		String props = "testData/props/direct-num-items-in-query-alpha-when-items-are-batched.properties";
		assertFalse(PingEquellaDriver.setup(props));
		assertFalse(Config.getInstance().isSetupValid());
		assertEquals(
				"Config init should have failed due to alpha direct.num.items.per.query",
				Config.getInstance().getSetupInvalidReason(),
				String.format(
						"Unable to use %s: Property [direct.num.items.per.query] should be a number.",
						props));
	}

	@Test
	public void testDirectNumItemsInQueryLowWhenItemsAreBatched() {
		String props = "testData/props/direct-num-items-in-query-low-when-items-are-batched.properties";
		assertFalse(PingEquellaDriver.setup(props));
		assertFalse(Config.getInstance().isSetupValid());
		assertEquals(
				"Config init should have failed due to low direct.num.items.per.query",
				Config.getInstance().getSetupInvalidReason(),
				String.format(
						"Unable to use %s: Property [direct.num.items.per.query] should be above 1.",
						props));
	}

	@Test
	public void testInstitutionHandleAndShortnameEqualAttsDoNotExist() {
		assertTrue(PingEquellaDriver
				.setup("testData/props/direct-institutions-cache-handle-and-shortname-equal-atts-do-not-exist.properties"));
		assertFalse(PingEquellaDriver.run());
		assertTrue(ReportManager.getInstance().hasFatalErrors());
		int lastIdx = ReportManager.getInstance().getFatalErrors().size() - 1;
		assertEquals(
				ReportManager.getInstance().getFatalErrors().get(lastIdx),
				"Institution [vanilla] filestore attachments directory "
						+ "[..../ping-equella/testData/filestores/"
						+ "testInstitutionHandleAndShortnameEqualAttsDoNotExist/Institutions/vanilla/Attachments] does not exist.");

	}

	@Test
	public void testInstitutionHandleAndShortnameDiffConfigMissing() {
		assertTrue(PingEquellaDriver
				.setup("testData/props/direct-institutions-cache-handle-and-shortname-diff-config-missing.properties"));
		assertFalse(PingEquellaDriver.run());
		assertTrue(ReportManager.getInstance().hasFatalErrors());
		int lastIdx = ReportManager.getInstance().getFatalErrors().size() - 1;
		assertEquals(
				ReportManager.getInstance().getFatalErrors().get(lastIdx),
				"Institution [vanilla] filestore handle is different than shortname, but is not specified in the properties.");

	}

	@Test
	public void testInstitutionHandleAndShortnameDiffConfigEmpty() {
		assertTrue(PingEquellaDriver
				.setup("testData/props/direct-institutions-cache-handle-and-shortname-diff-config-empty.properties"));
		assertFalse(PingEquellaDriver.run());
		assertTrue(ReportManager.getInstance().hasFatalErrors());
		int lastIdx = ReportManager.getInstance().getFatalErrors().size() - 1;
		assertEquals(
				ReportManager.getInstance().getFatalErrors().get(lastIdx),
				"Institution [vanilla] filestore handle is different than shortname, but is not specified in the properties.");

	}

	@Test
	public void testInstitutionHandleAndShortnameDiffConfigBadHandle() {
		assertTrue(PingEquellaDriver
				.setup("testData/props/direct-institutions-cache-handle-and-shortname-diff-config-bad-handle.properties"));
		assertFalse(PingEquellaDriver.run());
		assertTrue(ReportManager.getInstance().hasFatalErrors());
		int lastIdx = ReportManager.getInstance().getFatalErrors().size() - 1;
		assertEquals(
				ReportManager.getInstance().getFatalErrors().get(lastIdx),
				"Institution [vanilla] filestore handle is different than shortname, but the handle (directory) specified in the properties [swirl] does not exist.");

	}

	@Test
	public void testInstitutionHandleAndShortnameDiffConfigAttsDoNotExist() {
		assertTrue(PingEquellaDriver
				.setup("testData/props/direct-institutions-cache-handle-and-shortname-diff-config-atts-do-not-exist.properties"));
		assertFalse(PingEquellaDriver.run());
		assertTrue(ReportManager.getInstance().hasFatalErrors());
		int lastIdx = ReportManager.getInstance().getFatalErrors().size() - 1;
		assertEquals(
				ReportManager.getInstance().getFatalErrors().get(lastIdx),
				"Institution [vanilla] filestore attachments directory [...../ping-equella/testData/filestores/testInstitutionHandleAndShortnameDiffConfigAttsDoNotExist/Institutions/swirl/Attachments] does not exist.");

	}

	@Test
	public void testCompareMissingAttachmentsDoNotExist() {
		File props = TestUtils.createTempProperties();
		TestUtils.buildBasicDirectProps(props);
		assertTrue(PingEquellaDriver.setup(props.getAbsolutePath()));
		assertFalse(Config.getInstance().isCompareMissingAttachmentsEnabled());
	}
	
	@Test
	public void testCompareMissingAttachmentsIsEmpty() {
		File props = TestUtils.createTempProperties();
		TestUtils.buildBasicDirectProps(props);
		TestUtils.addProp(props, "compare.missing.attachments", "");
		assertTrue(PingEquellaDriver.setup(props.getAbsolutePath()));
		assertFalse(Config.getInstance().isCompareMissingAttachmentsEnabled());
	}

	@Test
	public void testCompareMissingAttachmentsIsNotTrue() {
		File props = TestUtils.createTempProperties();
		TestUtils.buildBasicDirectProps(props);
		TestUtils.addProp(props, "compare.missing.attachments", "asd");
		assertTrue(PingEquellaDriver.setup(props.getAbsolutePath()));
		assertFalse(Config.getInstance().isCompareMissingAttachmentsEnabled());
	}
	
	@Test
	public void testCompareMissingAttachmentsIsTrue() {
		File props = TestUtils.createTempProperties();
		TestUtils.buildBasicDirectProps(props);
		TestUtils.addProp(props, "compare.missing.attachments", "TrUe");
		assertTrue(PingEquellaDriver.setup(props.getAbsolutePath()));
		assertTrue(Config.getInstance().isCompareMissingAttachmentsEnabled());
	}
}
