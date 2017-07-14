package com.pearson.equella.support;

import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;

public class Config {
	private static Properties props;
	private static String dbUrl = "";
	private static String dbUsername = "";
	private static String dbPassword = "";
	private static File todoList;
	private static boolean dryrun;
	
	private Config() {
		 
	}
	
	private static String getProp(String key) throws Exception {
		String val = props.getProperty(key);
		if((val == null) || val.isEmpty()) {
			throw new Exception(String.format("Property [%s] was either not found or empty.", key));
		}
		return val;
	}
	
	public static boolean initConfig() {
		if(props == null) {
			props = new Properties();
			try {
				props.load(new FileInputStream("ReferencedUrlRemover.properties"));
				dbUrl = getProp("db.url");
				dbUsername = getProp("db.username");
				dbPassword = getProp("db.password");
				String todoListStr = getProp("todo");
				 todoList = new File(todoListStr);
				if(!todoList.exists() || !todoListStr.endsWith(".csv")) {
					throw new Exception("'todo' csv doesn't exist or is not a csv file.");
				}
				String dryrunStr = getProp("dryrun");
				dryrun = Boolean.parseBoolean(dryrunStr);
			} catch (Exception e) {
				e.printStackTrace();
				System.out.println(String.format(
						"ERROR - Unable to use ReferencedUrlRemover.properties: %s",
						e.getMessage()));
				return false;
			}
		}
		return true;
	}

	public static String getDbUrl() {
		return dbUrl;
	}

	public static String getDbUsername() {
		return dbUsername;
	}

	public static String getDbPassword() {
		return dbPassword;
	}

	public static File getTodoList() {
		return todoList;
	}

	public static boolean isDryrun() {
		return dryrun;
	}
}
