package com.pearson.equella.support.institutionsurgery.driver;

public class ItemBundle {
	private String uuid;
	private String name;
	private String collectionUuid;
	private String associatedWorkflow;
	private String version;

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getCollectionUuid() {
		return collectionUuid;
	}

	public void setCollectionUuid(String collectionUuid) {
		this.collectionUuid = collectionUuid;
	}

	public String getAssociatedWorkflow() {
		return associatedWorkflow;
	}

	public void setAssociatedWorkflow(String associatedWorkflow) {
		this.associatedWorkflow = associatedWorkflow;
	}

	public String toString() {
		return String.format(
				"Item Bundle:  uuid/version=[%s/%s],  name=[%s], collectionUuid=[%s], associatedWorkflow=[%s]", uuid,
				version, name, collectionUuid, associatedWorkflow);
	}
}
