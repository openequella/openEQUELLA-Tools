package com.pearson.equella.support.ping.direct;

public class ResultsRow {
	public static final String PRESENT = "Present";
	public static final String MISSING = "Missing";
	public static final String UNKNOWN = "Unknown";
	public static final String IGNORED = "Ignored";
	public static final String NOATT = "No Att";

	private int institutionId = Integer.MIN_VALUE;
	private int itemId = Integer.MIN_VALUE;
	private String institutionShortname = "[[Institution shortname not set]]";
	private String itemUuid = "[[Item UUID not set]]";
	private String itemVersion = "[[Item version not set]]";
	private String itemName = "[[Item name not set]]";
	private String itemStatus = "[[Item status not set]]";
	private String collectionUuid = "[[Collection uuid not set]]";
	private String attType = "[[Attachment type not set]]";
	private String attFilePath = "[[Attachment file path not set]]";
	private String attUuid = "[[Attachment UUID not set]]";
	private int attId = Integer.MIN_VALUE;
	private String attStatus = "[[Attachment status not set]]";
	private String attRespCode = "[[Attachment resp code not set]]";
	private String sep = ",";
	private String attUrl = "[[Attachment URL not set]]";

	public static String getHeader() {
		return "Institution Shortname,Collection UUID,Item UUID,Item Version,ItemStatus,"
				+ "Attachment Type,Attachment UUID,Attachment Status,Attachment Response Code,"
				+ "Item Name,Attachment Filepath";
	}

	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append(institutionShortname).append(sep);
		sb.append(collectionUuid).append(sep);
		sb.append(itemUuid).append(sep);
		sb.append(itemVersion).append(sep);
		sb.append(itemStatus).append(sep);
		sb.append(attType).append(sep);
		sb.append(attUuid).append(sep);
		sb.append(attStatus).append(sep);
		sb.append(attRespCode).append(sep);
		sb.append("\"").append(itemName).append("\"").append(sep);
		sb.append("\"").append(attFilePath).append("\"").append(sep);
		return sb.toString();
	}

	public void setItemUuid(String itemUuid) {
		this.itemUuid = itemUuid;
	}

	public void setItemVersion(String itemVersion) {
		this.itemVersion = itemVersion;
	}

	public void setItemName(String itemName) {
		this.itemName = itemName;
	}

	public void setItemStatus(String itemStatus) {
		this.itemStatus = itemStatus;
	}

	public void setCollectionUuid(String collectionUuid) {
		this.collectionUuid = collectionUuid;
	}

	public void setAttType(String attType) {
		this.attType = attType;
	}

	public void setAttFilePath(String attFilePath) {
		this.attFilePath = attFilePath;
	}

	public void setAttUuid(String attUuid) {
		this.attUuid = attUuid;
	}

	public void setAttStatus(String attStatus) {
		this.attStatus = attStatus;
	}

	public void setAttRespCode(String string) {
		this.attRespCode = string;
	}

	public static ResultsRow buildItemFrame(ResultsRow itemRow) {
		ResultsRow r = new ResultsRow();
		r.setItemName(itemRow.itemName);
		r.setItemStatus(itemRow.itemStatus);
		r.setItemUuid(itemRow.itemUuid);
		r.setItemVersion(itemRow.itemVersion);
		r.setCollectionUuid(itemRow.collectionUuid);
		r.setInstitutionShortname(itemRow.institutionShortname);
		r.setItemId(itemRow.itemId);
		r.setInstitutionId(itemRow.institutionId);
		return r;
	}

	public String getAttType() {
		return attType;
	}

	public String getAttFilePath() {
		return attFilePath;
	}

	public String getAttUuid() {
		return attUuid;
	}

	public String getAttRespCode() {
		return attRespCode;
	}

	public String getAttUrl() {
		return attUrl;
	}

	public String getAttStatus() {
		return attStatus;
	}

	public void setAttUrl(String attUrl) {
		this.attUrl = attUrl;
	}

	public String getItemUuid() {
		return itemUuid;
	}

	public String getItemVersion() {
		return itemVersion;
	}

	public String getCollectionUuid() {
		return collectionUuid;
	}

	public int getItemId() {
		return itemId;
	}

	public void setItemId(int itemId) {
		this.itemId = itemId;
	}

	public String getPrimaryKey() {
		return String.format("%s-%s-%s-%s-%s", this.institutionShortname,
				this.itemUuid, this.itemVersion, this.attUuid, this.attStatus);
	}

	public int getAttId() {
		return attId;
	}

	public void setAttId(int attId) {
		this.attId = attId;
	}

	public void setInstitutionId(int id) {
		this.institutionId = id;
	}

	public int getInstitutionId() {
		return this.institutionId;
	}

	public String getInstitutionShortname() {
		return institutionShortname;
	}

	public void setInstitutionShortname(String institutionShortname) {
		this.institutionShortname = institutionShortname;
	}
}
