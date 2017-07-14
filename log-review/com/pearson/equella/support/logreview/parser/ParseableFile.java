package com.pearson.equella.support.logreview.parser;

public class ParseableFile implements Comparable<ParseableFile> {
	private String server;
	private String date;
	private String filepath;
	private String filename;

	public String getServer() {
		return server;
	}

	public void setServer(String server) {
		this.server = server;
	}

	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}

	public String getFilepath() {
		return filepath;
	}

	public void setFilepath(String filepath) {
		this.filepath = filepath;
	}

	public String getFilename() {
		return filename;
	}

	public void setFilename(String filename) {
		String[] parts = filename.split("\\.");
		if (parts.length == 2) {
			// ie application.html
			this.filename = parts[0] + "000";
		} else if (parts[1].length() == 1) {
			// ie application.4.html
			this.filename = parts[0] + "00" + parts[1];
		} else if (parts[1].length() == 2) {
			// ie application.14.html
			this.filename = parts[0] + "0" + parts[1];
		} else if (parts[1].length() == 3) {
			// ie application.344.html
			this.filename = parts[0] + parts[1];
		}
	}

	public ParseableFile copy() {
		ParseableFile baby = new ParseableFile();
		baby.server = this.server;
		baby.date = this.date;
		baby.filepath = this.filepath;
		baby.filename = this.filename;
		return baby;
	}

	@Override
	public int compareTo(ParseableFile o) {
		ParseableFile of = (ParseableFile) o;
		if (this.server.equals(of.server)) {
			// Same server
			if (this.date.equals(of.date)) {
				// Same date - reverse the ordering.
				return this.filename.compareTo(of.filename) * -1;
			} else {
				return this.date.compareTo(of.date);
			}
		} else {
			return this.server.compareTo(of.server);
		}
	}

	public String toString() {
		return this.server + "-" + this.date + "-" + this.filename;
	}

}
