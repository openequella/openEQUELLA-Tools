package com.pearson.equella.support.ping.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.pearson.equella.support.ping.direct.ResultsRow;
import com.pearson.equella.support.ping.report.ResultComparison;

public class PingUtils {
private static final Logger logger = LogManager.getLogger(PingUtils.class);

	/**
	 * Walk through both files, only ignoring Institution, timestamps, and
	 * duration
	 * 
	 * Meant to compare runs of the same ping type
	 * 
	 * @param r1
	 * @param r2
	 * @return
	 * @throws IOException
	 */
	public static boolean mediumStrictCompare(File r1, File r2)
			throws IOException {
		int lineCounter = 0;
		List<String> l1Rows = new ArrayList<String>();
		List<String> l2Rows = new ArrayList<String>();
		BufferedReader br1 = new BufferedReader(new FileReader(r1));
		BufferedReader br2 = new BufferedReader(new FileReader(r2));
		logger.info("Comparing [{}] with [{}]", r1.getAbsolutePath(),
				r2.getAbsolutePath());
		boolean readyToCheck = false;
		while (!readyToCheck) {
			lineCounter++;
			String l1 = br1.readLine();
			String l2 = br2.readLine();
			if (((l1 != null) && (l2 == null)) || (l1 == null) && (l2 != null)) {
				// Both lines should be null if one is null.
				logger.warn("Reached EOF on one of the files before the other!");
				logger.warn("[{}] L1:  {}", lineCounter, l1);
				logger.warn("[{}] L2:  {}", lineCounter, l2);
				return false;
			} else if ((l1 == null) && (l2 == null)) {
				readyToCheck = true;
			} else if (comp(l1, l2, "Institution")
					|| comp(l1, l2, "Report Timestamp")
					|| comp(l1, l2, "Report Duration")
					|| comp(l1, l2, "Report Start")
					|| comp(l1, l2, "Report End")
					|| comp(l1, l2, "Total duration of queries")
					|| comp(l1, l2, "Average duration of queries")) {
				// Ignore, no error
			} else {
				//cache the rows
				l1Rows.add(l1);
				l2Rows.add(l2);
			}
		}
		
		Collections.sort(l1Rows);
		Collections.sort(l2Rows);
		for(int i = 0; i < l1Rows.size(); i++) {
			String l1 = l1Rows.get(i);
			String l2 = l2Rows.get(i);
			if (!l1.contentEquals(l2)) {
				// The lines should be equal.
				logger.warn("Lines don't match!");
				logger.warn("[{}] L1:  {}", lineCounter, l1);
				logger.warn("[{}] L2:  {}", lineCounter, l2);

				return false;
			}
		}
		
		logger.info("Files have the same report re: attachments");
		return true;
	}

	/**
	 * Walk through both files, ignoring Institution, timestamps, and duration.
	 * Sorts the file rows, and only checks specific values of the csv row read
	 * in.
	 * 
	 * Meant to compare runs of different ping type
	 * 
	 * @param r1
	 * @param r2
	 * @return
	 * @throws IOException
	 */
	public static ResultComparison looseCompare(File r1, File r2) {
		try {
			if(r1 == null) {
				logger.warn("First comparison file is null, unable to build a comparison report.");
				return null;
			}
			logger.info("Beginning to loose compare [{}] and [{}]",
					r1.getName(), r2.getName());
			Map<String, ResultsRow> rows1 = slurpAndNormalizeCsvFile(r1);
			logger.info("Found [{}] result rows in [{}]", rows1.size(),
					r1.getName());
			Map<String, ResultsRow> rows2 = slurpAndNormalizeCsvFile(r2);
			logger.info("Found [{}] result rows in [{}]", rows2.size(),
					r2.getName());
			Set<String> keys1 = rows1.keySet();
			Set<String> keys2 = rows2.keySet();

			List<ResultsRow> onlyInFirst = new ArrayList<ResultsRow>();
			for (String s : findOnlyInFirst(keys1, keys2)) {
				onlyInFirst.add(rows1.get(s));
			}
			List<ResultsRow> onlyInSecond = new ArrayList<ResultsRow>();
			for (String s : findOnlyInFirst(keys2, keys1)) {
				onlyInSecond.add(rows2.get(s));
			}
			return new ResultComparison(r1.getName(), onlyInFirst,
					r2.getName(), onlyInSecond);
		} catch (Exception e) {
			logger.error("Error trying to compare results:  {}",
					e.getMessage(), e);
			return null;
		}
	}

