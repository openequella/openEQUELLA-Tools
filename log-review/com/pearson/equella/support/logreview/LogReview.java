package com.pearson.equella.support.logreview;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

import com.pearson.equella.support.logreview.parser.LogMessage;
import com.pearson.equella.support.logreview.parser.ParseableFile;
import com.pearson.equella.support.logreview.utils.DbUtils;
import com.pearson.equella.support.logreview.utils.ReviewWriter;
import com.pearson.equella.support.logreview.utils.SearchFacet;
import com.pearson.equella.support.logreview.utils.SearchFacet.SearchType;
import com.pearson.equella.support.logreview.utils.Utils;

/**
 * Needs: Path to JSON control file (UTF-8). The 'log directory' is assumed to
 * be the parent of the [serverName] directories (expected format
 * [serverName]/[date]/application(.*).html)
 */
public class LogReview {
	// Specifying a timestamp that appears in the 'Detailed list' will trim the
	// results. Useful when there are logs that have nothing to do with the
	// review.

	public enum Granularity {
		D,
		/** Day */
		H,
		/** Hour */
		M,
		/** Minute */
		S,
		/** Second */
		X /** Unknown */
	}

	// These are special pre-compile flags when the log data is huge. Left at
	// these defaults, the program will create a fresh dataset each run and
	// build out a full report.
	public static final boolean LOG_SQL = false;
	public static final boolean USE_CURRENT_DB_STATE = false;
	public static final boolean REPORT_DETAILS = true;
	public static final boolean REPORT_UNKNOWNS = true;

	// Instance variables
	private Granularity granularity = Granularity.X;
	private String logDirectory = "";
	private String trimGuide = "";
	private String round = "";
	private List<SearchFacet> facets;
	private String outputDirectory = "";
	private String outputBase = "logReview";
	private long timestamp = System.currentTimeMillis();
	List<LogMessage> msgs = new ArrayList<>();

	public static void main(String[] args) throws Exception {
		if (args.length != 2) {
			System.err.println("Expected two arguments - the json control file, and DB creds file.");
		}
		DbUtils.primeConnection(args[1]);
		LogReview lr2 = new LogReview();
		Utils.outlnf("STATUS:  Setting configuration...");
		lr2.setConfig(args[0]);

		if (USE_CURRENT_DB_STATE) {
			Utils.outlnf("STATUS:  Bypassing scanning and parsing of log files...");
		} else {
			DbUtils.resetDb();
			Utils.outlnf("STATUS:  Scanning for log files...");
			List<ParseableFile> files = lr2.scanForFiles();
			Utils.outlnf("STATUS:  Parsing log files...");
			lr2.parseLogs(files);
			Utils.outlnf("STATUS:  Finalizing DB queries...");
			DbUtils.finalizeStore();
		}
		if (REPORT_DETAILS) {
			Utils.outlnf("STATUS:  Building the general reports...");
			lr2.displayReport();
		}

		if (REPORT_UNKNOWNS) {
			Utils.outlnf("STATUS:  Building the Unknowns Report...");
			lr2.displayUnknownMessages();
		}
		Utils.outlnf("STATUS:  Review complete...");

	}

	public void setConfig(String controlFilename) throws Exception {
		Utils.outlnf("Control file: [%s]", controlFilename);
		JSONObject ctl = Utils.parseJsonFile(controlFilename, Charset.forName("UTF-8"));

		logDirectory = ctl.getString("logDirectory");
		Utils.outlnf("Target Directory: [%s]", logDirectory);

		outputDirectory = ctl.getString("outputDirectory");
		Utils.outlnf("Output Directory: [%s]", outputDirectory);

		outputBase = ctl.getString("outputBase");
		Utils.outlnf("Output Base: [%s]", outputBase);

		granularity = Granularity.valueOf(ctl.getString("granularity"));
		Utils.outlnf("Primary Flag [%s] (D = day, H = hour, M = minute, S = second)", granularity);
		trimGuide = ctl.getString("trimGuide");
		Utils.outlnf("Trim guide: [%s]", trimGuide);
		round = ctl.getString("roundName");
		Utils.outlnf("Round name: [%s]", round);
		facets = new ArrayList<>();

		JSONArray facetArray = ctl.getJSONArray("searchFacets");
		for (int facetIndex = 0; facetIndex < facetArray.length(); facetIndex++) {
			JSONObject facetJson = facetArray.getJSONObject(facetIndex);
			SearchFacet facet = new SearchFacet(facetJson);
			Utils.outlnf("Search Facet [%s]", facet);
			facets.add(facet);
		}

		Utils.outlnf("[%s] Search Facets found", facets.size());
	}

