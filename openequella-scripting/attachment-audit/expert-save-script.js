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

// Meant for use in the openEQUELLA Expert Save Script.

try {
	// Setup parameters for attachment audit
	var conf = {
		attachmentsBasePath: "/my/path/to/equella/filestore/Institutions/demo/Attachments", /** No trailing slash */
		xpathForFcoListing: "/metadata/attAudit/fcoListing/fco",
		xpathForDbcoListing: "/metadata/attAudit/dbcoListing/dbco",
		xpathForDbcoListingMissing: "/metadata/attAudit/dbcoListingMissing/dbco",
		javaPathToFileLister: "/path/to/FileLister20180711.jar",
		slash: "/" /** Note:  For windows systems, use \\ .  For *nix systems, use / . */
	};
	// Logger helper to identify the target resource and the user
	var sig = "[" + user.getUsername() + "] " + currentItem.getUuid() + "/" + currentItem.getVersion() + " (" + currentItem.getName() + ") - ";
		
	if(staging.isAvailable()) {
		var fcoCheckResult = listFCOs(true);
		var dbcoCheckResult = listAndCheckDBCOs();
		logger.log(sig + "Attachment Audit complete [Expert Save Script].  " +
			"# of FCOs = [" + fcoCheckResult[0] + "], " +
			"FCO duration = " + fcoCheckResult[1] + "ms, " +
			"# of DBCOs checked = [" + dbcoCheckResult[1] + "], " +
			"# of DBCOs ignored = [" + dbcoCheckResult[2] + "], " +
			"DBCO duration = " + dbcoCheckResult[3] + "ms.  " + 
			dbcoCheckResult[0]);	
	}
} catch (err) {
	logger.log(sig + "ATT_AUDIT_ALERT - exception thrown: " + err);
}
/**
 * Lists all DBCO (DB content object) that consist of files / folders 
 * aka 'item attachments in the database that correspond to entities in the filestore' in the item xml and in the log. 
 * Preconditions:  listFCOs() has been run.
 * Returns [DBCO-in-FCO status, number of DBCOs checked, number of DBCOs ignored, duration]
 */
function listAndCheckDBCOs() {
	var start = new Date();
	
	// Reset DBCO audit
	xml.deleteAll(conf.xpathForDbcoListing);
	xml.deleteAll(conf.xpathForDbcoListingMissing)

	// Walk through all known attachments in the database for the item
	var numOfDbcosToCheck = 0;
	var numOfDbcosIgnored = 0;
	var dbcos = attachments.list();
	for(var i = 0; i < dbcos.size(); i++) {
		var dbco = dbcos.get(i);
		var dbcoName = "";
		numOfDbcosToCheck++;
		if((dbco.getType() == "FILE") || (dbco.getType() == "ZIP") || (dbco.getType() == "IMS")) {
			dbcoName = dbco.getFilename();
		} else if(dbco.getType() == "HTML") {
			dbcoName = "_mypages" + conf.slash + dbco.getUuid() + conf.slash + "page.html";
		} else if((dbco.getType() == "CUSTOM") && ((dbco.getCustomType() == "scorm") || (dbco.getCustomType() == "scormres"))) {
			dbcoName = dbco.getUrl();
		} else {
			numOfDbcosToCheck--;
			numOfDbcosIgnored++;
			logger.log(sig + "Ignoring DBCO attachment due to type: " + displayAttachment(dbco));
		}

		if(dbcoName != "") {
			// Check if the DBCO is in the listing of FCOs
			if(!xml.contains(conf.xpathForFcoListing, dbcoName)) {
				xml.add(conf.xpathForDbcoListingMissing, dbcoName);
			}
			
			xml.add(conf.xpathForDbcoListing, dbcoName);
		}
	}	
	logger.log(sig + "DBCO listing (files/folders): [" + displayArray(xml.getAll(conf.xpathForDbcoListing)) + "]");
	
	var result = [];
	// Create DBCO-in-FCO status
	if(xml.exists(conf.xpathForDbcoListingMissing)) {
		result.push("DBCO-in-FCO check - FAILED.  FCOs are missing:  " + displayArray(xml.getAll(conf.xpathForDbcoListingMissing)));
		logger.log(sig + "ATT_AUDIT_ALERT - DBCO-in-FCO check failed");
	} else {
		result.push("DBCO-in-FCO check - OKAY.");
	}
	result.push(numOfDbcosToCheck);
	result.push(numOfDbcosIgnored);
	result.push((new Date()) - start);
	return result;
}

/**
 * Lists all FCO (filestore content object) in the item xml and in the log. 
 * Uses the staging folder if available, otherwise use the persistent resource Attachments folder
 * Preconditions:  needs the system Java utility app FileLister available
 * Returns [number of fcos, duration]
 */
function listFCOs(useStaging) {
	var res = [];
	var start = new Date();
	
	// Reset FCO audit
	xml.deleteAll(conf.xpathForFcoListing);

	// Determine the filesystem path to list
	var path = ".";
	if(useStaging) {
		var stagingFiles = staging.list("","*");
		if(stagingFiles.size() == 0) {
			// Nothing in staging.  Nicely return.
			logger.log(sig + " FCO listing (0 files/dirs).");
			res.push(0);
			res.push((new Date()) - start);
			return res;
		}
		// The actual file is not important, as the Java utility FileLister will use the parent file (folder)
		path = staging.getFileHandle(stagingFiles.get(0));
	} else {
		var hash = currentItem.getUuid().hashCode() & 127;
		path = conf.attachmentsBasePath+conf.slash+hash+conf.slash+currentItem.getUuid()+conf.slash+currentItem.getVersion()
	}

	if(useStaging) {
		logger.log(sig + "Inspecting resource staging folder.");
	} else {
		logger.log(sig + "Inspecting resource attachment path: " + path);
	}

	// Run FileLister
	var opts = ["-jar", conf.javaPathToFileLister, path, conf.slash]
	if(useStaging) {
		opts.push("-useParent");
	}
	var rsp = system.execute("java", opts);
	
	if(rsp.getCode() == 0) {
		var fileListing = rsp.getStandardOutput();
		var files = JSON.parse(fileListing).files;
		logger.log(sig + " FCO listing ("+files.length+" files/dirs): " + files);
		res.push(files.length);
		for(var i = 0; i < files.length; i++) {
			xml.add(conf.xpathForFcoListing, files[i]);	
		}
	} else {
		res.push("-1");
		// TODO send email or alert admins.
		logger.log(sig + "ATT_AUDIT_ALERT - FileLister utility failed.");
		logger.log(sig + "ERROR listing files.  Resp code: " + rsp.getCode());
		logger.log(sig + "ERROR listing files.  Std Out: " + rsp.getStandardOutput());
		logger.log(sig + "ERROR listing files.  Err Out: " + rsp.getErrorOutput());	
	}
	res.push((new Date()) - start);
	return res;
}

// Helper method to convert an array (of strings) to a string
function displayArray(arr) {
	var res = "";
	for(var i = 0; i < arr.length; i++) {
		res += " " + arr[i];
	}
	return res;
}

function displayAttachment(att) {
	var res = "Name=[" + att.getFilename() +
		"], Desc=[" + att.getDescription() +
		"], URL=[" + att.getUrl() + 
		"], type=[" + att.getType();
	if(att.getType() == "CUSTOM") {
		res += "], customType=[" + att.getCustomType(); 
	}
		res += "]";
	return res;
}
