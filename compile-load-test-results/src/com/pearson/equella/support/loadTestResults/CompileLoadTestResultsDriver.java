package com.pearson.equella.support.loadTestResults;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamReader;

import com.pearson.equella.support.loadTestResults.averagedAnalysis.AveragedAnalysis;
import com.pearson.equella.support.loadTestResults.utils.DbUtils;
import com.pearson.equella.support.loadTestResults.utils.FileUtils;

public class CompileLoadTestResultsDriver {
	// private static final String ANALYSIS_HEADER_ACTIVE_THREADS =
	// "ALL - Active Threads";
	// private static final String ANALYSIS_HEADER_THROUGHPUT = "ALL - KBs";
	// private static final String ANALYSIS_HEADER_TRANSACTIONS = "ALL - Trans";
	// private static final String ANALYSIS_HEADER_ERRORS = "ALL - Errors";
	// private static final String ANALYSIS_HEADER_ERROR_RATE = "ALL - % Error";
	// private static final String ANALYSIS_HEADER_RESP_TIME_AVG =
	// "ALL - Avg Resp time";
	// private static final String ANALYSIS_HEADER_RESP_TIME_90 =
	// "ALL - 90% Resp time";
	// private static final String ANALYSIS_HEADER_RESP_TIME_99 =
	// "ALL - 99% Resp time";
	// private static final String ANALYSIS_HEADER_RESP_TIME_MAX =
	// "ALL - Max Resp time";
	// private static final String ANALYSIS_HEADER_SESSION = "ALL - Sessions";
	// private static final String ANALYSIS_HEADER_CPU = "ALL - CPU Usage %";
	// private static final String ANALYSIS_HEADER_GC = "ALL - GC Activity %";
	private static final String ROUND_HEADER_ACTIVE_THREADS = "ROUND - "
			+ AveragedAnalysis.SUF_ACTIVE_THREADS;
	private static final String ROUND_HEADER_THROUGHPUT = "ROUND - "
			+ AveragedAnalysis.SUF_THROUGHPUT;
	private static final String ROUND_HEADER_TRANSACTIONS = "ROUND - "
			+ AveragedAnalysis.SUF_TRANS;
	private static final String ROUND_HEADER_ERRORS = "ROUND - "
			+ AveragedAnalysis.SUF_ERR_NUM;
	private static final String ROUND_HEADER_ERROR_RATE = "ROUND - "
			+ AveragedAnalysis.SUF_ERR_PERCENT;
	private static final String ROUND_HEADER_RESP_TIME_AVG = "ROUND - "
			+ AveragedAnalysis.SUF_RESP_AVG;
	private static final String ROUND_HEADER_RESP_TIME_90 = "ROUND - "
			+ AveragedAnalysis.SUF_RESP_90;
	private static final String ROUND_HEADER_RESP_TIME_99 = "ROUND - "
			+ AveragedAnalysis.SUF_RESP_99;
	private static final String ROUND_HEADER_RESP_TIME_MAX = "ROUND - "
			+ AveragedAnalysis.SUF_RESP_MAX;
	private static final String ROUND_HEADER_SESSION = "ROUND - "
			+ AveragedAnalysis.SUF_SESSIONS;
	private static final String ROUND_HEADER_CPU = "ROUND - "
			+ AveragedAnalysis.SUF_CPU;
	private static final String ROUND_HEADER_CPU_SRV1 = "ROUND - CPU Usage % (n1)";
	private static final String ROUND_HEADER_CPU_SRV2 = "ROUND - CPU Usage % (n2)";
	private static final String ROUND_HEADER_GC = "ROUND - "
			+ AveragedAnalysis.SUF_GC;
	private static final String ROUND_HEADER_GC_SRV1 = "ROUND - GC Activity % (n1)";
	private static final String ROUND_HEADER_GC_SRV2 = "ROUND - GC Activity % (n2)";

	private static List<JVisualVmResult> jVisualVmResults = new ArrayList<JVisualVmResult>();
	private static List<TomcatSessionResult> sessionResults = new ArrayList<TomcatSessionResult>();
	private static long globalCounter = 0L;
	private static Map<String, Map<String, MetricSlice>> slicesByRound = new HashMap<String, Map<String, MetricSlice>>();
	private static boolean skipJtlParse = true;

