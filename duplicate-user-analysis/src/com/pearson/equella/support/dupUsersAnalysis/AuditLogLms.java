package com.pearson.equella.support.dupUsersAnalysis;

public class AuditLogLms implements GenericEntity {
	private String id;
	private String contentType;
	private String latest;
	private String resource;
	private String selected;
	private String sessionId;
	private String timestamp;
	private String type;
	private String userId;
	private String uuid;
	private String version;
	private String institutionId;
	
	public String toString() {
		return String.format("InstID=[%s], ID=[%s], contentType=[%s], " +
				"Latest=[%s], Resource=[%s], Selected=[%s], " +
				"SessionId=[%s], Timestamp=[%s], Type=[%s], " +
				"UserId=[%s], Uuid=[%s], Version=[%s]", 
				institutionId, id, contentType, latest, resource, selected,
				sessionId, timestamp, type, userId, uuid, version);
	}
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getContentType() {
		return contentType;
	}
	public void setContentType(String contentType) {
		this.contentType = contentType;
	}
	public String getLatest() {
		return latest;
	}
	public void setLatest(String latest) {
		this.latest = latest;
	}
	public String getResource() {
		return resource;
	}
	public void setResource(String resource) {
		this.resource = resource;
	}
	public String getSelected() {
		return selected;
	}
	public void setSelected(String selected) {
		this.selected = selected;
	}
	public String getSessionId() {
		return sessionId;
	}
	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}
	public String getTimestamp() {
		return timestamp;
	}
	public void setTimestamp(String timestamp) {
		this.timestamp = timestamp;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getUserId() {
		return userId;
	}
	public void setUserId(String userId) {
		this.userId = userId;
	}
	public String getUuid() {
		return uuid;
	}
	public void setUuid(String uuid) {
		this.uuid = uuid;
	}
	public String getVersion() {
		return version;
	}
	public void setVersion(String version) {
		this.version = version;
	}
	public String getInstitutionId() {
		return institutionId;
	}
	public void setInstitutionId(String institutionId) {
		this.institutionId = institutionId;
	}
	
	
}
