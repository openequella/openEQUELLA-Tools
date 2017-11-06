/*
 * Copyright 2017 Apereo
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

package org.equella.tools.apiTester;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.equella.tools.apiTester.rest.RestActions;
import org.equella.tools.apiTester.soap.SoapActions;

public class Driver {
	private Logger logger = LogManager.getLogger(Driver.class);
    public static void main(String[] args) {
    	Driver d = new Driver();
    	d.runRestTests();
    	d.runSoapTests();
    }
    
    public void runRestTests() {
    	try {
			RestActions ra = new RestActions(System.getProperty("equellaUrl"),
					System.getProperty("equellaRestClientId"),
					System.getProperty("equellaRestClientSecret"));
			logger.info("Running against: "+System.getProperty("equellaUrl"));
			ra.seedTestUserUuid();
			ra.gatherAccessToken();
			ra.getUser(false);
			ra.createUser();
			ra.getUser(true);
			ra.updateUser();
			ra.getUser(true);
			ra.deleteUser();
			ra.getUser(false);
			logger.info("REST Tests successful.");
		} catch (Exception e) {
			logger.error("Failed to run REST tests.  Reason="+e.getMessage());
		}
    }
    
    public void runSoapTests() {
    	try {
			SoapActions sa = new SoapActions(System.getProperty("equellaUrl"),
					System.getProperty("equellaSoapUsername"),
					System.getProperty("equellaSoapPassword"),
					System.getProperty("equellaSoapItemUuid"),
					Integer.parseInt(System.getProperty("equellaSoapItemVersion")));
			sa.getAndSaveItem();
			sa.createAndDeleteUser();
			logger.info("SOAP Tests successful.");
		} catch (Exception e) {
			logger.error("Failed to run SOAP tests.  Reason="+e.getMessage());
		}
    }
}