	private static boolean debugErrors = false;

	/**
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		if (args.length != 1) {
			System.out.println("Script expects 1 argument (round name).");
			System.exit(9);
		}
		String round = args[0];
		DbUtils.primeConnection();
		if (debugErrors) {
			debugErrors(round);
		} else {
			if (skipJtlParse)
				System.out
						.println("Skipping the parse of JTL files and using what's in the DB...");
			if (!skipJtlParse)
				DbUtils.resetDb(round);
			System.out
					.println("*********************** Parsing Results for Round "
							+ round + " *********************** ");
			parseResultFiles(FileUtils.FILE_ROOT, round);
			if (!skipJtlParse)
				DbUtils.finalizeStore();

			System.out
					.println("*********************** Analyzing Datapoints for Round "
							+ round + " *********************** ");

			buildRoundMetricsViaDb(round);

			reportOnRoundMetricsOverview(round);
			reportOnRoundMetricsErrors(round);
			reportOnRoundMetricsThreadsVsTransactionsVsThroughput(round);
			reportOnRoundMetricsThreadsVsRespTimes(round);
			reportOnRoundMetricsThreadsVsTransactionsVsSessions(round);
			reportOnRoundMetricsTransactionsVsCpuVsGc(round);
			reportOnLongestAvgRuntimesByLabel(round);
		}
	}

	/**
	 * Dump out all sampler errors.
	 * 
	 * @param round
	 * @throws SQLException
	 * @throws ParseException
	 */
	private static void debugErrors(String round) throws SQLException,
			ParseException {
		Map<String, MetricSlice> slices = new HashMap<String, MetricSlice>();
		slicesByRound.put(round, slices);

		// For each timeslice, gather the rows and aggregate them.
		List<String> timeSlices = new ArrayList<String>();
		timeSlices.add("2016-11-30 07:07 MST");
		System.out.println("Samplers that have a failed transaction:");
		for (String timeSlice : timeSlices) {
			List<JtlXmlResult> resultsForSlice = DbUtils.fetchJtlXmlResults(
					timeSlice, round);
			System.out.printf("%s: \n", timeSlice);
			for (JtlXmlResult res : resultsForSlice) {
				if (!res.isSuccess()) {
					System.out.printf("- [%s] %s\n", res.getTestType(),
							res.getLabel());
				}
			}
		}
	}

	private static void reportOnLongestAvgRuntimesByLabel(String round)
			throws IOException {
		Map<String, Long> labelTotals = new HashMap<String, Long>();
		Map<String, Long> labelCounts = new HashMap<String, Long>();

		Map<String, MetricSlice> slices = slicesByRound.get(round);
		for (String key : slices.keySet()) {
			MetricSlice ms = slices.get(key);
			for (String label : JtlLabelTracker.getLabels()) {
				String avg = ms.getAverageResponseTime(label);
				if (avg.length() > 0) {
					if (!labelTotals.containsKey(label)) {
						labelTotals.put(label, 0L);
						labelCounts.put(label, 0L);
					}
					labelTotals.put(label,
							labelTotals.get(label) + Long.parseLong(avg));
					labelCounts.put(label, labelCounts.get(label) + 1);
				}
			}
		}

		List<ComparableBundle> labelAverages = new ArrayList<ComparableBundle>();
		for (String label : labelTotals.keySet()) {
			Long avg = labelTotals.get(label) / labelCounts.get(label);
			labelAverages.add(new ComparableBundle(label, avg, false));
		}
		Collections.sort(labelAverages);

		FileUtils.setupWriter("SamplerResponseTimeRankings_" + round + ".csv");
		FileUtils.out(true, "Sampler,Average");
		for (int i = (labelAverages.size() - 1); i >= 0; i--) {
			FileUtils.out(true, "%s,%s", labelAverages.get(i).getString(),
					labelAverages.get(i).getLong());
		}
		FileUtils.closeWriter();
	}

