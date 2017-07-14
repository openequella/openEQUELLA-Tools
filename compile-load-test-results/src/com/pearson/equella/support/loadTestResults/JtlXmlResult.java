package com.pearson.equella.support.loadTestResults;

import java.util.Calendar;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;

public class JtlXmlResult extends GenericResult {
	private String testType = "";
	private Long elapsedMillis = -1L;
	private Long idleMillis = -1L;
	private Long latencyMillis = -1L;
	private boolean success = false;
	private String label = null;
	private int responseCode = -1;
	private String responseMessage = null;
	private String threadName = null;
	private String dataType = null;
	private long bytes = -1L;
	private int sampleCount = -1;
	private int errorCount = -1;
	private int numOfActiveGroupThreads = -1;
	private int numOfActiveThreads = -1;

	/**
	 * Example format: <httpSample t="444" it="0" lt="426" ts="1479915709785"
	 * s="true" lb=" OAuth Auth Code Request " rc="200"
	 * rm="OK" tn="JMeter Users 1-1" dt="text" by="5129" sc="1" ec="0" ng="1"
	 * na="1">
	 * 
	 * @param resultElem
	 * @throws Exception
	 */
	public static JtlXmlResult consumeAtts(String testType, Map<String,String> atts) throws Exception {
		JtlXmlResult res = new JtlXmlResult();
		res.testType = testType;
		res.elapsedMillis = Long.parseLong(pullReqStringAtt(atts, "t"));
		res.idleMillis = Long.parseLong(pullReqStringAtt(atts, "it"));
		res.latencyMillis = Long.parseLong(pullReqStringAtt(atts, "lt"));
		res.timestamp = Calendar.getInstance(TimeZone.getTimeZone("MST"));
		res.timestamp.setTimeInMillis(Long.parseLong(pullReqStringAtt(
				atts, "ts")));
		res.success = Boolean.parseBoolean(pullReqStringAtt(atts, "s"));
		res.label = pullReqStringAtt(atts, "lb");
		//res.responseCode = Integer.parseInt(pullReqStringAtt(resultElem, "rc"));
		res.responseMessage = pullReqStringAtt(atts, "rm");
		res.threadName = pullReqStringAtt(atts, "tn");
		res.dataType = pullReqStringAtt(atts, "dt");
		res.bytes = Long.parseLong(pullReqStringAtt(atts, "by"));
		res.sampleCount = Integer.parseInt(pullReqStringAtt(atts, "sc"));
		res.errorCount = Integer.parseInt(pullReqStringAtt(atts, "ec"));
		res.numOfActiveGroupThreads = Integer.parseInt(pullReqStringAtt(
				atts, "ng"));
		res.numOfActiveThreads = Integer.parseInt(pullReqStringAtt(atts,
				"na"));
		return res;
	}

	private static String pullReqStringAtt(Map<String,String> elem, String id)
			throws Exception {
		if (elem.containsKey(id)) {
			return elem.get(id);
		} else {
			
			Set<String> keys = elem.keySet();
			StringBuffer sb = new StringBuffer();
			for (String key : keys) {
				sb.append(key);
				sb.append("=");
				sb.append(elem.get(key));
				sb.append(", ");
			}
			throw new Exception(String.format(
					"Element [%s] did not have expected attribute [%s]",
					sb.toString(), id));
		}
	}

	public Long getElapsedMillis() {
		return elapsedMillis;
	}

	public Long getIdleMillis() {
		return idleMillis;
	}

	public Long getLatencyMillis() {
		return latencyMillis;
	}


	public boolean isSuccess() {
		return success;
	}

	public String getLabel() {
		return label;
	}

	public int getResponseCode() {
		return responseCode;
	}

	public String getResponseMessage() {
		return responseMessage;
	}

	public String getThreadName() {
		return threadName;
	}

	public String getDataType() {
		return dataType;
	}

	public long getBytes() {
		return bytes;
	}

	public int getSampleCount() {
		return sampleCount;
	}

	public int getErrorCount() {
		return errorCount;
	}

	public int getNumOfActiveGroupThreads() {
		return numOfActiveGroupThreads;
	}

	public int getNumOfActiveThreads() {
		return numOfActiveThreads;
	}

	public void setNumOfActiveThreads(String string) {
		numOfActiveThreads = Integer.parseInt(string);
	}

	public void setBytes(String string) {
		bytes = Long.parseLong(string);
	}

	public void setSuccess(String string) {
		success = Boolean.parseBoolean(string);
	}

	public void setElapsedMillis(String string) {
		elapsedMillis = Long.parseLong(string);
	}

	public void setLabel(String string) {
		this.label = string;
	}

	public String getTestType() {
		return testType;
	}
	
	public void setTestType(String tt) {
		this.testType = tt;
	}

}
