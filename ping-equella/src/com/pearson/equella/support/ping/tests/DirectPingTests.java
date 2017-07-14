package com.pearson.equella.support.ping.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;

import org.junit.Test;

import com.pearson.equella.support.ping.PingEquellaDriver;
import com.pearson.equella.support.ping.report.ReportManager;
import com.pearson.equella.support.ping.utils.Config;
import com.pearson.equella.support.ping.utils.PingUtils;

public class DirectPingTests {
	@Test
	public void testRun1ItemsInSingleQuery() {
		try {
			assertTrue(PingEquellaDriver
					.setup("testData/props/e2e-run1-direct-items-in-single-query.properties"));
			assertTrue(PingEquellaDriver.run());
			assertTrue(PingEquellaDriver.finalizeRun());

			// ALL results
			File allResults = new File(ReportManager.getInstance()
					.getStdOutFilename());
			String filename = Config.getInstance().getTestDataDir()
					+ "/outputSamplesToCompare/ping_direct_junitTestsDirectRun1_ItemsInSingleQuery_all_stats_GOLD.csv";
			File allResultsToCompare = new File(filename);
			assertTrue(allResults.exists());
			assertTrue(allResultsToCompare.exists());
			assertTrue(PingUtils.mediumStrictCompare(allResults,
					allResultsToCompare));

			// ERR results
			File errResults = new File(ReportManager.getInstance()
					.getErrOutFilename());
			String errFilename = Config.getInstance().getTestDataDir()
					+ "/outputSamplesToCompare/ping_direct_junitTestsDirectRun1_ItemsInSingleQuery_error_stats_GOLD.csv";
			File errResultsToCompare = new File(errFilename);
			assertTrue(errResults.exists());
			assertTrue(errResultsToCompare.exists());
			assertTrue(PingUtils.mediumStrictCompare(errResults,
					errResultsToCompare));
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}

	/**
	 * An empty filter.by.institution.shortname will be ignored.
	 */
	@Test
	public void testRun1ItemsInSingleQueryInstitutionFilterEmpty() {
		try {
			assertTrue(PingEquellaDriver
					.setup("testData/props/e2e-run1-direct-items-in-single-query-institution-filter-empty.properties"));
			assertTrue(PingEquellaDriver.run());
			assertTrue(PingEquellaDriver.finalizeRun());

			// ALL results
			File allResults = new File(ReportManager.getInstance()
					.getStdOutFilename());
			String filename = Config.getInstance().getTestDataDir()
					+ "/outputSamplesToCompare/ping_direct_junitTestsDirectRun1_ItemsSingleQueryNoInstitutionFilter_all_stats_GOLD.csv";
			File allResultsToCompare = new File(filename);
			assertTrue(allResults.exists());
			assertTrue(allResultsToCompare.exists());
			assertTrue(PingUtils.mediumStrictCompare(allResults,
					allResultsToCompare));

			// ERR results
			File errResults = new File(ReportManager.getInstance()
					.getErrOutFilename());
			String errFilename = Config.getInstance().getTestDataDir()
					+ "/outputSamplesToCompare/ping_direct_junitTestsDirectRun1_ItemsSingleQueryNoInstitutionFilter_error_stats_GOLD.csv";
			File errResultsToCompare = new File(errFilename);
			assertTrue(errResults.exists());
			assertTrue(errResultsToCompare.exists());
			assertTrue(PingUtils.mediumStrictCompare(errResults,
					errResultsToCompare));
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}

	@Test
	public void testRun1ItemsInSingleQueryInstitutionFilterBad() {
		try {
			assertTrue(PingEquellaDriver
					.setup("testData/props/e2e-run1-direct-items-in-single-query-institution-filter-bad.properties"));
			assertFalse(PingEquellaDriver.run());
			assertTrue(ReportManager.getInstance().hasFatalErrors());
			assertEquals(
					"Utility should state the short to filter by is not in the cache.",
					ReportManager.getInstance().getFatalErrors().get(0),
					"The institution shortname to filter by [swirl] is not in the institution cache.");

		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}

	@Test
	public void testRun1ItemsBatchedBy1000() {
		try {
			assertTrue(PingEquellaDriver
					.setup("testData/props/e2e-run1-direct-items-batched-by-1000.properties"));
			assertTrue(PingEquellaDriver.run());
			assertTrue(PingEquellaDriver.finalizeRun());

			// ALL results
			File allResults = new File(ReportManager.getInstance()
					.getStdOutFilename());
			String filename = Config.getInstance().getTestDataDir()
					+ "/outputSamplesToCompare/ping_direct_junitTestsDirectRun1_ItemsBatchedBy16plus_all_stats_GOLD.csv";
			File allResultsToCompare = new File(filename);
			assertTrue(allResults.exists());
			assertTrue(allResultsToCompare.exists());
			assertTrue(PingUtils.mediumStrictCompare(allResults,
					allResultsToCompare));

			// ERR results
			File errResults = new File(ReportManager.getInstance()
					.getErrOutFilename());
			String errFilename = Config.getInstance().getTestDataDir()
					+ "/outputSamplesToCompare/ping_direct_junitTestsDirectRun1_ItemsBatchedBy16plus_error_stats_GOLD.csv";
			File errResultsToCompare = new File(errFilename);
			assertTrue(errResults.exists());
			assertTrue(errResultsToCompare.exists());
			assertTrue(PingUtils.mediumStrictCompare(errResults,
					errResultsToCompare));
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}

	/**
	 * 16 is the total number of items available
	 */
	@Test
	public void testRun1ItemsBatchedBy16() {
		try {
			assertTrue(PingEquellaDriver
					.setup("testData/props/e2e-run1-direct-items-batched-by-16.properties"));
			assertTrue(PingEquellaDriver.run());
			assertTrue(PingEquellaDriver.finalizeRun());

			// ALL results
			File allResults = new File(ReportManager.getInstance()
					.getStdOutFilename());
			String filename = Config.getInstance().getTestDataDir()
					+ "/outputSamplesToCompare/ping_direct_junitTestsDirectRun1_ItemsBatchedBy16plus_all_stats_GOLD.csv";
			File allResultsToCompare = new File(filename);
			assertTrue(allResults.exists());
			assertTrue(allResultsToCompare.exists());
			assertTrue(PingUtils.mediumStrictCompare(allResults,
					allResultsToCompare));

			// ERR results
			File errResults = new File(ReportManager.getInstance()
					.getErrOutFilename());
			String errFilename = Config.getInstance().getTestDataDir()
					+ "/outputSamplesToCompare/ping_direct_junitTestsDirectRun1_ItemsBatchedBy16plus_error_stats_GOLD.csv";
			File errResultsToCompare = new File(errFilename);
			assertTrue(errResults.exists());
			assertTrue(errResultsToCompare.exists());
			assertTrue(PingUtils.mediumStrictCompare(errResults,
					errResultsToCompare));
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}

	/**
	 * 16 is the total number of items available in one institution
	 */
	@Test
	public void testRun1ItemsBatchedBy16NoInstitutionFilter() {
		try {
			assertTrue(PingEquellaDriver
					.setup("testData/props/e2e-run1-direct-items-batched-by-16-no-institution-filter.properties"));
			assertTrue(PingEquellaDriver.run());
			assertTrue(PingEquellaDriver.finalizeRun());

			// ALL results
			File allResults = new File(ReportManager.getInstance()
					.getStdOutFilename());
			String filename = Config.getInstance().getTestDataDir()
					+ "/outputSamplesToCompare/ping_direct_junitTestsDirectRun1_ItemsBatchedBy16NoInstitutionFilter_all_stats_GOLD.csv";
			File allResultsToCompare = new File(filename);
			assertTrue(allResults.exists());
			assertTrue(allResultsToCompare.exists());
			assertTrue(PingUtils.mediumStrictCompare(allResults,
					allResultsToCompare));

			// ERR results
			File errResults = new File(ReportManager.getInstance()
					.getErrOutFilename());
			String errFilename = Config.getInstance().getTestDataDir()
					+ "/outputSamplesToCompare/ping_direct_junitTestsDirectRun1_ItemsBatchedBy16NoInstitutionFilter_error_stats_GOLD.csv";
			File errResultsToCompare = new File(errFilename);
			assertTrue(errResults.exists());
			assertTrue(errResultsToCompare.exists());
			assertTrue(PingUtils.mediumStrictCompare(errResults,
					errResultsToCompare));
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}

	/**
	 * 16 is the total number of items available in one institution
	 */
	@Test
	public void testRun1ItemsSingleQueryNoInstitutionFilter() {
		try {
			assertTrue(PingEquellaDriver
					.setup("testData/props/e2e-run1-direct-items-single-query-no-institution-filter.properties"));
			assertTrue(PingEquellaDriver.run());
			assertTrue(PingEquellaDriver.finalizeRun());

			// ALL results
			File allResults = new File(ReportManager.getInstance()
					.getStdOutFilename());
			String filename = Config.getInstance().getTestDataDir()
					+ "/outputSamplesToCompare/ping_direct_junitTestsDirectRun1_ItemsSingleQueryNoInstitutionFilter_all_stats_GOLD.csv";
			File allResultsToCompare = new File(filename);
			assertTrue(allResults.exists());
			assertTrue(allResultsToCompare.exists());
			assertTrue(PingUtils.mediumStrictCompare(allResults,
					allResultsToCompare));

			// ERR results
			File errResults = new File(ReportManager.getInstance()
					.getErrOutFilename());
			String errFilename = Config.getInstance().getTestDataDir()
					+ "/outputSamplesToCompare/ping_direct_junitTestsDirectRun1_ItemsSingleQueryNoInstitutionFilter_error_stats_GOLD.csv";
			File errResultsToCompare = new File(errFilename);
			assertTrue(errResults.exists());
			assertTrue(errResultsToCompare.exists());
			assertTrue(PingUtils.mediumStrictCompare(errResults,
					errResultsToCompare));
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}

	@Test
	public void testRun1ItemsBatchedBy2() {
		try {
			assertTrue(PingEquellaDriver
					.setup("testData/props/e2e-run1-direct-items-batched-by-2.properties"));
			assertTrue(PingEquellaDriver.run());
			assertTrue(PingEquellaDriver.finalizeRun());

			// ALL results
			File allResults = new File(ReportManager.getInstance()
					.getStdOutFilename());
			String filename = Config.getInstance().getTestDataDir()
					+ "/outputSamplesToCompare/ping_direct_junitTestsDirectRun1_ItemsBatchedBy2_all_stats_GOLD.csv";
			File allResultsToCompare = new File(filename);
			assertTrue(allResults.exists());
			assertTrue(allResultsToCompare.exists());
			assertTrue(PingUtils.mediumStrictCompare(allResults,
					allResultsToCompare));

			// ERR results
			File errResults = new File(ReportManager.getInstance()
					.getErrOutFilename());
			String errFilename = Config.getInstance().getTestDataDir()
					+ "/outputSamplesToCompare/ping_direct_junitTestsDirectRun1_ItemsBatchedBy2_error_stats_GOLD.csv";
			File errResultsToCompare = new File(errFilename);
			assertTrue(errResults.exists());
			assertTrue(errResultsToCompare.exists());
			assertTrue(PingUtils.mediumStrictCompare(errResults,
					errResultsToCompare));
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}

	/**
	 * Total number of items when filtered.
	 */
	@Test
	public void testRun1FilterByCollectionItemsBatchedBy6() {
		try {
			assertTrue(PingEquellaDriver
					.setup("testData/props/e2e-run1-direct-filterByCollectionItemsBatchedBy6.properties"));
			assertTrue(PingEquellaDriver.run());
			assertTrue(PingEquellaDriver.finalizeRun());

			// ALL results
			File allResults = new File(ReportManager.getInstance()
					.getStdOutFilename());
			String filename = Config.getInstance().getTestDataDir()
					+ "/outputSamplesToCompare/ping_direct_junitTestsDirectRun1FilterByCollectionBatchedBy6plus_all_stats_GOLD.csv";
			File allResultsToCompare = new File(filename);
			assertTrue(allResults.exists());
			assertTrue(allResultsToCompare.exists());
			assertTrue(PingUtils.mediumStrictCompare(allResults,
					allResultsToCompare));

			// ERR results
			File errResults = new File(ReportManager.getInstance()
					.getErrOutFilename());
			String errFilename = Config.getInstance().getTestDataDir()
					+ "/outputSamplesToCompare/ping_direct_junitTestsDirectRun1FilterByCollectionBatchedBy6plus_error_stats_GOLD.csv";
			File errResultsToCompare = new File(errFilename);
			assertTrue(errResults.exists());
			assertTrue(errResultsToCompare.exists());
			assertTrue(PingUtils.mediumStrictCompare(errResults,
					errResultsToCompare));
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}

	/**
	 * Over the total number of items when filtered.
	 */
	@Test
	public void testRun1FilterByCollectionItemsBatchedBy1000() {
		try {
			assertTrue(PingEquellaDriver
					.setup("testData/props/e2e-run1-direct-filterByCollectionItemsBatchedBy1000.properties"));
			assertTrue(PingEquellaDriver.run());
			assertTrue(PingEquellaDriver.finalizeRun());

			// ALL results
			File allResults = new File(ReportManager.getInstance()
					.getStdOutFilename());
			String filename = Config.getInstance().getTestDataDir()
					+ "/outputSamplesToCompare/ping_direct_junitTestsDirectRun1FilterByCollectionBatchedBy6plus_all_stats_GOLD.csv";
			File allResultsToCompare = new File(filename);
			assertTrue(allResults.exists());
			assertTrue(allResultsToCompare.exists());
			assertTrue(PingUtils.mediumStrictCompare(allResults,
					allResultsToCompare));

			// ERR results
			File errResults = new File(ReportManager.getInstance()
					.getErrOutFilename());
			String errFilename = Config.getInstance().getTestDataDir()
					+ "/outputSamplesToCompare/ping_direct_junitTestsDirectRun1FilterByCollectionBatchedBy6plus_error_stats_GOLD.csv";
			File errResultsToCompare = new File(errFilename);
			assertTrue(errResults.exists());
			assertTrue(errResultsToCompare.exists());
			assertTrue(PingUtils.mediumStrictCompare(errResults,
					errResultsToCompare));
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}

	/**
	 * Under the total number of items when filtered.
	 */
	@Test
	public void testRun1FilterByCollectionItemsBatchedBy2() {
		try {
			assertTrue(PingEquellaDriver
					.setup("testData/props/e2e-run1-direct-filterByCollectionItemsBatchedBy2.properties"));
			assertTrue(PingEquellaDriver.run());
			assertTrue(PingEquellaDriver.finalizeRun());

			// ALL results
			File allResults = new File(ReportManager.getInstance()
					.getStdOutFilename());
			String filename = Config.getInstance().getTestDataDir()
					+ "/outputSamplesToCompare/ping_direct_junitTestsDirectRun1FilterByCollectionBatchedBy2_all_stats_GOLD.csv";
			File allResultsToCompare = new File(filename);
			assertTrue(allResults.exists());
			assertTrue(allResultsToCompare.exists());
			assertTrue(PingUtils.mediumStrictCompare(allResults,
					allResultsToCompare));

			// ERR results
			File errResults = new File(ReportManager.getInstance()
					.getErrOutFilename());
			String errFilename = Config.getInstance().getTestDataDir()
					+ "/outputSamplesToCompare/ping_direct_junitTestsDirectRun1FilterByCollectionBatchedBy2_error_stats_GOLD.csv";
			File errResultsToCompare = new File(errFilename);
			assertTrue(errResults.exists());
			assertTrue(errResultsToCompare.exists());
			assertTrue(PingUtils.mediumStrictCompare(errResults,
					errResultsToCompare));
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}

	@Test
	public void testRun1FilterByCollectionItemsInSingleQuery() {
		try {
			assertTrue(PingEquellaDriver
					.setup("testData/props/e2e-run1-direct-filterByCollectionItemsInSingleQuery.properties"));
			assertTrue(PingEquellaDriver.run());
			assertTrue(PingEquellaDriver.finalizeRun());

			// ALL results
			File allResults = new File(ReportManager.getInstance()
					.getStdOutFilename());
			String filename = Config.getInstance().getTestDataDir()
					+ "/outputSamplesToCompare/ping_direct_junitTestsDirectRun1FilterByCollectionItemsInSingleQuery_all_stats_GOLD.csv";
			File allResultsToCompare = new File(filename);
			assertTrue(allResults.exists());
			assertTrue(allResultsToCompare.exists());
			assertTrue(PingUtils.mediumStrictCompare(allResults,
					allResultsToCompare));

			// ERR results
			File errResults = new File(ReportManager.getInstance()
					.getErrOutFilename());
			String errFilename = Config.getInstance().getTestDataDir()
					+ "/outputSamplesToCompare/ping_direct_junitTestsDirectRun1FilterByCollectionItemsInSingleQuery_error_stats_GOLD.csv";
			File errResultsToCompare = new File(errFilename);
			assertTrue(errResults.exists());
			assertTrue(errResultsToCompare.exists());
			assertTrue(PingUtils.mediumStrictCompare(errResults,
					errResultsToCompare));
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}

	@Test
	public void testRun1FilterByCollectionItemsInSingleQueryNoInstitutionFilter() {
		try {
			assertTrue(PingEquellaDriver
					.setup("testData/props/e2e-run1-direct-filterByCollectionItemsInSingleQueryNoInstitutionFilter.properties"));
			assertTrue(PingEquellaDriver.run());
			assertTrue(PingEquellaDriver.finalizeRun());

			// ALL results
			File allResults = new File(ReportManager.getInstance()
					.getStdOutFilename());
			String filename = Config.getInstance().getTestDataDir()
					+ "/outputSamplesToCompare/ping_direct_junitTestsDirectRun1FilterByCollectionItemsInSingleQueryNoInstitutionFilter_all_stats_GOLD.csv";
			File allResultsToCompare = new File(filename);
			assertTrue(allResults.exists());
			assertTrue(allResultsToCompare.exists());
			assertTrue(PingUtils.mediumStrictCompare(allResults,
					allResultsToCompare));

			// ERR results
			File errResults = new File(ReportManager.getInstance()
					.getErrOutFilename());
			String errFilename = Config.getInstance().getTestDataDir()
					+ "/outputSamplesToCompare/ping_direct_junitTestsDirectRun1FilterByCollectionItemsInSingleQueryNoInstitutionFilter_error_stats_GOLD.csv";
			File errResultsToCompare = new File(errFilename);
			assertTrue(errResults.exists());
			assertTrue(errResultsToCompare.exists());
			assertTrue(PingUtils.mediumStrictCompare(errResults,
					errResultsToCompare));
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}
}