	private static void parseResultFiles(String filebase, String currentRound)
			throws Exception {
		if (!skipJtlParse) {
			//TODO loop through each JMeter jtl results file with the following invocation:
			//parseJtlXmlFileStream(currentRound, "test-name", filebase + currentRound + "-details/results.jtl");
		}
		parseTomcatSessionsFile(currentRound, filebase + currentRound
				+ "-details/sessions.log");
		//TODO loop through each JVisualVM tracer csv file with the following invocation:
		// parseJVisualVmTraceFile(currentRound, "server-name", filebase + currentRound + " tracers/my-tracer-details.csv");
	}

	private static void reportOnRoundMetricsOverview(String round)
			throws IOException {
		Map<String, MetricSlice> slices = slicesByRound.get(round);

		Set<String> masterTimes = new HashSet<String>();
		masterTimes.addAll(slices.keySet());
		List<String> masterTimesSorted = new ArrayList<String>(masterTimes);
		Collections.sort(masterTimesSorted);
		FileUtils.setupWriter("RoundMetricsOverview-" + round + ".csv");
		FileUtils.outComma(ROUND_HEADER_ACTIVE_THREADS.replaceFirst("ROUND",
				round));
		FileUtils
				.outComma(ROUND_HEADER_THROUGHPUT.replaceFirst("ROUND", round));
		FileUtils.outComma(ROUND_HEADER_TRANSACTIONS.replaceFirst("ROUND",
				round));
		FileUtils.outComma(ROUND_HEADER_ERRORS.replaceFirst("ROUND", round));
		FileUtils
				.outComma(ROUND_HEADER_ERROR_RATE.replaceFirst("ROUND", round));
		FileUtils.outComma(ROUND_HEADER_RESP_TIME_AVG.replaceFirst("ROUND",
				round));
		FileUtils.outComma(ROUND_HEADER_RESP_TIME_90.replaceFirst("ROUND",
				round));
		FileUtils.outComma(ROUND_HEADER_RESP_TIME_99.replaceFirst("ROUND",
				round));
		FileUtils.outComma(ROUND_HEADER_RESP_TIME_MAX.replaceFirst("ROUND",
				round));
		FileUtils.outComma(ROUND_HEADER_SESSION.replaceFirst("ROUND", round));
		FileUtils.outComma(ROUND_HEADER_CPU.replaceFirst("ROUND", round));
		FileUtils.outComma(ROUND_HEADER_CPU_SRV1.replaceFirst("ROUND", round));
		FileUtils.outComma(ROUND_HEADER_CPU_SRV2.replaceFirst("ROUND", round));
		FileUtils.outComma(ROUND_HEADER_GC.replaceFirst("ROUND", round));
		FileUtils.outComma(ROUND_HEADER_GC_SRV1.replaceFirst("ROUND", round));
		FileUtils.outComma(ROUND_HEADER_GC_SRV2.replaceFirst("ROUND", round));
		for (int i = 0; i < JtlLabelTracker.getLabels().size(); i++) {
			String lab = JtlLabelTracker.getLabels().get(i);
			FileUtils.outComma(round + " - " + AveragedAnalysis.SUF_RESP_AVG
					+ " (" + lab + ")");
			FileUtils.outComma(round + " - " + AveragedAnalysis.SUF_RESP_90
					+ " (" + lab + ")");
			FileUtils.outComma(round + " - " + AveragedAnalysis.SUF_RESP_99
					+ " (" + lab + ")");
			FileUtils.outComma(round + " - " + AveragedAnalysis.SUF_RESP_MAX
					+ " (" + lab + ")");
			FileUtils.outComma(round + " - " + AveragedAnalysis.SUF_ERR_NUM
					+ " (" + lab + ")");
		}
		FileUtils.outln();

		for (String key : masterTimesSorted) {
			//System.out.printf("Writing out timeslice [%s] \n", key);
			MetricSlice slice = slices.get(key);
			FileUtils.out(false, key);
			FileUtils.outComma(slice.getActiveThreads());
			FileUtils.outComma(slice.getThroughputInKBs());
			FileUtils.outComma(slice.getTrans());
			FileUtils.outComma(slice.getErrors(""));
			FileUtils.outComma(slice.getErrorRate(""));
			FileUtils.outComma(slice.getAverageResponseTime());
			FileUtils.outComma(slice.getPercentileResponseTime(90));
			FileUtils.outComma(slice.getPercentileResponseTime(99));
			FileUtils.outComma(slice.getMaxResponseTime());
			FileUtils.outComma(slice.getSessionAverage());
			FileUtils.outComma(slice.getAvgCpuUsage());
			FileUtils.outComma(slice.getAvgCpuUsage("node1")); // TODO parameterize
			FileUtils.outComma(slice.getAvgCpuUsage("node2")); // TODO parameterize
			FileUtils.outComma(slice.getAvgGcActivity());
			FileUtils.outComma(slice.getAvgGcActivity("node1")); // TODO parameterize
			FileUtils.outComma(slice.getAvgGcActivity("node2")); // TODO parameterize

			for (int i = 0; i < JtlLabelTracker.getLabels().size(); i++) {
				FileUtils.outComma(slice.getAverageResponseTime(JtlLabelTracker
						.getLabels().get(i)));
				FileUtils.outComma(slice.getPercentileResponseTime(
						JtlLabelTracker.getLabels().get(i), 90));
				FileUtils.outComma(slice.getPercentileResponseTime(
						JtlLabelTracker.getLabels().get(i), 99));
				FileUtils.outComma(slice.getMaxResponseTime(JtlLabelTracker
						.getLabels().get(i)));
				FileUtils.outComma(slice.getErrors(JtlLabelTracker.getLabels()
						.get(i)));
			}
			FileUtils.outln();
		}
		FileUtils.closeWriter();
	}

