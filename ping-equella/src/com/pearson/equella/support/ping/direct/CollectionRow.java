package com.pearson.equella.support.ping.direct;

public class CollectionRow {

	private int id = Integer.MIN_VALUE;
	private int institutionId = Integer.MIN_VALUE;
	private String uuid = "";
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public int getInstitutionId() {
		return institutionId;
	}
	public void setInstitutionId(int institutionId) {
		this.institutionId = institutionId;
	}
	public String getUuid() {
		return uuid;
	}
	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	public String toString() {
		return String.format("CollectionRow: ID=[%d],  InstitutionID=[%d], UUID=[%s]", id, institutionId, uuid);
	}
}
