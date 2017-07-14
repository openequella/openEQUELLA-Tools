package com.pearson.equella.support.dupUsersAnalysis;

public class EqUser {
	private String uuid;
	private String id;
	private String username;
	private String email;
	private String fname;
	private String lname;
	private String institutionId;
	private String password;
	
	public String getUuid() {
		return uuid;
	}
	public void setUuid(String uuid) {
		this.uuid = uuid;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public String getFname() {
		return fname;
	}
	public void setFname(String fname) {
		this.fname = fname;
	}
	public String getLname() {
		return lname;
	}
	public void setLname(String lname) {
		this.lname = lname;
	}
	public String getInstitutionId() {
		return institutionId;
	}
	public void setInstitutionId(String institutionId) {
		this.institutionId = institutionId;
	}
	
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}

	public String toString() {
		return String.format("InstID=[%s], ID=[%s], UUID=[%s], Username=[%s], FirstName=[%s], LastName=[%s], Email=[%s], PW=[%s]", institutionId, id, uuid, username, fname, lname, email, password);
	}
}