	// TODO test...
	public static ResultComparison comparePreviousMissingAttachments(
			String currentMissingAttachmentsFilename) {
		File r2 = new File(currentMissingAttachmentsFilename);
		return looseCompare(findPreviousErrorStats(), r2);
	}
	
	//Only returns the n-1 file if n is greater than 1.
	public static File findPreviousErrorStats() {
		File dir = new File(Config.getInstance().getOutputFolder());
		String[] errorStats = dir.list(new OnlyErrorStatsFileFilter());
		List<String> errorStatsArray = Arrays.asList(errorStats);
		Collections.sort(errorStatsArray);
		File f = null;
		if (errorStatsArray.size() > 1) {
			f = new File(Config.getInstance().getOutputFolder(),
					errorStatsArray.get(errorStatsArray.size() - 2));
		}
		return f;
	}
	
	public static Collection<String> findOnlyInFirst(Set<String> s1,
			Set<String> s2) {
		Set<String> base = new HashSet<String>();
		base.addAll(s1);
		base.removeAll(s2);
		return base;
	}

	private static Map<String, ResultsRow> slurpAndNormalizeCsvFile(File f)
			throws Exception {
		Map<String, ResultsRow> rows = new HashMap<String, ResultsRow>();
		if (!f.exists()) {
			return rows;
		}
		BufferedReader br = new BufferedReader(new FileReader(f));
		String row = br.readLine();
		while (row != null) {
			if (row.startsWith("Institution") || row.startsWith("Ping type")
					|| row.startsWith("Report Timestamp")
					|| row.startsWith("Report Duration")
					|| row.startsWith("Database URL")
					|| row.startsWith("Database Type")
					|| row.startsWith("Filestore attachments")
					|| row.startsWith("Filter By Institution")
					|| row.startsWith("Filter By Collection")
					|| row.startsWith("# Of") || row.startsWith("Item UUID")
					|| row.startsWith("Stats")
					|| row.startsWith("Report Start")
					|| row.startsWith("Report End")
					|| row.startsWith("# Of queries ran")
					|| row.startsWith("Total duration of queries (ms)")
					|| row.startsWith("Average duration of queries (ms)")) {
				// Ignore - not a 'results row'.
			} else {
				ResultsRow parsedRow = slurpAndNormalizeCsvRow(row);
				rows.put(parsedRow.getPrimaryKey(), parsedRow);
			}
			row = br.readLine();
		}
		br.close();
		return rows;
	}

	/**
	 * Lines parsed into: Item UUID Item Version ItemStatus Collection UUID
	 * Attachment Type Attachment UUID Attachment Status Attachment Response
	 * Code Item Name *** Attachment Filepath ***
	 * 
	 * *** Quoted CSV data. not used in comparisons, thus not set.
	 * 
	 * @param row
	 * @return
	 * @throws Exception
	 */
	private static ResultsRow slurpAndNormalizeCsvRow(String row)
			throws Exception {
		StringTokenizer csv = new StringTokenizer(row, ",");
		if (csv.countTokens() < 10) {
			throw new Exception(String.format(
					"CSV row must have 11 tokens.  row=[%s]", row));
		}
		ResultsRow rr = new ResultsRow();
		rr.setInstitutionShortname(csv.nextToken());
		rr.setCollectionUuid(csv.nextToken());
		rr.setItemUuid(csv.nextToken());
		rr.setItemVersion(csv.nextToken());
		rr.setItemStatus(csv.nextToken().toUpperCase());
		rr.setAttType(normalizeAttType(csv.nextToken()));
		rr.setAttUuid(csv.nextToken());
		rr.setAttStatus(csv.nextToken());
		rr.setAttRespCode(csv.nextToken());
		//Jump one token, and then set the attachment file path
		csv.nextToken();
		rr.setAttFilePath(csv.nextToken());
		return rr;
	}

	private static String normalizeAttType(String nextToken) {
		switch (nextToken.toUpperCase()) {
		case "HTMLPAGE":
		case "HTML":
			return "HTMLPAGE";
		}
		return nextToken.toUpperCase();
	}

	private static boolean comp(String s1, String s2, String match) {
		return s1.startsWith(match) && s2.startsWith(match);
	}

	public static String str(String msg, Object... args) {
		return String.format(msg, args);
	}
}
