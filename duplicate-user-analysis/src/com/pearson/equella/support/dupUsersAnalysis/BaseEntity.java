package com.pearson.equella.support.dupUsersAnalysis;

import java.sql.ResultSet;
import java.sql.SQLException;

public class BaseEntity implements GenericEntity {
	private String id;
	private String dateCreated;
	private String dateModified;
	private String disabled;
	private String owner;
	private String systemType;
	private String uuid;
	private String descriptionId;
	private String institutionId;
	private String nameId;
	
	public BaseEntity(ResultSet rs) throws SQLException {
		this.id = rs.getString(1);
		this.dateCreated = rs.getString(2);
		this.dateModified = rs.getString(3);
		this.disabled = rs.getString(4);
		this.owner = rs.getString(5);
		this.systemType = rs.getString(6);
		this.uuid = rs.getString(7);
		this.descriptionId = rs.getString(8);
		this.institutionId = rs.getString(9);
		this.nameId = rs.getString(10);
	}
	public String toString() {
		return String.format("Id=[%s], DateCreated=[%s], DateModified=[%s], Disabled=[%s], " +
				"Owner=[%s], SystemType=[%s], UUID=[%s], " +
				"DescriptionId=[%s], InstitutionID=[%s], NameId=[%s]", 
				id, dateCreated, dateModified, disabled, owner, systemType, uuid, descriptionId, institutionId, nameId);
	}
}