	private static void reportOnRoundMetricsTransactionsVsCpuVsGc(String round)
			throws IOException {
		Map<String, MetricSlice> slices = slicesByRound.get(round);

		Set<String> masterTimes = new HashSet<String>();
		masterTimes.addAll(slices.keySet());
		List<String> masterTimesSorted = new ArrayList<String>(masterTimes);
		Collections.sort(masterTimesSorted);
		FileUtils.setupWriter("RoundMetricsTransactionsVsCpuVsGc-" + round
				+ ".csv");
		FileUtils.outComma(ROUND_HEADER_TRANSACTIONS.replaceFirst("ROUND",
				round));
		FileUtils.outComma(ROUND_HEADER_CPU.replaceFirst("ROUND", round));
		FileUtils.outComma(ROUND_HEADER_GC.replaceFirst("ROUND", round));
		FileUtils.outln();

		for (String key : masterTimesSorted) {
			MetricSlice slice = slices.get(key);
			FileUtils.out(false, key);
			FileUtils.outComma(slice.getTrans());
			FileUtils.outComma(slice.getAvgCpuUsage());
			FileUtils.outComma(slice.getAvgGcActivity());
			FileUtils.outln();
		}
		FileUtils.closeWriter();
	}

	private static void reportOnRoundMetricsThreadsVsTransactionsVsSessions(
			String round) throws IOException {
		Map<String, MetricSlice> slices = slicesByRound.get(round);

		Set<String> masterTimes = new HashSet<String>();
		masterTimes.addAll(slices.keySet());
		List<String> masterTimesSorted = new ArrayList<String>(masterTimes);
		Collections.sort(masterTimesSorted);
		FileUtils.setupWriter("RoundMetricsThreadsVsTransactionsVsSessions-"
				+ round + ".csv");
		FileUtils.outComma(ROUND_HEADER_ACTIVE_THREADS.replaceFirst("ROUND",
				round));
		FileUtils.outComma(ROUND_HEADER_TRANSACTIONS.replaceFirst("ROUND",
				round));
		FileUtils.outComma(ROUND_HEADER_SESSION.replaceFirst("ROUND", round));
		FileUtils.outln();

		for (String key : masterTimesSorted) {
			MetricSlice slice = slices.get(key);
			FileUtils.out(false, key);
			FileUtils.outComma(slice.getActiveThreads());
			FileUtils.outComma(slice.getTrans());
			FileUtils.outComma(slice.getSessionAverage());
			FileUtils.outln();
		}
		FileUtils.closeWriter();
	}

