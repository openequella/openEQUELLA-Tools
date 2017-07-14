package com.pearson.equella.support.oauthtester.util;

import java.util.ArrayList;
import java.util.Date;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;

import com.pearson.equella.support.oauthtester.driver.LaunchOAuthTester;

//TODO if this gets bigger, store in the session
public class History {
	private static final Logger logger = LogManager.getLogger(History.class);

	private static ArrayList<JSONObject> history = new ArrayList<JSONObject>();
	
	public static void add(JSONObject note) {
		logger.info("Adding: ["+note.toString()+"].  Delay: ["+Config.getDelay()+"]");
		history.add(note);
	}
	
	public static String display() {
		StringBuilder sb = new StringBuilder();
		sb.append("<table style=\"border: 1px solid black\"><tr><th>Date</th><th>Temp Code</th><th>Token Retrival Status</th><th>access_token</th><th>List Collections Status</th><th>Collection nNames</th></tr>");
		for(JSONObject obj : history) {
			sb.append(String.format("<tr><td>%s</td><td>%s</td><td>%s</td><td>%s</td><td>%s</td><td>%s</td></tr>",
					obj.getString("date"), 
					obj.getString("temp_code"), 
					obj.getString("status_to_retrieve_access_token"), 
					obj.getString("access_token"), 
					obj.getString("status_to_make_rest_call"), 
					obj.getString("rest_call_results")));
		}
		sb.append("</table>");
		return sb.toString();
	}
}
