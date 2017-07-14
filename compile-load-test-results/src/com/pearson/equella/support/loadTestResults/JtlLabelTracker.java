package com.pearson.equella.support.loadTestResults;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class JtlLabelTracker {
	private static List<String> labels = new ArrayList<String>();
	private static boolean sorted = false;
	public static void trackLabel(String label) {
		if(!labels.contains(label)) {
			labels.add(label);
			sorted = false;
		}
	}
	
	public static List<String> getLabels() {
		if(!sorted) {
			Collections.sort(labels);
		}
		return labels;
	}
}
