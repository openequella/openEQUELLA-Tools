package com.pearson.equella.support.loadTestResults;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.pearson.equella.support.loadTestResults.utils.StrUtils;

public class MetricSlice {
	Map<String, Integer> activeThreadsByTestType = new HashMap<String, Integer>();
	private long bytes = -1;
	private String pointInTime;
	
	private List<Long> rTimes = new ArrayList<Long>();
	private Map<String, List<Long>> separatedRTimes = new HashMap<String, List<Long>>();
	private Map<String, Integer> separatedErrors = new HashMap<String, Integer>();
	private Map<String, Integer> separatedTrans = new HashMap<String, Integer>();
	private int numOfSessions = 0;
	private int sessionTotal = 0;
	private Map<String, List<Double>> cpuUsage = new HashMap<String, List<Double>>();
	private Map<String, List<Double>> gcActivity = new HashMap<String, List<Double>>();
	
	
	public MetricSlice(String pointInTime) {
		this.pointInTime = pointInTime;
	}
	
	public String getActiveThreads() {
		if(activeThreadsByTestType.isEmpty()) return "";
		int sum = 0;
		for(String test : activeThreadsByTestType.keySet()) {
			sum += activeThreadsByTestType.get(test);
		}
		return ""+sum;
	}
	public void setActiveThreads(String test, int activeThreads) {
		if(!activeThreadsByTestType.containsKey(test) || (activeThreadsByTestType.get(test) < activeThreads)) {
			activeThreadsByTestType.put(test, activeThreads);
		}
	}
	public String getThroughputInKBs() {
		if(count("",separatedTrans) == 0) return "";
		return ""+Math.round(bytes/1024);
	}
	public void addThroughputInBytes(long bytes) {
		this.bytes += bytes;
	}

	public String getTrans() {
		int trans = count("",  this.separatedTrans);
		if(trans == 0) return "";
		return ""+trans;
	
	}

	public void addTrans(String sampler) {
		if(!separatedTrans.containsKey(sampler)) {
			separatedTrans.put(sampler, 1);
		} else {
			separatedTrans.put(sampler, separatedTrans.get(sampler) + 1);
		}
	}

	public String getErrorRate(String sampler) {
		int trans = count(sampler, this.separatedTrans);
		int errs = count(sampler, this.separatedErrors);
		if(trans == 0) return "";
		return StrUtils.getDecimalFormatter().format(((float)errs / trans)*100);
	}
	

	public void addFailedTrans(String sampler) {
		if(!separatedErrors.containsKey(sampler)) {
			separatedErrors.put(sampler, 1);
		} else {
			separatedErrors.put(sampler, separatedErrors.get(sampler) + 1);
		}
	}

	public void addResponseTime(Long elapsedMillis) {
		rTimes.add(elapsedMillis);
	}
	
	public String getAverageResponseTime() {
		if (rTimes.size() == 0) return "";
		Long total = 0L;
		for(Long l : rTimes) {
			total += l;
		}
		return ""+(total / rTimes.size());
	}

	public String getAverageResponseTime(String label) {
		List<Long> labeledRTimes = separatedRTimes.get(label);
		if (labeledRTimes == null || labeledRTimes.size() == 0) return "";
		Long total = 0L;
		for(Long l : labeledRTimes) {
			total += l;
		}
		return ""+(total / labeledRTimes.size());
	}

	public String getMaxResponseTime() {
		if (rTimes.size() == 0) return "";
		Long max = 0L;
		for(Long l : rTimes) {
			max = Math.max(max, l);
		}
		return ""+max;
	}
	
	public String getMaxResponseTime(String label) {
		List<Long> labeledRTimes = separatedRTimes.get(label);
		if (labeledRTimes == null || labeledRTimes.size() == 0) return "";
		Long max = 0L;
		for(Long l : labeledRTimes) {
			max = Math.max(max, l);
		}
		return ""+max;
	}
	/**
	 * Assumes 0 < percentile <= 100
	 * @param percentile
	 * @return
	 */
	public String getPercentileResponseTime(int percentile) {
		if (rTimes.size() == 0) return "";
		
		Collections.sort(rTimes);
		double index = (float)percentile/100 * rTimes.size();
		double finalIndex = Math.ceil(index);
		boolean isWhole = finalIndex == index;
		long result = -1L;
		if(isWhole) {
			result = (rTimes.get((int)finalIndex-1)+rTimes.get((int)finalIndex))/2;
		} else {
			result = rTimes.get((int)finalIndex-1);
		}
		//StringBuffer sb = new StringBuffer();
		//for(long l : rTimes) {
		//	sb.append(l+", ");
		//}
		//System.out.println("Samples:  " + sb);
		//System.out.println(String.format("Calc Percentile:  percent=[%s], size=[%s], index=[%s], isWhole=[%s], result=[%s]", percentile, rTimes.size(), index, isWhole, result));
		return ""+result;
	}
	