	public List<ParseableFile> scanForFiles() throws Exception {
		List<ParseableFile> files = new ArrayList<>();
		ParseableFile pf = new ParseableFile();

		File dir = new File(logDirectory);
		for (File serverNameDirectory : dir.listFiles()) {
			Utils.outlnf("Checking L1 file/directory: [%s].  IsDirectory=[%s]", serverNameDirectory,
					serverNameDirectory.isDirectory());

			if (serverNameDirectory.isDirectory()) {
				// Assume this is the 'serverName' directory.
				pf.setServer(serverNameDirectory.getName());
				
				for (File dateDirectory : serverNameDirectory.listFiles()) {
					Utils.outlnf("Checking L2 file/directory: [%s].  IsDirectory=[%s]", dateDirectory,
							dateDirectory.isDirectory());

					if (dateDirectory.isDirectory()) {
						pf.setDate(dateDirectory.getName());
						// Assume this directory is in the form YYYY-MM-DD, and
						// contains application html files.
						for (File logFile : dateDirectory.listFiles()) {
							Utils.outlnf("Checking L3 file/directory: [%s].  IsDirectory=[%s]", logFile,
									logFile.isDirectory());

							if (logFile.isFile()) {
								pf.setFilepath(logFile.getAbsolutePath());
								pf.setFilename(logFile.getName());
								files.add(pf.copy());
							} else {
								Utils.outlnf("Naughty - found a directory of [%s].  Expected log files.",
										logFile.getAbsolutePath());
							}
						}
					} else {
						Utils.outlnf("Naughty - found a non-directory of [%s].  Expected a date directory.",
								dateDirectory.getAbsolutePath());
					}
				}
			} else {
				Utils.outlnf("Naughty - found a non-directory of [%s].  Expected a serverName directory.",
						serverNameDirectory.getAbsolutePath());
			}
		}
		Collections.sort(files);
		return files;
	}

	/**
	 * For each server, walk through the logs as if they are one continuous file >
	 * Read / parse lines until a new start to a parsable message is found (this
	 * assumes a stack trace is part of a parseable message, NOT a separate message.
	 * There is a known risk that a given stack trace might not be associated to the
	 * previous log message). > Store the parsed message / stack trace in the DB
	 * 
	 * @param files
	 * @throws Exception
	 */
	public void parseLogs(List<ParseableFile> files) throws Exception {
		StringBuilder prevBuilder = new StringBuilder();
		StringBuilder curBuilder = new StringBuilder();
		ParseableFile prevParseableFile = null;

		for (ParseableFile pf : files) {
			Path current = Paths.get(pf.getFilepath());
			System.out.println("Parsing:  " + current.toString());
			BufferedReader br = new BufferedReader(new FileReader(current.toFile()));
			String line = null;
			try {
				while ((line = br.readLine()) != null) {
					if (line.startsWith("<tr><td")) {
						// Start a new statement
						curBuilder = new StringBuilder();
						curBuilder.append(line);
					} else if (line.endsWith("</td></tr>")) {
						curBuilder.append(line);
						if (containsStackTrace(curBuilder.toString())) {
							// Assume prevBuilder has the rest of the message
							String st = curBuilder.toString();
							st = st.substring(81, st.length() - 10);
							saveMessage(pf, prevBuilder.toString(), st);
							prevBuilder = null;
							prevParseableFile = null;
						} else {
							if (prevBuilder != null) {
								saveMessage(pf, prevBuilder.toString());
							}
							// Since it's not a stack trace, move the statement
							// to the holding builder and check the next
							// statement.
							prevBuilder = curBuilder;
							prevParseableFile = pf;
						}
					} else if (line.startsWith("<!DOCTYPE HTML PUBLIC") || line.startsWith("<html><head><title>")
							|| line.startsWith("body, table {font-family: arial,sans-serif")
							|| line.startsWith("th {background: #336699") || line.startsWith("--></style>")
							|| line.startsWith("<table cellspacing=") || line.startsWith("<tr><th>Time</th>")) {
						// Noise. don't use it.
					} else {
						// No statement markers detected. Assume a statement
						// portion.
						curBuilder.append(line);
					}
				}
				br.close();
			} catch (Exception e) {
				System.err.println("Last line read:  [" + line + "]");
				e.printStackTrace();
				System.exit(9);
			}
		}
		if (prevBuilder != null) {
			// save this last message into DB.
			saveMessage(prevParseableFile, prevBuilder.toString());
		}
	}

