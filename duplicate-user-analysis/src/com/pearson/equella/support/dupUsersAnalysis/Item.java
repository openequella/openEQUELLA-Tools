package com.pearson.equella.support.dupUsersAnalysis;

import java.sql.ResultSet;
import java.sql.SQLException;

public class Item implements GenericEntity {
	private String id;
	private String dateCreated;
	private String dateForIndex;
	private String dateModified;
	private String owner;
	private String status;
	private String uuid;
	private String version;
	private String descriptionId;
	private String institutionId;
	private String itemXmlId;
	private String nameId;
	
	public Item(ResultSet rs) throws SQLException {
		this.id = rs.getString(1);
		this.dateCreated = rs.getString(2);
		this.dateForIndex = rs.getString(3);
		this.dateModified = rs.getString(4);
		this.owner = rs.getString(5);
		this.status = rs.getString(6);
		this.uuid = rs.getString(7);
		this.version = rs.getString(8);
		this.descriptionId = rs.getString(9);
		this.institutionId = rs.getString(10);
		this.itemXmlId = rs.getString(11);
		this.nameId = rs.getString(12);
	}
	public String toString() {
		return String.format("Id=[%s], DateCreated=[%s], DateForIndex=[%s], DateModified=[%s], " +
				"Owner=[%s], Status=[%s], Uuid=[%s], Version=[%s], DescriptionId=[%s], " +
				"InstitutionId=[%s], ItemXmlId=[%s], NameId=[%s]", 
				id, dateCreated, dateForIndex, dateModified, owner, status, uuid, 
				version, descriptionId, institutionId, itemXmlId, nameId);
	}
}
