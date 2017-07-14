package com.pearson.equella.support;

public class Result {
	public enum Status {
		BadItemId,
		BadUrlId,
		DbConfirmedPartialRemove,
		DbConfirmedFullRemove,
		BadAssociations,
		IP
	}
	private long itemId;
	private long referencedUrlId;
	private Status stat;
	private String uuidVer = "ERROR";
	
	public Result(long iId, long ruId, Status s) {
		this.itemId = iId;
		this.referencedUrlId = ruId;
		this.stat = s;
	}

	public long getItemId() {
		return itemId;
	}

	public long getReferencedUrlId() {
		return referencedUrlId;
	}

	public Status getStat() {
		return stat;
	}

	public void setStat(Status stat) {
		this.stat = stat;
	}

	public void setAffectItemUuidVersion(String uuidVer) {
		this.uuidVer = uuidVer;
	}
	
	public String getAffectItemUuidVersion() {
		return uuidVer;
	}
}
