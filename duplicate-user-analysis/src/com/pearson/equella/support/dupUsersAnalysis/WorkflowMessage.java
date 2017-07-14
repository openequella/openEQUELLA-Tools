package com.pearson.equella.support.dupUsersAnalysis;

import java.sql.ResultSet;
import java.sql.SQLException;

public class WorkflowMessage implements GenericEntity {
	private String id;
	private String date;
	private String message;
	private String type;
	private String user;
	private String nodeId;
	
	public WorkflowMessage(ResultSet rs) throws SQLException {
		this.id = rs.getString(1);
		this.date = rs.getString(2);
		this.message = rs.getString(3);
		this.type = rs.getString(4);
		this.user = rs.getString(5);
		this.nodeId = rs.getString(6);
	}
	public String toString() {
		return String.format("Id=[%s], Date=[%s], Message=[%s], Type=[%s], User=[%s], NodeId=[%s]", 
				id, date, message, type, user, nodeId);
	}
}