	private static void reportOnRoundMetricsThreadsVsRespTimes(String round)
			throws IOException {
		Map<String, MetricSlice> slices = slicesByRound.get(round);

		Set<String> masterTimes = new HashSet<String>();
		masterTimes.addAll(slices.keySet());
		List<String> masterTimesSorted = new ArrayList<String>(masterTimes);
		Collections.sort(masterTimesSorted);
		FileUtils.setupWriter("RoundMetricsThreadsVsRespTimes-" + round
				+ ".csv");
		FileUtils.outComma(ROUND_HEADER_ACTIVE_THREADS.replaceFirst("ROUND",
				round));
		FileUtils.outComma(ROUND_HEADER_RESP_TIME_AVG.replaceFirst("ROUND",
				round));
		FileUtils.outComma(ROUND_HEADER_RESP_TIME_90.replaceFirst("ROUND",
				round));
		FileUtils.outComma(ROUND_HEADER_RESP_TIME_99.replaceFirst("ROUND",
				round));
		FileUtils.outComma(ROUND_HEADER_RESP_TIME_MAX.replaceFirst("ROUND",
				round));
		FileUtils.outln();

		for (String key : masterTimesSorted) {
			MetricSlice slice = slices.get(key);
			FileUtils.out(false, key);
			FileUtils.outComma(slice.getActiveThreads());
			FileUtils.outComma(slice.getAverageResponseTime());
			FileUtils.outComma(slice.getPercentileResponseTime(90));
			FileUtils.outComma(slice.getPercentileResponseTime(99));
			FileUtils.outComma(slice.getMaxResponseTime());
			FileUtils.outln();
		}
		FileUtils.closeWriter();
	}

	private static void reportOnRoundMetricsErrors(String round)
			throws IOException {
		Map<String, MetricSlice> slices = slicesByRound.get(round);

		Set<String> masterTimes = new HashSet<String>();
		masterTimes.addAll(slices.keySet());
		List<String> masterTimesSorted = new ArrayList<String>(masterTimes);
		Collections.sort(masterTimesSorted);
		FileUtils.setupWriter("RoundMetricsErrors-" + round + ".csv");
		FileUtils.outComma(ROUND_HEADER_ERRORS.replaceFirst("ROUND", round));
		FileUtils
				.outComma(ROUND_HEADER_ERROR_RATE.replaceFirst("ROUND", round));

		for (int i = 0; i < JtlLabelTracker.getLabels().size(); i++) {
			String lab = JtlLabelTracker.getLabels().get(i);
			FileUtils.outComma(round + " - " + AveragedAnalysis.SUF_ERR_NUM
					+ " (" + lab + ")");
		}
		FileUtils.outln();

		for (String key : masterTimesSorted) {
			MetricSlice slice = slices.get(key);
			FileUtils.out(false, key);
			FileUtils.outComma(slice.getErrors(""));
			FileUtils.outComma(slice.getErrorRate(""));
			for (int i = 0; i < JtlLabelTracker.getLabels().size(); i++) {
				String lab = JtlLabelTracker.getLabels().get(i);
				FileUtils.outComma(slice.getErrors(lab));
			}
			FileUtils.outln();
		}
		FileUtils.closeWriter();
	}

	private static void reportOnRoundMetricsThreadsVsTransactionsVsThroughput(
			String round) throws IOException {
		Map<String, MetricSlice> slices = slicesByRound.get(round);

		Set<String> masterTimes = new HashSet<String>();
		masterTimes.addAll(slices.keySet());
		List<String> masterTimesSorted = new ArrayList<String>(masterTimes);
		Collections.sort(masterTimesSorted);
		FileUtils.setupWriter("RoundMetricsThreadsVsTransactionsVsThroughput_"
				+ round + ".csv");
		FileUtils.outComma(ROUND_HEADER_ACTIVE_THREADS.replaceFirst("ROUND",
				round));
		FileUtils
				.outComma(ROUND_HEADER_THROUGHPUT.replaceFirst("ROUND", round));
		FileUtils.outComma(ROUND_HEADER_TRANSACTIONS.replaceFirst("ROUND",
				round));
		FileUtils.outln();

		for (String key : masterTimesSorted) {
			MetricSlice slice = slices.get(key);
			FileUtils.out(false, key);
			FileUtils.outComma(slice.getActiveThreads());
			FileUtils.outComma(slice.getThroughputInKBs());
			FileUtils.outComma(slice.getTrans());
			FileUtils.outln();
		}
		FileUtils.closeWriter();
	}

