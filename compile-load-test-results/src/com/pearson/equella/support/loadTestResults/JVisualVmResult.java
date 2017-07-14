package com.pearson.equella.support.loadTestResults;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.TimeZone;


public class JVisualVmResult extends GenericResult {
	
	//For "1/20/2017  12:44:09 AM": "M/d/yyyy  hh:mm:ss a"
	//For "7:10:28.193 AM, Dec 5, 2016": "HH:mm:ss.SSS a, MMM d, yyyy"
	public static final SimpleDateFormat timeFormatter = new SimpleDateFormat("hh:mm:ss.SSS a, MMM d, yyyy");
	private double cpuUsage = -1;
	private double gcActivity = -1;
	private String server = "ERR";
	private String round = "";
	
	public JVisualVmResult(String round) {
		this.round = round;
	}
	
	@Override
	public void setTimeSlice(String raw) throws ParseException {
		timestamp = Calendar.getInstance(TimeZone.getTimeZone("EST"));
		timestamp.setTimeInMillis(JVisualVmResult.timeFormatter.parse(raw).getTime());
	}
	
	public String getRound() {
		return this.round;
	}

	public double getCpuUsage() {
		return cpuUsage;
	}

	public void setCpuUsage(double cpuUsage) {
		this.cpuUsage = cpuUsage;
	}

	public double getGcActivity() {
		return gcActivity;
	}

	public void setGcActivity(double gcActivity) {
		this.gcActivity = gcActivity;
	}

	public void setServer(String server) {
		this.server = server;
	}
	
	public String getServer() {
		return this.server;
	}
}
