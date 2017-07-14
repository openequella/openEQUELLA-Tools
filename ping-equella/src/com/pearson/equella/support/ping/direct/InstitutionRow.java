package com.pearson.equella.support.ping.direct;

public class InstitutionRow {
	private int id;
	private String shortname;
	private String name;
	private String url;
	private String uniqueId;
	private String filestoreHandle;
	
	public int getId() {
		return id;
	}
	public void setId(int i) {
		this.id = i;
	}
	public String getShortname() {
		return shortname;
	}
	public void setShortname(String shortname) {
		this.shortname = shortname;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public String getUniqueId() {
		return uniqueId;
	}
	public void setUniqueId(String uniqueId) {
		this.uniqueId = uniqueId;
	}
	
	public String toString() {
		return String.format("InstitutionRow:  ID=[%s], shortname=[%s], name=[%s], url=[%s], uniqueId=[%s]",id, shortname, name, url, uniqueId);
	}
	public String getFilestoreHandle() {
		return filestoreHandle;
	}
	public void setFilestoreHandle(String filestoreHandle) {
		this.filestoreHandle = filestoreHandle;
	}
}
