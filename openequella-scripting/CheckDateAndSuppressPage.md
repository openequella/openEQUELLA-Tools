# Example Script - Check Date and Suppress Page if past

_Description:_ This script is meant to be used in a page scriptlet and will suppress the page if the specified date is equal to or after the current date.

_Disclaimer:_ Example scripts are provided as-is without any warrenty, and are meant as Proof-of-Concepts for various abilities of the Equella scripting engine. It is assumed thorough testing of any example script in this repository will be performed by the adopter before running on a Production institution.

```
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
 
// This script is meant to be used in a page scriptlet and will suppress the page if the 
// specified date is equal to or after the current date.

// This script assumes the deadline format is YYYY-MM-DD

var bRet = true; 
var sig = "[" + page.getPageTitle() + "] - ";
try {
	var deadlineXPath = '/my/metadata/path';
	if( xml.exists(deadlineXPath) ) 
	{
	    // Parse the specified deadline
	    var deadlineStr = xml.get(deadlineXPath);
	    logger.log(sig + "Deadline specified: " + deadlineStr);
	    var deadlineArr = deadlineStr.split("-");
	    if(deadlineArr.length != 3) {
	      throw "deadline for [" + deadlineXPath + "] should have 3 components, but found " + deadlineArr.length; 
	    }
	    var deadline = new Date(deadlineArr[0], deadlineArr[1]-1, deadlineArr[2]); //The month is zero based, so need to subtract 1.

	    // Find the current date
	    var currentDate = new Date();
	    currentDate.setHours(0,0,0,0); //Find the current date without the time

	    // Check if the deadline has been hit.  Switch <= to < for allowing the page to be shown on the actual deadline.
	    var isDeadlinePast = deadline.getTime() <= (currentDate.getTime());
	    logger.log(sig + "Is deadline [" + deadline + "] been hit?  Current date = [" + currentDate + "].  Answer = " + isDeadlinePast);
	    if(isDeadlinePast) {
	    	   // Past deadline.  Suppress the page
	    	   bRet = false; 
	    }
	} 
} catch (err) {
    logger.log(sig + "ERROR:  Encountered an issue during page scriplet: " + err);
}
return bRet; 
```