	private static void buildRoundMetricsViaDb(String round)
			throws SQLException, ParseException, IOException {
		Map<String, MetricSlice> slices = new HashMap<String, MetricSlice>();
		slicesByRound.put(round, slices);

		// For each timeslice, gather the rows and aggregate them.
		List<String> timeSlices = DbUtils.fetchDistinctTimeSlices(round);
		int counter = 0;
		System.out.println("Aggregrating JTL XML results into timeslices.");
		for (String timeSlice : timeSlices) {
			counter++;
			if (counter % 50 == 0) {
				System.out.println(".");
			} else {
				System.out.print(".");
			}
			List<JtlXmlResult> resultsForSlice = DbUtils.fetchJtlXmlResults(
					timeSlice, round);
			for (JtlXmlResult res : resultsForSlice) {
				if (!slices.containsKey(res.getTimeSlice())) {
					slices.put(res.getTimeSlice(),
							new MetricSlice(res.getTimeSlice()));
				}
				MetricSlice slice = slices.get(res.getTimeSlice());
				slice.setActiveThreads(res.getTestType(),
						res.getNumOfActiveThreads());
				slice.addThroughputInBytes(res.getBytes());
				slice.addResponseTime(res.getElapsedMillis());
				
				//Normalize label naming convention.
				String label = res.getLabel();
				if (!label.startsWith(res.getTestType())) {
					label = res.getTestType() + " - Other Samplers";
				}
				
				slice.addResponseTime(label, res.getElapsedMillis());
				JtlLabelTracker.trackLabel(label);
				slice.addTrans(label);
				if (!res.isSuccess()) {
					slice.addFailedTrans(label);
				}
			}
		}

		System.out.println("Aggregation complete.");

		for (TomcatSessionResult sessionResult : sessionResults) {
			if (sessionResult.getRound().equals(round)) {
				if (!slices.containsKey(sessionResult.getTimeSlice())) {
					slices.put(sessionResult.getTimeSlice(), new MetricSlice(
							sessionResult.getTimeSlice()));
				}
				MetricSlice slice = slices.get(sessionResult.getTimeSlice());
				slice.trackTomcatSession(sessionResult.getNumOfSessions());
				// System.out.println("Adding session ["+sessionResult.getNumOfSessions()+"] to slice ["+sessionResult.getTimeSlice()+"]");
			}
		}

		for (JVisualVmResult jvmResult : jVisualVmResults) {
			if (jvmResult.getRound().equals(round)) {
				if (!slices.containsKey(jvmResult.getTimeSlice())) {
					slices.put(jvmResult.getTimeSlice(), new MetricSlice(
							jvmResult.getTimeSlice()));
				}
				MetricSlice slice = slices.get(jvmResult.getTimeSlice());
				slice.trackCpuUsage(jvmResult.getServer(),
						jvmResult.getCpuUsage());
				slice.trackGcActivity(jvmResult.getServer(),
						jvmResult.getGcActivity());
				// System.out.println("Adding JVM stats Server=["+jvmResult.getServer()+"] CPU=["+jvmResult.getCpuUsage()+"] GC=["+jvmResult.getGcActivity()+"] to slice ["+jvmResult.getTimeSlice()+"]");
			}
		}
	}

	private static void storeResult(String currentRound, JtlXmlResult result)
			throws Exception {
		DbUtils.store(result, currentRound);
	}

