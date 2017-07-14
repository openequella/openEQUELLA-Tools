package com.pearson.equella.support.dupUsersAnalysis;

import java.sql.ResultSet;
import java.sql.SQLException;

public class DrmAcceptance implements GenericEntity {
	private String id;
	private String date;
	private String user;
	private String itemId;
	
	public DrmAcceptance(ResultSet rs) throws SQLException {
		this.id = rs.getString(1);
		this.date = rs.getString(2);
		this.user = rs.getString(3);
		this.itemId = rs.getString(4);
	}
	public String toString() {
		return String.format("Id=[%s], Date=[%s], User=[%s], ItemId=[%s]", 
				id, date, user, itemId);
	}
}
