package com.pearson.equella.support.dupUsersAnalysis;

import java.sql.ResultSet;
import java.sql.SQLException;

public class FavSearch implements GenericEntity {
	private String id;
	private String criteria;
	private String dateMod;
	private String name;
	private String owner;
	private String query;
	private String url;
	private String within;
	private String instId;
	
	public FavSearch(ResultSet rs) throws SQLException {
		this.id = rs.getString(1);
		this.criteria = rs.getString(2);
		this.dateMod = rs.getString(3);
		this.name = rs.getString(4);
		this.owner = rs.getString(5);
		this.query = rs.getString(6);
		this.url = rs.getString(7);
		this.within = rs.getString(8);
		this.instId = rs.getString(9);
	}
	public String toString() {
		return String.format("Id=[%s], Criteria=[%s], DateModified=[%s], Name=[%s], " +
				"Owner=[%s], Query=[%s], URL=[%s], Within=[%s], InstId=[%s]", 
				id, criteria, dateMod, name, owner, query, url, within, instId);
	}
}
