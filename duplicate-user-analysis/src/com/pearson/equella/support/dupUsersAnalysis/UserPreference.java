package com.pearson.equella.support.dupUsersAnalysis;

import java.sql.ResultSet;
import java.sql.SQLException;

public class UserPreference implements GenericEntity {
	private String institution;
	private String preferenceId;
	private String userId;
	private String data;
	
	public UserPreference(ResultSet rs) throws SQLException {
		this.institution = rs.getString(1);
		this.preferenceId = rs.getString(2);
		this.userId = rs.getString(3);
		this.data = rs.getString(4);
	}
	public String toString() {
		return String.format("Institution=[%s], PreferenceId=[%s], UserId=[%s], Data=[%s]", 
				institution, preferenceId, userId, data);
	}
}
