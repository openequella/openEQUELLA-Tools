package com.pearson.equella.support.dupUsersAnalysis;

import java.sql.ResultSet;
import java.sql.SQLException;

public class Notification implements GenericEntity {
	private String id;
	private String attemptId;
	private String batched;
	private String date;
	private String itemId;
	private String itemIdOnly;
	private String lastAttempt;
	private String processed;
	private String reason;
	private String userTo;
	private String instId;
	
	public Notification(ResultSet rs) throws SQLException {
		this.id = rs.getString(1);
		this.attemptId = rs.getString(2);
		this.batched = rs.getString(3);
		this.date = rs.getString(4);
		this.itemId = rs.getString(5);
		this.itemIdOnly = rs.getString(6);
		this.lastAttempt = rs.getString(7);
		this.processed = rs.getString(8);
		this.reason = rs.getString(9);
		this.userTo = rs.getString(10);
		this.instId = rs.getString(11);
	}
	public String toString() {
		return String.format("Id=[%s], AttemptId=[%s], Batched=[%s], " +
				"Date=[%s], ItemId=[%s], ItemIdOnly=[%s], LastAttempt=[%s], " +
				"Processed=[%s], Reason=[%s], UserTo=[%s], InstId=[%s] ", 
				id, attemptId, batched, date, itemId, itemIdOnly, lastAttempt, 
				processed, reason, userTo, instId);
	}
}
