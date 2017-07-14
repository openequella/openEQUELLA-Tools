package com.pearson.equella.support.logreview.parser;

import java.io.IOException;

import org.json.JSONObject;

import com.pearson.equella.support.logreview.utils.ReviewWriter;

public class LogMessage {
	private String timestamp;
	private String context;
	private String level;
	private String category;
	private String message;
	private String stackTrace = "";
	private String normalizedTime;
	private String date;
	private String server;
	private String logFilePath;

	public String getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(String timestamp) {
		this.timestamp = timestamp;
	}

	public String getContext() {
		return context;
	}

	public void setContext(String context) {
		this.context = context;
	}

	public String getLevel() {
		return level;
	}

	public void setLevel(String level) {
		this.level = level;
	}

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public void setStackTrace(String s) {
		this.stackTrace = s;
	}

	public String getStackTrace() {
		return stackTrace;
	}

	public String toString() {
		return String.format(
				"TS=[%s], LogFilePath=[%s], Slot=[%s], Context=[%s], Level=[%s], Category=[%s], Message=[%s], st=[%s]",
				getTimestamp(), getLogFilePath(), getTimeSlot(), getContext(), getLevel(), getCategory(), getMessage(),
				getStackTrace());
	}

	public void setTimeSlot(String normalizedTime) {
		this.normalizedTime = normalizedTime;
	}

	public String getTimeSlot() {
		return normalizedTime;
	}

	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}

	public String getServer() {
		return server;
	}

	public void setServer(String server) {
		this.server = server;
	}

	public String toJson() {
		JSONObject o = new JSONObject();
		o.put("date", date);
		o.put("normalizedTime", normalizedTime);
		o.put("timestamp", timestamp);
		o.put("level", level);
		o.put("category", category);
		o.put("server", server);
		o.put("context", context);
		o.put("message", message);
		o.put("stackTrace", stackTrace);
		o.put("logFilePath", logFilePath);
		return o.toString();
	}

	public void prettyPrint(ReviewWriter w) throws IOException {
		w.writeln("Message: " + message);
		w.writeln("Date: " + date);
		w.writeln("Timestamp: " + timestamp);
		w.writeln("Slot: " + normalizedTime);
		w.writeln("Level: " + level);
		w.writeln("Category: " + category);
		w.writeln("Server: " + server);
		w.writeln("Context: " + context);
		w.writeln("Log Filepath: " + logFilePath);
		w.writeln("Stack Trace: " + stackTrace);
	}

	public void setLogFilePath(String filepath) {
		this.logFilePath = filepath;
	}

	public String getLogFilePath() {
		return this.logFilePath;
	}
}
