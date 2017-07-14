package com.pearson.equella.support.dupUsersAnalysis;

import java.sql.ResultSet;
import java.sql.SQLException;

public class PortletPreference implements GenericEntity {
	private String id;
	private String closed;
	private String minimised;
	private String order;
	private String position;
	private String userId;
	private String portletId;
	
	public PortletPreference(ResultSet rs) throws SQLException {
		this.id = rs.getString(1);
		this.closed = rs.getString(2);
		this.minimised = rs.getString(3);
		this.order = rs.getString(4);
		this.position = rs.getString(5);
		this.userId = rs.getString(6);
		this.portletId = rs.getString(7);
	}
	public String toString() {
		return String.format("Id=[%s], Closed=[%s], Minimised=[%s], Order=[%s], " +
				"Position=[%s], UserId=[%s], PortletId=[%s]", 
				id, closed, minimised, order, position, userId, portletId);
	}
}
