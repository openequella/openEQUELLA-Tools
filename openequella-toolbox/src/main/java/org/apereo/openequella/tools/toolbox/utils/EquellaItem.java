/*
 * Copyright 2018 Apereo
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apereo.openequella.tools.toolbox.utils;

import java.util.Date;

import org.json.JSONObject;

public class EquellaItem {
	private String uuid;
	private int version;
	private String name;
	private String description;
	private String filepath;
	private String kalturaMediaId;
	private JSONObject json;
	private String metadata;
	private String kalturaTags;
	private String createdDateStr;
	private Date createdDate;
	private String primaryFileType;
	
	public String getKalturaMediaId() {
		return kalturaMediaId;
	}
	public void setKalturaMediaId(String kalturaMediaId) {
		this.kalturaMediaId = kalturaMediaId;
	}
	public String getUuid() {
		return uuid;
	}
	public void setUuid(String uuid) {
		this.uuid = uuid;
	}
	public int getVersion() {
		return version;
	}
	public void setVersion(int version) {
		this.version = version;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public String getFilepath() {
		return filepath;
	}
	public void setFilepath(String filepath) {
		this.filepath = filepath;
	}
	
	public String toString() {
		return name + ": Desc=[" + description + "], UUID=" + uuid + ", Version=" + version;
	}
	
	public String getSignature() {
		return String.format("%s/%s: [%s]", uuid, version, name);
	}
	
	public void setJson(JSONObject resourceObj) {
		this.json = resourceObj;
	}
	public JSONObject getJson() {
		return this.json;
	}
	public String getMetadata() {
		return metadata;
	}
	public void setMetadata(String metadata) {
		this.metadata = metadata;
	}
	public String getKalturaTags() {
		return kalturaTags;
	}
	public void setKalturaTags(String kalturaTags) {
		this.kalturaTags = kalturaTags;
	}
	public Date getCreatedDate() {
		return createdDate;
	}
	public void setCreatedDate(Date date) {
		createdDate = date;
	}
	public void setPrimaryFileType(String suffix) {
		primaryFileType = suffix;
	}
	
	public String getPrimaryFileType() {
		return primaryFileType;
	}
	
}