	private static void parseTomcatSessionsFile(String currentRound,
			String filename) throws IOException, ParseException {
		System.out.println("Parsing Tomcat sessions file [" + filename + "].");
		BufferedReader br = new BufferedReader(new FileReader(filename));
		String line = br.readLine();
		while (line != null) {
			String[] parts = line.split(",");
			TomcatSessionResult res = new TomcatSessionResult(currentRound);
			if (parts.length < 2) {
				System.out.println("Ignoring Tomcat session line [" + line
						+ "].");
			} else {
				res.setNumOfSessions(Integer.parseInt(parts[1]));
				res.setTimeSlice(parts[0]);
				sessionResults.add(res);
				// System.out.println("Found a new session: ["+parts[0]+"], ["+res.getTimeSlice()+"] and ["+res.getNumOfSessions()+"]");
			}
			line = br.readLine();
		}
		br.close();
		System.out.println("Found [" + sessionResults.size()
				+ "] session results.");
	}

	// Assumes the first line is a header.
	// Assumes the fields are CSV, and quoted.
	// Assumes Timestamp is the first field
	// Assumes CPU Usage % is the second field
	// Assumes GC Activity % is the third field
	private static void parseJVisualVmTraceFile(String currentRound,
			String server, String filename) throws Exception {
		System.out.println("Parsing JVisualVM tracer file [" + filename
				+ "], server [" + server + "].");
		BufferedReader br = new BufferedReader(new FileReader(filename));
		String line = br.readLine();
		while (line != null) {
			if (!line.startsWith("\"Time")) { // Ignore the header
				List<String> parts = parseJVisualVMLine(line);
				if (parts.size() != 10) {
					throw new Exception(
							String.format(
									"Line is expected to have 10 parts.  instead had [%s].  Line=[%s]",
									parts.size(), line));
				}
				// System.out.println("Line: "+line);

				JVisualVmResult res = new JVisualVmResult(currentRound);
				res.setServer(server);
				res.setTimeSlice(parts.get(0));
				res.setCpuUsage(Double.parseDouble(parts.get(1)));
				res.setGcActivity(Double.parseDouble(parts.get(2)));
				jVisualVmResults.add(res);
				// System.out.println(String.format("TS=[%s], CPU=[%s], GC=[%s]",
				// res.getTimeSlice(), res.getCpuUsage(), res.getGcActivity()));
			}
			line = br.readLine();
		}
		br.close();
		System.out.println("Found [" + jVisualVmResults.size()
				+ "] session results.");
	}

	private static List<String> parseJVisualVMLine(String line) {
		List<String> parts = new ArrayList<String>();
		int lastIndex = 0;
		for (int i = 0; i < line.length(); i++) {
			if ((line.charAt(i) == ',') && (line.charAt(i - 1) == '"')
					&& (line.charAt(i + 1) == '"')) {
				parts.add(line.substring(lastIndex, i).replace('"', ' ').trim());
				lastIndex = i + 1;
			}
		}
		return parts;
	}

	private static void parseJtlXmlFileStream(String currentRound,
			String testType, String filename) {
		try {
			FileInputStream in = new FileInputStream(filename);
			XMLInputFactory factory = XMLInputFactory.newInstance();
			XMLStreamReader parser = factory.createXMLStreamReader(in);
			int counter = 0;
			for (int event = parser.next(); event != XMLStreamConstants.END_DOCUMENT; event = parser
					.next()) {
				if ((event == XMLStreamConstants.START_ELEMENT)
						&& (parser.getLocalName().equals("httpSample"))) {
					counter++;
					globalCounter++;
					// System.out.println("---> Found an httpSample: ");
					Map<String, String> atts = new HashMap<String, String>();
					for (int attIndex = 0; attIndex < parser
							.getAttributeCount(); attIndex++) {
						atts.put(parser.getAttributeName(attIndex)
								.getLocalPart(), parser
								.getAttributeValue(attIndex));
					}
					storeResult(currentRound,
							JtlXmlResult.consumeAtts(testType, atts));

					if (counter % 10000 == 0) {
						System.out.println(String.format(
								"Parsed [%s] / [%s] results...", counter,
								globalCounter));
					}
				}
			}
			parser.close();
			in.close();

			System.out.println(String.format("Found [%s] results in [%s].",
					counter, filename));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
