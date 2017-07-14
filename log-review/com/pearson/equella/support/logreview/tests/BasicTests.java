package com.pearson.equella.support.logreview.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.junit.Test;

import com.pearson.equella.support.logreview.LogReview;
import com.pearson.equella.support.logreview.LogReview.Granularity;
import com.pearson.equella.support.logreview.parser.LogMessage;
import com.pearson.equella.support.logreview.parser.ParseableFile;
import com.pearson.equella.support.logreview.utils.DbUtils;

public class BasicTests {
	private static final String BASE = "/base/directory/";
	private static final String TEST_FILES = "test-files/";
	private static final String TEST_CREDS = "creds.properties";

	@Test
	public void testE2EDiscreteFacets() {
		LogReview lr2 = new LogReview();
		try {
			DbUtils.primeConnection(BASE + TEST_FILES + TEST_CREDS);
			DbUtils.resetDb();
			assertEquals(DbUtils.countLogMessages(), 0);

			lr2.setConfig(BASE + TEST_FILES + "testDiscretes/control-discretes.json");

			List<ParseableFile> files = lr2.scanForFiles();
			assertEquals(files.size(), 1);
			for (ParseableFile f : files) {
				System.out.println(f);
			}
			assertEquals(buildId(files.get(0)), "node1-2016-12-29-application000");

			lr2.parseLogs(files);
			DbUtils.finalizeStore();

			String goldName = "testDiscretes/ExpectedOutput";

			Path[] targets = lr2.displayReport();
			Path unknownsTarget = lr2.displayUnknownMessages();

			Path unknownsGold = Paths.get(BASE + TEST_FILES + goldName + "-unknowns.txt");
			compare(unknownsGold, unknownsTarget);
			Path gold = Paths.get(BASE + TEST_FILES + goldName + ".csv");
			compare(gold, targets[0]);
			compare(Paths.get(BASE + TEST_FILES + goldName + "-condensed.csv"), targets[1]);

			// lr2.runReports();
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}

	private String buildId(ParseableFile pf) {
		return pf.getServer() + "-" + pf.getDate() + "-" + pf.getFilename();
	}

	@Test
	public void testDatabaseOps() {
		try {
			DbUtils.primeConnection(BASE + TEST_FILES + TEST_CREDS);
			DbUtils.resetDb();
			assertEquals(DbUtils.countLogMessages(), 0);
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}

	@Test
	public void testGranularity() {
		String s = "23:41:57,300";
		assertEquals(LogReview.normalizeTime(Granularity.D, s), "");
		assertEquals(LogReview.normalizeTime(Granularity.H, s), "23");
		assertEquals(LogReview.normalizeTime(Granularity.M, s), "23:41");
		assertEquals(LogReview.normalizeTime(Granularity.S, s), "23:41:57");
		assertEquals(LogReview.normalizeTime(Granularity.X, s), "ERR-GRAN");
	}

	private void compare(Path p1, Path p2) {
		try (BufferedReader goldReader = Files.newBufferedReader(p1, Charset.forName("UTF-8"));
				BufferedReader targetReader = Files.newBufferedReader(p2, Charset.forName("UTF-8"))) {
			String goldLine = goldReader.readLine();
			String targetLine = targetReader.readLine();

			while ((goldLine != null) && (targetLine != null)) {
				if (!goldLine.equals(targetLine)) {
					fail("Files are not equal based on the following lines:\nGOLD:  " + goldLine + "\nTARGET:  "
							+ targetLine);
				}
				goldLine = goldReader.readLine();
				targetLine = targetReader.readLine();
			}

			if (goldLine != null) {
				fail("Target file had less lines then Gold file.");
			}
			if (targetLine != null) {
				fail("Gold file had less lines then Target file.");
			}
		} catch (IOException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}
}