	/**
	 * Assumes 0 < percentile <= 100
	 * @param percentile
	 * @return
	 */
	public String getPercentileResponseTime(String label, int percentile) {
		List<Long> labeledRTimes = separatedRTimes.get(label);
		if (labeledRTimes == null || labeledRTimes.size() == 0) return "";
		
		Collections.sort(labeledRTimes);
		double index = (float)percentile/100 * labeledRTimes.size();
		double finalIndex = Math.ceil(index);
		boolean isWhole = finalIndex == index;
		long result = -1L;
		if(isWhole) {
			result = (labeledRTimes.get((int)finalIndex-1)+labeledRTimes.get((int)finalIndex))/2;
		} else {
			result = labeledRTimes.get((int)finalIndex-1);
		}
		//StringBuffer sb = new StringBuffer();
		//for(long l : labeledRTimes) {
		//	sb.append(l+", ");
		//}
		//System.out.println("Samples:  " + sb);
		//System.out.println(String.format("Calc Percentile:  percent=[%s], size=[%s], index=[%s], isWhole=[%s], result=[%s]", percentile, labeledRTimes.size(), index, isWhole, result));
		return ""+result;
	}
	

	public void trackTomcatSession(int numOfSessions) {
		this.numOfSessions++;
		sessionTotal += numOfSessions;
	}

	public String getSessionAverage() {
		if(numOfSessions != 0) return "" + (sessionTotal / numOfSessions);
		return "";
	}

	public void trackCpuUsage(String server, double cpuUsage) {
		if(!this.cpuUsage.containsKey(server)) {
			this.cpuUsage.put(server, new ArrayList<Double>());
		}
		this.cpuUsage.get(server).add(cpuUsage);
	}
	
	public String getAvgCpuUsage(String server) {
		if (this.cpuUsage.size() == 0) return "";
		List<Double> cpuUsages = this.cpuUsage.get(server);
		if(cpuUsages == null) {
			System.out.printf("Interesting - no cpu data for server [%s] during [%s]\n", server, this.pointInTime);
			return "";
		} else {
			BigDecimal sum = new BigDecimal(0);
			for(Double d : cpuUsages) {
				sum = sum.add(new BigDecimal(d));
			}
			return StrUtils.getDecimalFormatter().format(sum.doubleValue() / cpuUsages.size());
		}
	}
	
	/**
	 * Uses both servers.
	 * @return
	 */
	public String getAvgCpuUsage() {
		int count = 0;
		BigDecimal sum = new BigDecimal(0);
		if (this.cpuUsage.size() == 0) return "";
		for(String key : this.cpuUsage.keySet()) {
			List<Double> cpuUsages = this.cpuUsage.get(key);
			for(Double d : cpuUsages) {
				sum = sum.add(new BigDecimal(d));
				count++;
			}
		}
		return StrUtils.getDecimalFormatter().format(sum.doubleValue() / count);
	}

	public void trackGcActivity(String server, double gcActivity) {
		if(!this.gcActivity.containsKey(server)) {
			this.gcActivity.put(server, new ArrayList<Double>());
		}
		this.gcActivity.get(server).add(gcActivity);
	}
	
	public String getAvgGcActivity(String server) {
		if (this.gcActivity.size() == 0) return "";
		List<Double> gcActivities = this.gcActivity.get(server);
		if(gcActivities == null) {
			System.out.printf("Interesting - no gc data for server [%s] during [%s]\n", server, this.pointInTime);
			return "";
		} else {
			BigDecimal sum = new BigDecimal(0);
			for(Double d : gcActivities) {
				sum = sum.add(new BigDecimal(d));
			}
			return StrUtils.getDecimalFormatter().format(sum.doubleValue() / gcActivities.size());
		}
	}
	
	/**
	 * Uses both servers.
	 * @return
	 */
	public String getAvgGcActivity() {
		int count = 0;
		BigDecimal sum = new BigDecimal(0);
		if (this.gcActivity.size() == 0) return "";
		for(String key : this.gcActivity.keySet()) {
			List<Double> gcActivities = this.gcActivity.get(key);
			for(Double d : gcActivities) {
				sum = sum.add(new BigDecimal(d));
				count++;
			}
		}
		return StrUtils.getDecimalFormatter().format(sum.doubleValue() / count);
	}
	
	private int count(String sampler, Map<String, Integer> map) {
		if((sampler == null) || sampler.isEmpty()) {
			//Count all
			int sum = 0;
			for(String s : map.keySet()) {
				sum += map.get(s);
			}
			return sum;
		} else {
			//Count only the sampler's amount
			if(map.containsKey(sampler)) {
				return map.get(sampler);
			} else {
				return 0;
			}
		}
	}
	
	/**
	 * 
	 * @param sampler  null or empty will return all errors.
	 * @return
	 */
	public String getErrors(String sampler) {
		if(count(sampler, this.separatedTrans) == 0) return "";
		return "" + count(sampler, this.separatedErrors);
	}

	public void addResponseTime(String label, Long elapsedMillis) {
		if(!separatedRTimes.containsKey(label)) {
			separatedRTimes.put(label, new ArrayList<Long>());
		} 
		separatedRTimes.get(label).add(elapsedMillis);
	}
	
}
