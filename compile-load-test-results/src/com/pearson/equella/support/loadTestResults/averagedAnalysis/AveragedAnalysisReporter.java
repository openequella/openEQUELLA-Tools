package com.pearson.equella.support.loadTestResults.averagedAnalysis;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.pearson.equella.support.loadTestResults.utils.FileUtils;

public class AveragedAnalysisReporter {
	
	public static void reportOnAnalysisOverview(AveragedAnalysis a)
			throws IOException {
		genericReport(a, a.getFileName("Overview"), a.getLabels());
	}

	public static void reportOnAnalysisTransactionsVsCpuVsGc(AveragedAnalysis a)
			throws IOException {
		List<String> sub = new ArrayList<String>();
		for (String s : a.getLabels()) {
			if (s.endsWith(AveragedAnalysis.SUF_TRANS)
					|| s.endsWith(AveragedAnalysis.SUF_CPU)
					|| s.endsWith(AveragedAnalysis.SUF_GC)) {
				sub.add(s);
			}
		}
		genericReport(a, a.getFileName("TransactionsVsCpuVsGc"), sub);
	}

	public static void reportOnAnalysisThreadsVsTransactionsVsSessions(
			AveragedAnalysis a) throws IOException {
		List<String> sub = new ArrayList<String>();
		for (String s : a.getLabels()) {
			if (s.endsWith(AveragedAnalysis.SUF_ACTIVE_THREADS)
					|| s.endsWith(AveragedAnalysis.SUF_TRANS)
					|| s.endsWith(AveragedAnalysis.SUF_SESSIONS)) {
				sub.add(s);
			}
		}
		genericReport(a, a.getFileName("ThreadsVsTransactionsVsSessions"), sub);
	}

	public static void reportOnAnalysisThreadsVsRespTimes(AveragedAnalysis a)
			throws IOException {
		List<String> sub = new ArrayList<String>();
		for (String s : a.getLabels()) {
			if (s.endsWith(AveragedAnalysis.SUF_ACTIVE_THREADS)
					|| s.endsWith(AveragedAnalysis.SUF_RESP_AVG)
					|| s.endsWith(AveragedAnalysis.SUF_RESP_90)
					|| s.endsWith(AveragedAnalysis.SUF_RESP_99)
					|| s.endsWith(AveragedAnalysis.SUF_RESP_MAX)) {
				sub.add(s);
			}
		}
		genericReport(a, a.getFileName("ThreadsVsRespTimes"), sub);
	}

	public static void reportOnAnalysisErrors(
			AveragedAnalysis a) throws IOException {
		List<String> sub = new ArrayList<String>();
		for (String s : a.getLabels()) {
			if (s.endsWith(AveragedAnalysis.SUF_ERR_NUM)
					|| s.endsWith(AveragedAnalysis.SUF_ERR_PERCENT)) {
				sub.add(s);
			}
		}
		genericReport(a, a.getFileName("Errors"), sub);
	}

	public static void reportOnAnalysisThreadsVsTransactionsVsThroughput(
			AveragedAnalysis a) throws IOException {
		List<String> sub = new ArrayList<String>();
		for (String s : a.getLabels()) {
			if (s.endsWith(AveragedAnalysis.SUF_ACTIVE_THREADS)
					|| s.endsWith(AveragedAnalysis.SUF_TRANS)
					|| s.endsWith(AveragedAnalysis.SUF_THROUGHPUT)) {
				sub.add(s);
			}
		}
		genericReport(a, a.getFileName("ThreadsVsTransactionsVsThroughput"), sub);
	}

	private static void genericReport(AveragedAnalysis a, String filename,
			List<String> labels) throws IOException {
		FileUtils.setupWriter(filename);

		for (String s : labels) {
			FileUtils.outComma(s);
		}
		FileUtils.outln();

		List<Integer> times = a.getSortedTimes();
		for (int i = 0; i < times.size(); i++) {
			FileUtils.out(false, "" + times.get(i));
			for (String s : labels) {
				FileUtils.outComma(a.getMapping(i).get(s));
			}
			FileUtils.outln();
		}
		FileUtils.closeWriter();
	}
}
