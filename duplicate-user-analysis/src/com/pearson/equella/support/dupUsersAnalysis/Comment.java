package com.pearson.equella.support.dupUsersAnalysis;

import java.sql.ResultSet;
import java.sql.SQLException;

public class Comment implements GenericEntity {
	private String id;
	private String anonymous;
	private String comment;
	private String dateCreated;
	private String owner;
	private String rating;
	private String uuid;
	private String itemId;
	
	public Comment(ResultSet rs) throws SQLException {
		this.id = rs.getString(1);
		this.anonymous = rs.getString(2);
		this.comment = rs.getString(3);
		this.dateCreated = rs.getString(4);
		this.owner = rs.getString(5);
		this.rating = rs.getString(6);
		this.uuid = rs.getString(7);
		this.itemId = rs.getString(8);
	}
	public String toString() {
		return String.format("Id=[%s], Anonymous=[%s], Comment=[%s], " +
				"DateCreated=[%s], Owner=[%s], Rating=[%s], UUID=[%s], ItemId=[%s]", 
				id, anonymous, comment, dateCreated, owner, rating, uuid, itemId);
	}
}
