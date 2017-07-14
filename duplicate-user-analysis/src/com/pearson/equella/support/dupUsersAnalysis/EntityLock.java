package com.pearson.equella.support.dupUsersAnalysis;

import java.sql.ResultSet;
import java.sql.SQLException;

public class EntityLock implements GenericEntity {
	private String id;
	private String userId;
	private String userSession;
	private String entityId;
	private String instId;
	
	public EntityLock(ResultSet rs) throws SQLException {
		this.id = rs.getString(1);
		this.userId = rs.getString(2);
		this.userSession = rs.getString(3);
		this.entityId = rs.getString(4);
		this.instId = rs.getString(5);
	}
	public String toString() {
		return String.format("Id=[%s], UserId=[%s], UserSession=[%s], EntityId=[%s], InstId=[%s]", 
				id, userId, userSession, entityId, instId);
	}
}
