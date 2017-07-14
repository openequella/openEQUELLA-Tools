package com.pearson.equella.support.dupUsersAnalysis;

import java.sql.ResultSet;
import java.sql.SQLException;

public class OAuthClient implements GenericEntity {
	private String id;
	private String userId;
	private String clientId;
	private String redirectUrl;
	private String requiresApproval;
	
	public OAuthClient(ResultSet rs) throws SQLException {
		this.id = rs.getString(1);
		this.userId = rs.getString(2);
		this.clientId = rs.getString(3);
		this.redirectUrl = rs.getString(4);
		this.requiresApproval = rs.getString(5);
	}
	public String toString() {
		return String.format("Id=[%s], UserId=[%s], ClientId=[%s], " +
				"RedirectUrl=[%s], RequiresApproval=[%s]", 
				id, userId, clientId, redirectUrl, requiresApproval);
	}
}
