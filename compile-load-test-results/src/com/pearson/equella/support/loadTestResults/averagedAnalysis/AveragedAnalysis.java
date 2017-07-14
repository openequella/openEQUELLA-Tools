package com.pearson.equella.support.loadTestResults.averagedAnalysis;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class AveragedAnalysis {
	public static final String SUF_ACTIVE_THREADS = "Active Threads";
	public static final String SUF_TRANS = "Trans";
	public static final String SUF_THROUGHPUT = "KBs";
	public static final String SUF_CPU = "CPU Usage %";
	public static final String SUF_GC = "GC Activity %";
	public static final String SUF_RESP_AVG = "Avg Resp time";
	public static final String SUF_RESP_90 = "90% Resp time";
	public static final String SUF_RESP_99 = "99% Resp time";
	public static final String SUF_RESP_MAX = "Max Resp time";
	public static final String SUF_ERR_NUM = "Errors";
	public static final String SUF_ERR_PERCENT = "% Error";
	public static final String SUF_SESSIONS = "Sessions";
	
	private Map<Integer, Map<String, String>> vals = new HashMap<Integer, Map<String, String>>();
	private List<String> labels = new ArrayList<String>();
	private String name;
	private String roundName1;
	private String roundName2;
	private String roundName3;
	
	public AveragedAnalysis(String name, String r1, String r2, String r3) {
		this.name = name;
		this.roundName1 = r1;
		this.roundName2 = r2;
		this.roundName3 = r3;
	}
	
	public int getNumOfDataRows() {
		return vals.size();
	}
	
	public String getFileName(String flavor) {
		return String.format("AveragedAnalysis_%s_%s_%s_%s_%s.csv", name, flavor, roundName1, roundName2, roundName3);
	}
	
	public void addSliceDataPoint(Integer time, String header, String value) {
		if(!vals.containsKey(time)) {
			vals.put(time, new HashMap<String, String>());
		}
		vals.get(time).put(header, value);
		
		if(!labels.contains(header)) {
			labels.add(header);
		}
	}

	public Map<String, String> getMapping(Integer time) {
		return vals.get(time);
	}
	
	public List<String> getLabels() {
		return labels;
	}
	
	public List<Integer> getSortedTimes() {
		Set<Integer> keys = vals.keySet();
		List<Integer> listedKeys = new ArrayList<Integer>();
		listedKeys.addAll(keys);
		Collections.sort(listedKeys);
		return listedKeys;
	}

	public String getName() {
		return name;
	}

	public String getRoundName1() {
		return roundName1;
	}

	public String getRoundName2() {
		return roundName2;
	}

	public String getRoundName3() {
		return roundName3;
	}
}
