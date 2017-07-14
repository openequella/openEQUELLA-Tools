package com.pearson.equella.support.oauthtester.util;

import java.io.FileInputStream;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Config {
	private static final Logger logger = LogManager.getLogger(Config.class);

	private static Properties testerProps;
	private static String endpoint = "";
	private static String clientId = "";
	private static String hostname = "";
	private static int port = -1;
	private static int delay = 0;
	
	public static String getEndpoint() {
		return endpoint;
	}

	public static String getClientId() {
		return clientId;
	}

	public static String getRedirectUrl() {
		return String.format("http://%s:%d/eqsupport/primary", hostname, port);
	}
	
	public static String getHostname() {
		return hostname;
	}
	
	public static int getPort() {
		return port;
	}
	
	public static int getDelay() {
		return delay;
	}
	
	private Config() throws Exception {
		 
	}
	
	private static String getProp(String key) throws Exception {
		String val = testerProps.getProperty(key);
		if((val == null) || val.isEmpty()) {
			throw new Exception(String.format("Property [%s] was either not found or empty.", key));
		}
		return val;
	}
	
	public static boolean initConfig() {
		if(testerProps == null) {
			testerProps = new Properties();
			try {
				testerProps.load(new FileInputStream("oauth-tester.properties"));
				endpoint = getProp("equella.endpoint");
				clientId = getProp("client.id");
				hostname = getProp("tester.hostname");
				port = Integer.parseInt(getProp("tester.port"));
				delay = Integer.parseInt(getProp("tester.delay"));
			} catch (Exception e) {
				e.printStackTrace();
				logger.info(String.format(
						"Unable to use oauth-tester.properties: %s",
						e.getMessage()));
				return false;
			}
		}
		return true;
	}
}
