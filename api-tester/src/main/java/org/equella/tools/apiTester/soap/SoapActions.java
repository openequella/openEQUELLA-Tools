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

package org.equella.tools.apiTester.soap;

import java.util.UUID;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@SuppressWarnings("nls")
public class SoapActions {
	private Logger logger = LogManager.getLogger(SoapActions.class);

	private final String endpoint;
	private final String username;
	private final String password;

	private final String itemUuid;
	private final int itemVersion;

	public SoapActions(String url, String username, String password, String itemUuid, int itemVersion) {
		this.endpoint = url + "/services/SoapService51";
		this.username = username;
		this.password = password;
		this.itemUuid = itemUuid;
		this.itemVersion = itemVersion;
	}

	public void getAndSaveItem() {
		logger.trace("Getting item %s/%d/\n", itemUuid, itemVersion);

		final EQUELLASOAP equella = new EQUELLASOAP(endpoint, username, password);

		final XMLWrapper result = equella.getItem(itemUuid, itemVersion);

		logger.info("Item retrieved successfully.");
		logger.trace(result.toString());

		equella.saveItem(result, true);
		logger.info("Item saved successfully.");
		
		equella.logout();
	}

	public void createAndDeleteUser() throws Exception {
		final EQUELLASOAP equella = new EQUELLASOAP(endpoint, username, password);
		String uuid = UUID.randomUUID().toString();
		String uid = "temp-auto-test-user-" + System.currentTimeMillis();
		String password = UUID.randomUUID().toString();
		String result = equella.addUser(uuid, uid, password, "Temp", "Autotest", "no-reply@example.com");
		String getUserResult = "<user><uuid>" + uuid + "</uuid><username>" + uid
				+ "</username><firstName>Temp</firstName><lastName>Autotest</lastName><email>no-reply@example.com</email></user>";
		if (result.equals(uuid)) {
			logger.info("User created successfully.");
		} else {
			throw new Exception(String.format(
					"User creation attempted, but result was not the expected uuid.  Expected uuid=[%s], actual result=[%s]\n",
					uuid, result));
		}
		String getUserResultB = equella.getUser(uuid);
		if (getUserResult.equals(getUserResultB)) {
			logger.info("User retrieved successfully.");
		} else {
			throw new Exception("Failed to either create or retrieve user: " + uid);
		}

		equella.deleteUser(uuid);
		boolean foundDeletedUser = true;
		try {
			equella.getUser(uuid);
		} catch (Exception e) {
			// Expected
			foundDeletedUser = false;
			logger.info("User deleted successfully.");
		}
		if (foundDeletedUser) {
			throw new Exception("Failed to delete user: " + uid);
		}

		equella.logout();
	}
}
