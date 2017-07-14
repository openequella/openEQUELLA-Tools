package com.pearson.equella.support.loadTestResults;

import java.text.ParseException;
import java.util.Calendar;

public abstract class GenericResult {
	protected Calendar timestamp = null;
	protected String timeSlice = null;
	
	public String getTimeSlice() {
		if(timeSlice != null) return timeSlice;
		return getAndPad(Calendar.YEAR, 4)+"-"+
				getAndPad(Calendar.MONTH, 2, 1)+"-"+
				getAndPad(Calendar.DAY_OF_MONTH, 2)+" "+
				getAndPad(Calendar.HOUR_OF_DAY, 2)+":"+
				getAndPad(Calendar.MINUTE, 2)+" MST";
		//+":"+getAndPad(Calendar.SECOND, 2) 
	}

	private String getAndPad(int facet, int padding, int adjustment) {
		String res = "" + (timestamp.get(facet)+adjustment);
		while (res.length() < padding) {
			res = "0" + res;
		}
		return res;
	}
	
	private String getAndPad(int facet, int padding) {
		String res = "" + timestamp.get(facet);
		while (res.length() < padding) {
			res = "0" + res;
		}
		return res;
	}
	

	public Calendar getTimestamp() {
		return timestamp;
	}
	
	
	//The stringified short version of the timestamp.
	public void setTimeSlice(String string) throws ParseException {
		timeSlice = string;
	}

}
