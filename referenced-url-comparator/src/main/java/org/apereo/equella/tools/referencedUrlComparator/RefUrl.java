/*
 * Copyright 2019 Apereo
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

package org.apereo.equella.tools.referencedUrlComparator;

public class RefUrl {
	private String url;
	private String status;
	private String success;
	private String tries;
	private String message;

	public RefUrl(String url, String status, String success, String tries, String message) {
		this.url = url;
		this.status = status;
		this.success = success;
		this.tries = tries;
		this.message = message;
	}

	public String getUrl() {
		return url;
	}

	public String getStatus() {
		return status;
	}

	public String getSuccess() {
		return success;
	}

	public String getTries() {
		return tries;
	}

	public String getMessage() {
		return message;
	}

	public boolean isHeader() {
		return url.equals("url") && status.equals("status") && success.equals("success") && tries.equals("tries")
				&& message.equals("message");
	}
	
	public String toString() {
		return String.format("URL: [%s], %s", url, toStringNoUrl());
	}

	public Object toStringNoUrl() {
		return String.format("Status: [%s], Success: [%s], Tries: [%s], Message: [%s]", status, success, tries, message);
	}
}
