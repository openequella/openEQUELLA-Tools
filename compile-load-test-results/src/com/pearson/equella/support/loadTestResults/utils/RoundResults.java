package com.pearson.equella.support.loadTestResults.utils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RoundResults {
	private static final int INDEX_TIME_KEY = 0;
	private static final int INDEX_ACTIVE_THREADS = 1;
	private String[] headers;
	private Map<String, String[]> values = new HashMap<String, String[]>();
	private List<String> keys = new ArrayList<String>();
	private int start = 0;
	private int end = 0;
	private String handle;

	private RoundResults() {

	}

	public String[] getHeaders() {
		return headers;
	}

	public static RoundResults parseRoundResults(String handle)
			throws Exception {
		RoundResults rr = new RoundResults();
		rr.handle = handle;
		String path = FileUtils.FILE_ROOT + handle
				+ " reports/RoundMetricsOverview-" + handle + ".csv";
		System.out.println("Parsing Round Results file [" + path + "].");
		BufferedReader br = new BufferedReader(new FileReader(path));
		String line = br.readLine();
		while (line != null) {
			String[] parts = line.split(",");
			if (parts.length > 1) {
				if (parts[0].isEmpty()) {
					// Assume header.
					rr.headers = parts;
					System.out.println("**** Adding the header: "+line);
				} else {
					rr.values.put(parts[INDEX_TIME_KEY], parts);
					rr.keys.add(parts[INDEX_TIME_KEY]);
					System.out.println("**** Adding a data row ["+parts[INDEX_TIME_KEY]+"]: "+line);
				}
			} else {
				System.out.println("Found an unparsable line: ["+line+"].");
			}
			line = br.readLine();
		}
		br.close();
		System.out.println("Finished Parsing Round Results file [" + path
				+ "].");
		System.out.println("Setting up start index.");

		// While there are no active threads in current sorted time slice
		while (rr.values.get(rr.keys.get(rr.start))[INDEX_ACTIVE_THREADS]
				.equals("")) {
			// Move to the next time slice
			rr.start++;
		}
		System.out.println("Start index found: " + rr.start);

		System.out.println("Setting up end index.");
		rr.end = rr.keys.size() - 1;
		// While there are no active threads in current sorted time slice
		while (rr.values.get(rr.keys.get(rr.end))[INDEX_ACTIVE_THREADS]
				.equals("")) {
			// Move to the previous time slice
			rr.end--;
		}
		System.out.println("End index found: " + rr.end);

		return rr;
	}

	public int getKeySize() {
		return end - start;
	}

	public void adjustStartIndex(int size) {
		// If it's already the correct size, no adjustment needed.
		if ((end - start) == size) {
			System.out
					.println("No need to adjust the start ["+this.start+"] and end indexes for ["
							+ this.handle + "].");
			return; // noop
		}
		// Assumed that the size is already the min.
		while ((end - start) > size) {
			// Assumed that there is an actual numeric value
			if (Integer
					.parseInt(values.get(keys.get(start))[INDEX_ACTIVE_THREADS]) > Integer
					.parseInt(values.get(keys.get(end))[INDEX_ACTIVE_THREADS])) {
				end--;
			} else {
				start++;
			}
		}

		System.out.println("Adjusted the start and end indexes for ["
				+ this.handle + "] to be [" + start + "] and [" + end + "]");
	}

	public String[] get(int i) {
		return values.get(keys.get(start + i));
	}

	public String getResult(int i, int j) {
		if (get(i).length > j) {
			return get(i)[j];
		}
		return "";
	}
}
