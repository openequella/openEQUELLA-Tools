package com.pearson.equella.support.loadTestResults;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.TimeZone;


public class TomcatSessionResult extends GenericResult {
	private String round;
	
	public TomcatSessionResult(String round) {
		this.round = round;
	}
	
	SimpleDateFormat sdf = new SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy");
	private int numOfSessions = -1;
	
	public int getNumOfSessions() {
		return numOfSessions;
	}
	public void setNumOfSessions(int numOfSessions) {
		this.numOfSessions = numOfSessions;
	}
	
	public String getRound() {
		return this.round;
	}
	
	//Expects a format like:  "Mon Dec 05 08:07:44 MST 2016"
	@Override
	public void setTimeSlice(String raw) throws ParseException {
		timestamp = Calendar.getInstance(TimeZone.getTimeZone("MST"));
		timestamp.setTimeInMillis(sdf.parse(raw).getTime());
	}
}
