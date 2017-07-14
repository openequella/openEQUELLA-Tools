package com.pearson.equella.support.oauthtester.driver;

import java.util.logging.LogManager;

import org.apache.logging.log4j.Logger;

import com.pearson.equella.support.oauthtester.jetty.EmbeddedJetty;
import com.pearson.equella.support.oauthtester.util.Config;


/**
 * Step 1: Your application must redirect to the OAuth authorisation endpoint .../oauth/authorise:
 *			.../oauth/authorise?response_type=code&client_id=[clientid]&redirect_uri=[redirect URL]
 * Step 2: User is presented with an EQUELLA login screen (if they are not already logged in, e.g. in another browser tab)
 * Step 3: The EQUELLA OAuth authorisation endpoint redirects to your application (as determined by the redirect_uri) 
 * 			with a code parameter appended: [redirect URL]?code=[a short lived code]
 * Step 4: Your application needs to detect the presence of the code parameter and do a server side request to the EQUELLA 
 * 			OAuth token endpoint: .../oauth/access_token?grant_type=authorization_code&client_id=[client id]&code=[code from step 3]
 *			where a token will returned in JSON format. For example,
 *       	{
 *          	"scope" : null,
 *            	"state" : null,
 *            	"access_token" : "1d331cef-185a-4c22-az22-b99639qwef4f",
 *            	"refresh_token" : null,
 *            	"token_type" : "equella_api",
 *            	"expires_in" : 9223372036544775807
 *			}
 * Step 5: Your application must extract the access_token value. Once you have retrieved the token, each API call needs to 
 * 			include an "X-Authorization" HTTP header with a value of "access_token=[token]"
 * 
 *
 */
public class LaunchOAuthTester {
	private static final Logger logger = LogManager.getLogger(LaunchOAuthTester.class);

	public static void main(final String[] args) throws Exception {
		org.eclipse.jetty.util.log.Log.setLog(new org.eclipse.jetty.util.log.Slf4jLog());
		
		logger.info(String.format("### Checking configs from oauth-tester.properties..."));
		if(!Config.initConfig()) {
			logger.info("### Config check failed.  Exiting...");
			System.exit(0);
		}
		logger.info("### Config check passed.");
		
		logger.info("### Starting OAuth Tester...");
		new EmbeddedJetty().startServer();
	}
}
