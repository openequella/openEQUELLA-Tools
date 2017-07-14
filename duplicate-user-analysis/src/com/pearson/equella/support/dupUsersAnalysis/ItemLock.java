package com.pearson.equella.support.dupUsersAnalysis;

import java.sql.ResultSet;
import java.sql.SQLException;

public class ItemLock implements GenericEntity {
	private String id;
	private String userId;
	private String userSession;
	private String instId;
	private String itemId;
	
	public ItemLock(ResultSet rs) throws SQLException {
		this.id = rs.getString(1);
		this.userId = rs.getString(2);
		this.userSession = rs.getString(3);
		this.instId = rs.getString(4);
		this.itemId = rs.getString(5);
	}
	public String toString() {
		return String.format("Id=[%s], UserId=[%s], UserSession=[%s], InstId=[%s], ItemId=[%s]", 
				id, userId, userSession, instId, itemId);
	}
}