	public boolean containsStackTrace(String s) {
		return s.startsWith("<tr><td bgcolor");
	}

	public void saveMessage(ParseableFile pf, String rawMessage) throws Exception {
		saveMessage(pf, rawMessage, "");
	}

	// TODO unit test
	private boolean isMessageOfInterest(LogMessage lm) {
		// Keep any messages that have a stack trace
		if (!lm.getStackTrace().isEmpty())
			return true;

		// Keep any messages that are ERROR and WARNING msgs
		if (lm.getLevel().contains("ERROR"))
			return true;
		if (lm.getLevel().contains("WARN"))
			return true;

		// Keep any messages that contain one of the search facets.
		for (SearchFacet sf : facets) {
			boolean allGood = inspect(lm.getMessage(), sf.getMsgType(), sf.getMsgQuery());
			allGood &= inspect(lm.getStackTrace(), sf.getStackTraceType(), sf.getStackTraceQuery());
			allGood &= inspect(lm.getLevel(), sf.getLevelType(), sf.getLevelQuery());
			allGood &= inspect(lm.getCategory(), sf.getCategoryType(), sf.getCategoryQuery());
			if (allGood)
				return true;
		}

		// Otherwise, disregard the message
		return false;
	}

	// TODO unit test
	// Meant to be used for isMessageOfInterest
	public boolean inspect(String logPart, SearchType type, String query) {
		boolean allGood = true;
		if (type == SearchType.EQUALS) {
			allGood &= logPart.equals(query);
		} else if (type == SearchType.LIKE) {
			for (String s : query.split("%")) {
				allGood &= logPart.contains(s);
			}
		}
		return allGood;
	}

	public void saveMessage(ParseableFile pf, String rawMessage, String rawStackTrace) throws Exception {
		if ((rawMessage == null) || rawMessage.isEmpty()) {
			return;
		}
		LogMessage lm = new LogMessage();
		lm.setLogFilePath(pf.getFilename());
		lm.setServer(pf.getServer());
		lm.setDate(pf.getDate());
		lm.setStackTrace(rawStackTrace);
		// System.out.println("Raw Message= [" + rawMessage + "]");
		int marker = rawMessage.indexOf("</td><td>");
		lm.setTimestamp(rawMessage.substring(8, marker));
		lm.setTimeSlot(normalizeTime(granularity, lm.getTimestamp()));
		rawMessage = rawMessage.substring(marker + 9);

		marker = rawMessage.indexOf("</td><td>");
		lm.setContext(rawMessage.substring(0, marker));
		rawMessage = rawMessage.substring(marker + 9);

		marker = rawMessage.indexOf("</td><td>");
		String lev = rawMessage.substring(0, marker);
		if (lev.contains("strong")) {
			lm.setLevel(lev.substring(30, lev.length() - 16));
		} else {
			lm.setLevel(lev);
		}
		rawMessage = rawMessage.substring(marker + 9);

		marker = rawMessage.indexOf("</td><td>");
		lm.setCategory(rawMessage.substring(0, marker));
		rawMessage = rawMessage.substring(marker + 9);

		lm.setMessage(rawMessage.substring(0, rawMessage.length() - 10));

		if (!isMessageOfInterest(lm)) {
			if (LOG_SQL)
				System.out.println("Message is NOT interesting:  " + lm);
			return;
		} else {
			if (LOG_SQL)
				System.out.println("Message is interesting:  " + lm);
		}

		DbUtils.store(lm);
	}

