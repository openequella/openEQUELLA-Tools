package com.pearson.equella.support.loadTestResults.averagedAnalysis;

import java.math.BigDecimal;
import java.math.RoundingMode;

import com.pearson.equella.support.loadTestResults.utils.RoundResults;
import com.pearson.equella.support.loadTestResults.utils.StrUtils;

public class GenerateAveragedAnalysisDriver {
	private static AveragedAnalysis averagedAnalysis;

	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		if (args.length != 4) {
			System.out
					.println("This script needs 4 args to run - ANALYSIS_NAME, ROUND_A, ROUND_B, and ROUND_C");
			System.exit(9);
		}
		String analysisName = args[0];
		String r1 = args[1];
		String r2 = args[2];
		String r3 = args[3];

		buildAveragedAnalysis(analysisName, r1, r2, r3);

		AveragedAnalysisReporter.reportOnAnalysisOverview(averagedAnalysis);
		AveragedAnalysisReporter.reportOnAnalysisTransactionsVsCpuVsGc(averagedAnalysis);
		AveragedAnalysisReporter.reportOnAnalysisErrors(averagedAnalysis);
		AveragedAnalysisReporter.reportOnAnalysisThreadsVsRespTimes(averagedAnalysis);
		AveragedAnalysisReporter.reportOnAnalysisThreadsVsTransactionsVsSessions(averagedAnalysis);
		AveragedAnalysisReporter.reportOnAnalysisTransactionsVsCpuVsGc(averagedAnalysis);
		AveragedAnalysisReporter.reportOnAnalysisThreadsVsTransactionsVsThroughput(averagedAnalysis);
	}

	/**
	 * Based on discrete start and end times (that should have the same # of
	 * timeSlices), average all fields. Use only the timeSlices that have
	 * 'active threads'. Choose the smallest number of timeSlices, and make the
	 * other ones 'fit' by removing equal numbers of slices from the beginning
	 * and ending
	 * @throws Exception 
	 */
	private static void buildAveragedAnalysis(String analysisName, String r1,
			String r2, String r3) throws Exception {

		RoundResults results1 = RoundResults.parseRoundResults(r1);
		RoundResults results2 = RoundResults.parseRoundResults(r2);
		RoundResults results3 = RoundResults.parseRoundResults(r3);

		int masterAmountOfSlices = Math.min(results1.getKeySize(),
				Math.min(results2.getKeySize(), results3.getKeySize()));

		results1.adjustStartIndex(masterAmountOfSlices);
		results2.adjustStartIndex(masterAmountOfSlices);
		results3.adjustStartIndex(masterAmountOfSlices);

		System.out.printf(
				"Building Averaged Analysis for rounds %s, %s, and %s\n", r1,
				r2, r3);

		averagedAnalysis = new AveragedAnalysis(analysisName, r1, r2, r3);

		// Create the averages of all values with timestamps converted to
		// indexes.
		for (int i = 0; i <= masterAmountOfSlices; i++) {
			if ((results1.getHeaders().length != results2.getHeaders().length)
					|| (results2.getHeaders().length != results3.getHeaders().length)) {
				throw new Exception(
						String.format(
								"Unequal header length between the three rounds: %s=[%s], %s=[%s], %s=[%s].",
								r1, results1.getHeaders().length, r2,
								results2.getHeaders().length, r3,
								results3.getHeaders().length));
			}
			
			//There is no 'first' header.
			for (int j = 1; j < results1.getHeaders().length; j++) {
				String subHeader1 = results1.getHeaders()[j].substring(7);
				String subHeader2 = results2.getHeaders()[j].substring(7);
				String subHeader3 = results3.getHeaders()[j].substring(7);
				
				if ((!subHeader1.equals(subHeader2))
						|| (!subHeader2.equals(subHeader3))) {
					throw new Exception(
							String.format(
									"Unequal headers: %s=[%s], %s=[%s], %s=[%s].",
									r1, subHeader1, r2,
									subHeader2, r3,
									subHeader3));
				}
				
				String val1 = results1.getResult(i,j);
				String val2 = results2.getResult(i,j);
				String val3 = results3.getResult(i,j);
				System.out.printf("Averaging %s - [%s], [%s], and [%s].\n", subHeader1, val1, val2, val3);
				if(val1.contains(".") || val2.contains(".") || val3.contains(".")) {
					averagedAnalysis.addSliceDataPoint(
							i,
							analysisName+subHeader1,
							averageDouble(val1,val2,val3));	
				} else {
					averagedAnalysis.addSliceDataPoint(
							i,
							analysisName+subHeader1,
							averageInt(val1,val2,val3));
				}
			}
		}
		System.out
				.printf("Completed building Averaged Analysis for rounds %s, %s, and %s.  Number of data rows: %s \n",
						r1, r2, r3, averagedAnalysis.getNumOfDataRows());

	}

	private static String averageInt(String val1, String val2, String val3) {
		int sum = 0;
		int count = 0;
		if (val1.length() != 0) {
			sum += Integer.parseInt(val1);
			count++;
		}
		if (val2.length() != 0) {
			sum += Integer.parseInt(val2);
			count++;
		}
		if (val3.length() != 0) {
			sum += Integer.parseInt(val3);
			count++;
		}
		String avg = "";
		if (count != 0)
			avg = "" + (sum / count);
		System.out.printf("AVG_I(%s,%s,%s) = [%s]", val1, val2, val3, avg);
		System.out.println();
		return avg;
	}

	private static String averageDouble(String val1, String val2, String val3) {
		BigDecimal sum = new BigDecimal(0);
		int count = 0;
		if (val1.length() != 0) {
			sum = sum.add(new BigDecimal(val1));
			count++;
		}
		if (val2.length() != 0) {
			sum = sum.add(new BigDecimal(val2));
			count++;
		}
		if (val3.length() != 0) {
			sum = sum.add(new BigDecimal(val3));
			count++;
		}
		String avg = "";
		if (count != 0) {
			avg = StrUtils.getDecimalFormatter().format(sum.divide(new BigDecimal(count), 2, RoundingMode.HALF_UP));
		}
		System.out.printf("AVG_D(%s,%s,%s) = [%s]", val1, val2, val3, avg);
		System.out.println();
		return avg;
	}
}
