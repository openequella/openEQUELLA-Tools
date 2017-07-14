package com.pearson.equella.support.dupUsersAnalysis;

import java.sql.ResultSet;
import java.sql.SQLException;

public class TleGroupUser implements GenericEntity {
	private String tlegroupId;
	private String element;
	
	public TleGroupUser(ResultSet rs) throws SQLException {
		this.tlegroupId = rs.getString(1);
		this.element = rs.getString(2);
	}
	public String toString() {
		return String.format("TleGroupId=[%s], Element=[%s]", 
				tlegroupId, element);
	}
}
