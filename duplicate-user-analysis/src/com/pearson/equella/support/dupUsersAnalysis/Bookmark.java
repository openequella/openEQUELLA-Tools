package com.pearson.equella.support.dupUsersAnalysis;

import java.sql.ResultSet;
import java.sql.SQLException;

public class Bookmark implements GenericEntity {
	private String id;
	private String alwaysLatest;
	private String dateModified;
	private String owner;
	private String institutionId;
	private String itemId;
	
	public Bookmark(ResultSet rs) throws SQLException {
		this.id = rs.getString(1);
		this.alwaysLatest = rs.getString(2);
		this.dateModified = rs.getString(3);
		this.owner = rs.getString(4);
		this.institutionId = rs.getString(5);
		this.itemId = rs.getString(6);
	}
	public String toString() {
		return String.format("Id=[%s], AlwaysLatest=[%s], DateModified=[%s], " +
				"Owner=[%s], InstitutionID=[%s], ItemId=[%s]", 
				id, alwaysLatest, dateModified, owner, institutionId, itemId);
	}
}