	public List<LogMessage> getMessages() {
		return msgs;
	}

	public static String normalizeTime(Granularity g, String s) {
		switch (g) {
		case D: {
			return "";
		}
		case H: {
			return s.substring(0, s.indexOf(":"));
		}
		case M: {
			return s.substring(0, s.lastIndexOf(":"));
		}
		case S: {
			return s.substring(0, s.lastIndexOf(","));
		}
		default: {
			return "ERR-GRAN";
		}
		}
	}

	/**
	 * As JSON...
	 * 
	 * @return
	 */
	public Path displayUnknownMessages() {
		String unknowns = outputDirectory + "/" + outputBase + "-" + timestamp + "-unknowns.txt";

		try (ReviewWriter w = new ReviewWriter(unknowns)) {
			System.out.println("STATUS:  Cleaning out the known msg details");
			DbUtils.cleanOutKnownMessages(facets);

			System.out.println("STATUS:  Gathering unknown msg details");
			List<LogMessage> msgs = DbUtils.getAllMessages();
			System.out.println("STATUS:  Printing the unknown msg details");
			for (LogMessage m : msgs) {
				m.prettyPrint(w);
				w.newln(false);
				w.newln(false);
			}
			return w.getReportPath();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	public Path[] displayReport() throws SQLException {
		String normal = outputDirectory + "/" + outputBase + "-" + timestamp + ".csv";
		String condensed = outputDirectory + "/" + outputBase + "-" + timestamp + "-condensed.csv";
		try (ReviewWriter w = new ReviewWriter(normal); ReviewWriter c = new ReviewWriter(condensed)) {
			w.attachedCondensedWriter(c);
			w.writelnf(true, "- - - LOG REVIEW V2.0 - - -");
			w.writelnf(true, "Target Directory: [%s]", logDirectory);
			w.writelnf(true, "Primary Flag [%s] (D = day, H = hour, M = minute, S = second, T = task)", granularity);
			// Utils.outlnf("Trim guide: [%s]", trimGuide);
			// Utils.outlnf("Round name: [%s]", round);
			for (SearchFacet sf : facets) {
				w.writelnf(true, "Search Facet [%s]", sf);
			}
			w.newln(true);

			System.out.println("STATUS:  Printing error severity summary");
			// List all facet counts that are of an 'error' severity
			w.writelnf(true, "- - - SUMMARY REPORT - ERROR SEVERITY - - -");
			w.writelnf(true, "#,Search Facet");
			for (SearchFacet facet : facets) {
				if (facet.getSeverity().equals("error")) {
					long num = DbUtils.countMsgsByFacet(facet);
					w.writelnf(num > 0, "%s,%s", num, facet.prettyPrint());
				}
			}
			w.newln(true);
			System.out.println("STATUS:  Printing warning severity summary");
			// List all facet counts that are of an 'warning' severity
			w.writelnf(true, "- - - SUMMARY REPORT - WARNING SEVERITY - - -");
			w.writelnf(true, "#,Search Facet");
			for (SearchFacet facet : facets) {
				if (facet.getSeverity().equals("warning")) {
					long num = DbUtils.countMsgsByFacet(facet);
					w.writelnf(num > 0, "%s,%s", num, facet.prettyPrint());
				}
			}
			w.newln(true);
			System.out.println("STATUS:  Printing normal severity summary");
			// List all facet counts that are of an 'normal' severity
			w.writelnf(true, "- - - SUMMARY REPORT - NORMAL SEVERITY - - -");
			w.writelnf(true, "#,Search Facet");
			for (SearchFacet facet : facets) {
				if (facet.getSeverity().equals("normal")) {
					long num = DbUtils.countMsgsByFacet(facet);
					w.writelnf(num > 0, "%s,%s", num, facet.prettyPrint());
				}
			}
			w.newln(true);
			w.writelnf(true, "- - - DETAILED REPORTS - - -");
			System.out.println("STATUS:  Printing error msg details");

			w.writelnf(true, "%s Occurrences Of ERROR Messages", DbUtils.countMsgsByLevel("ERROR"));

			// Headers
			w.writef(true, "Date / Time");
			for (String server : DbUtils.getAllServers()) {
				w.writef(true, ",%s", server);
			}
			w.newln(true);

			// By Date
			for (String date : DbUtils.getAllDates()) {
				// By timeslot
				for (String slot : DbUtils.getAllTimeSlots()) {
					w.writef(true, "%s %s", date, slot);
					// By server
					Map<String, Long> results = DbUtils.countMsgsByLevel(date, slot, "ERROR");
					for (String server : DbUtils.getAllServers()) {
						Long l = results.get(server);
						w.writef(true, ",%s", (l == null) ? 0 : l);
					}
					w.newln(true);
				}
			}
			w.newln(true);
			System.out.println("STATUS:  Printing warn msg details");

			w.writelnf(true, "%s Occurrences Of WARN Messages", DbUtils.countMsgsByLevel("WARN"));

			// Headers
			w.writef(true, "Date / Time");
			for (String server : DbUtils.getAllServers()) {
				w.writef(true, ",%s", server);
			}
			w.newln(true);

			// By Date
			for (String date : DbUtils.getAllDates()) {
				// By timeslot
				for (String slot : DbUtils.getAllTimeSlots()) {
					w.writef(true, "%s %s", date, slot);
					// By server
					Map<String, Long> results = DbUtils.countMsgsByLevel(date, slot, "WARN");
					for (String server : DbUtils.getAllServers()) {
						Long l = results.get(server);
						w.writef(true, ",%s", (l == null) ? 0 : l);
					}
					w.newln(true);
				}
			}
			w.newln(true);

			for (SearchFacet facet : facets) {
				System.out.println("STATUS:  Printing facet details - " + facet.prettyPrint());

				long total = DbUtils.countMsgsByFacet(facet);
				boolean con = total > 0;
				w.writelnf(con, "%s Occurrences Of %s", total, facet.prettyPrint());

				if (total == 0) {
					// No need to make the other DB calls.
					w.writelnf(false, "  NOTE:  Date / Time / Server matrix not needed.");
				} else {
					// Headers
					w.writef(con, "Date / Time");
					for (String server : DbUtils.getAllServers()) {
						w.writef(con, ",%s", server);
					}
					w.newln(con);

					// By Date
					for (String date : DbUtils.getAllDates()) {
						// By timeslot
						for (String slot : DbUtils.getAllTimeSlots()) {
							w.writef(con, "%s %s", date, slot);
							// By server
							Map<String, Long> results = DbUtils.countMsgs(date, slot, facet);
							for (String server : DbUtils.getAllServers()) {
								Long l = results.get(server);
								w.writef(con, ",%s", (l == null) ? 0 : l);
							}
							w.newln(con);
						}
					}
				}
				w.newln(con);
			}
			Path[] toRet = { w.getReportPath(), c.getReportPath() };
			return toRet;
		} catch (IOException x) {
			System.err.format("IOException creating the report: %s%n", x);
		} catch (Exception x) {
			System.err.format("Exception creating the report: %s%n", x);
		}
		return null;
	}

	public Granularity getGranularity() {
		return granularity;
	}

	public String getLogDirectory() {
		return logDirectory;
	}

	public String getTrimGuide() {
		return trimGuide;
	}

	public String getRound() {
		return round;
	}

	public List<SearchFacet> getFacets() {
		return facets;
	}

}
