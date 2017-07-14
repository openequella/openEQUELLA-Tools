package com.pearson.equella.support.institutionsurgery.driver;

public class CollectionBundle {
	private String uuid;
	private String name;
	private String workflowUuid;

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

	public String getWorkflowUuid() {
		return workflowUuid;
	}

	public void setWorkflowUuid(String workflowUuid) {
		this.workflowUuid = workflowUuid;
	}

	public String toString() {
		return String.format("Collection Bundle:  uuid=[%s], name=[%s], workflow-uuid=[%s]", uuid, name, workflowUuid);
	}
}
