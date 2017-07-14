package com.pearson.equella.support.institutionsurgery.driver;

import java.io.FileInputStream;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Config {
	private static final Logger logger = LogManager.getLogger(Config.class);

	private static Properties props;
	private static String unzippedInstitution = "";
	private static String surgeryType = "";

	public static String getSurgeryType() {
		return surgeryType;
	}

	private Config() throws Exception {

	}

	private static String getProp(String key) throws Exception {
		String val = props.getProperty(key);
		if ((val == null) || val.isEmpty()) {
			throw new Exception(String.format("Property [%s] was either not found or empty.", key));
		}
		return val;
	}

	public static boolean initConfig() {
		if (props == null) {
			props = new Properties();
			try {
				props.load(new FileInputStream("institution-surgery.properties"));
				unzippedInstitution = getProp("full.path.to.unzipped.institution");
				surgeryType = getProp("surgery.type");
			} catch (Exception e) {
				e.printStackTrace();
				logger.info(String.format("Unable to use institution-surgery.properties: %s", e.getMessage()));
				return false;
			}
		}
		return true;
	}

	public static String getUnzippedInstitution() {
		return unzippedInstitution;
	}
}
