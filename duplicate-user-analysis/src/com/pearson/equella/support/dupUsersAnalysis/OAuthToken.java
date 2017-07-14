package com.pearson.equella.support.dupUsersAnalysis;

import java.sql.ResultSet;
import java.sql.SQLException;

public class OAuthToken implements GenericEntity {
	private String id;
	private String code;
	private String created;
	private String expiry;
	private String token;
	private String userId;
	private String username;
	private String clientId;
	private String institutionId;
	
	public OAuthToken(ResultSet rs) throws SQLException {
		this.id = rs.getString(1);
		this.code = rs.getString(2);
		this.created = rs.getString(3);
		this.expiry = rs.getString(4);
		this.token = rs.getString(5);
		this.userId = rs.getString(6);
		this.username = rs.getString(7);
		this.clientId = rs.getString(8);
		this.institutionId = rs.getString(9);
	}
	public String toString() {
		return String.format("Id=[%s], Code=[%s], Created=[%s], " +
				"Expiry=[%s], Token=[%s], UserId=[%s], Username=[%s], ClientId=[%s], InstId=[%s]", 
				id, code, created, expiry, token, userId, username, clientId, institutionId);
	}
}
