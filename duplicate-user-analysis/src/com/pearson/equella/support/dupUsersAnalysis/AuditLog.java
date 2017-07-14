package com.pearson.equella.support.dupUsersAnalysis;

import java.sql.ResultSet;
import java.sql.SQLException;

public class AuditLog implements GenericEntity {
	private String id;
	private String data1;
	private String data2;
	private String data3;
	private String data4;
	private String eventCategory;
	private String eventType;
	private String sessionId;
	private String timestamp;
	private String userId;
	private String institutionId;
	
	public AuditLog(ResultSet rs) throws SQLException {
		this.setId(rs.getString(1));
		this.setUserId(rs.getString(2));
		this.setInstitutionId(rs.getString(3));
		this.setSessionId(rs.getString(4));
		this.setData1(rs.getString(5));
		this.setData2(rs.getString(6));
		this.setData3(rs.getString(7));
		this.setData4(rs.getString(8));
		this.setEventCategory(rs.getString(9));
		this.setEventType(rs.getString(10));
		this.setTimestamp(rs.getString(11));
		
	}
	public String toString() {
		return String.format("UserId=[%s], InstID=[%s], ID=[%s], Data1=[%s], " +
				"Data2=[%s], Data3=[%s], Data4=[%s], " +
				"EventCategory=[%s], EventType=[%s], SessionId=[%s], " +
				"Timestamp=[%s]", 
				userId, institutionId, id, data1, data2, data3, data4, 
				eventCategory, eventType, sessionId, timestamp);
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getData1() {
		return data1;
	}
	public void setData1(String data1) {
		this.data1 = data1;
	}
	public String getData2() {
		return data2;
	}
	public void setData2(String data2) {
		this.data2 = data2;
	}
	public String getData3() {
		return data3;
	}
	public void setData3(String data3) {
		this.data3 = data3;
	}
	public String getData4() {
		return data4;
	}
	public void setData4(String data4) {
		this.data4 = data4;
	}
	public String getEventCategory() {
		return eventCategory;
	}
	public void setEventCategory(String eventCategory) {
		this.eventCategory = eventCategory;
	}
	public String getEventType() {
		return eventType;
	}
	public void setEventType(String eventType) {
		this.eventType = eventType;
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
	public String getUserId() {
		return userId;
	}
	public void setUserId(String userId) {
		this.userId = userId;
	}
	public String getInstitutionId() {
		return institutionId;
	}
	public void setInstitutionId(String institutionId) {
		this.institutionId = institutionId;
	}


}
