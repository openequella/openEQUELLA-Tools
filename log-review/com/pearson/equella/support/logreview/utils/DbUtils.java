package com.pearson.equella.support.logreview.utils;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import com.pearson.equella.support.logreview.LogReview;
import com.pearson.equella.support.logreview.parser.LogMessage;
import com.pearson.equella.support.logreview.utils.SearchFacet.SearchType;

public class DbUtils {
	private static Connection conn = null;
	private static PreparedStatement batchInserts = null;
	private static int batchCounter = 0;
	private static long globalCounter = 0;
	private static int batchLimit = 100;

	public static void primeConnection(String credProps) throws SQLException, IOException {
		if (conn == null) {
			Properties p = new Properties();
			InputStream input = new FileInputStream(credProps);

			// load a properties file
			p.load(input);

			String url = "jdbc:postgresql://localhost/logreview2";
			Properties props = new Properties();
			props.setProperty("user", p.getProperty("db.username"));
			props.setProperty("password", p.getProperty("db.password"));
			props.setProperty("ssl", "false");
			conn = DriverManager.getConnection(url, props);
			Utils.outlnf("Primed DB connection.");
		}
	}

	/**
	 * 
	 * @return 1 if the MESSAGES table. If table doesn't exist, returns -1.
	 * @throws SQLException
	 */
	public static boolean pingDbTable() throws SQLException {

		DatabaseMetaData dbm = conn.getMetaData();
		// check if "employee" table is there
		ResultSet tables = dbm.getTables(null, null, "log_message", null);
		if (tables.next()) {
			// Table exists
			Utils.outlnf("DB check:  log_message table exists");
			return true;
		} else {
			// Table does not exist
			Utils.outlnf("DB check:  log_message table does NOT exist");
			return false;
		}

	}

	private static void createLogMessageTable() throws SQLException {
		Statement st = conn.createStatement();
		st.execute("CREATE TABLE log_message (ID TEXT PRIMARY KEY NOT NULL, LOG_DATE TEXT NOT NULL, "
				+ "SERVER TEXT NOT NULL, TIMESTAMP TEXT NOT NULL, NORMALIZED_TIME TEXT NOT NULL, "
				+ "CATEGORY TEXT NOT NULL, CONTEXT TEXT NOT NULL, LEVEL TEXT NOT NULL, MSG TEXT NOT NULL, STACK_TRACE TEXT NOT NULL, LOG_FILE_PATH TEXT NOT NULL);");
		if (LogReview.LOG_SQL)
			System.out.println(st);
		st.close();
		Utils.outlnf("DB table log_message created");
	}

	private static void deleteLogMessageTable() throws SQLException {
		Statement st = conn.createStatement();
		st.execute("DROP TABLE log_message");
		if (LogReview.LOG_SQL)
			System.out.println(st);
		st.close();
		Utils.outlnf("DB table log_message deleted");
	}

	public static void resetDb() throws Exception {
		Utils.outlnf("DB about to be reset.");
		if (pingDbTable()) {
			deleteLogMessageTable();
		}
		createLogMessageTable();
	}

	public static void finalizeStore() throws SQLException {
		if (batchInserts != null) {
			batchInserts.executeBatch();
			batchInserts.close();
			batchInserts = null;
		}
	}

	public static void store(LogMessage m) throws Exception {
		if (batchInserts == null) {
			batchInserts = conn
					.prepareStatement("INSERT INTO LOG_MESSAGE " + "(ID, LOG_DATE, SERVER, TIMESTAMP, NORMALIZED_TIME, "
							+ "CATEGORY, CONTEXT, LEVEL, MSG, STACK_TRACE, LOG_FILE_PATH) "
							+ "VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
		}

		batchInserts.setString(1, "" + globalCounter++);
		batchInserts.setString(2, m.getDate().trim());
		batchInserts.setString(3, m.getServer().trim());
		batchInserts.setString(4, m.getTimestamp().trim());
		batchInserts.setString(5, m.getTimeSlot().trim());
		batchInserts.setString(6, m.getCategory().trim());
		batchInserts.setString(7, m.getContext().trim());
		batchInserts.setString(8, m.getLevel().trim());
		batchInserts.setString(9, m.getMessage().trim());
		batchInserts.setString(10, m.getStackTrace().trim());
		batchInserts.setString(11, m.getLogFilePath().trim());
		batchInserts.addBatch();
		batchCounter++;
		if (batchCounter >= batchLimit) {
			int numInserted = batchInserts.executeBatch().length;
			if (numInserted != batchCounter) {
				throw new Exception(
						"Expected " + batchCounter + " rows to be inserted.  Instead had [" + numInserted + "] rows.");
			}
			batchCounter = 0;
		}
	}

	public static long countLogMessages() throws SQLException {
		PreparedStatement st = null;
		ResultSet rs = null;
		try {
			st = conn.prepareStatement("SELECT count(1) FROM log_message");
			if (LogReview.LOG_SQL)
				System.out.println(st);

			rs = st.executeQuery();
			while (rs.next()) {
				return rs.getLong(1);
			}
		} finally {
			if (rs != null)
				rs.close();
			if (st != null)
				st.close();
		}
		return -1;
	}

	public static void buildPSqlForFacet(StringBuilder sb, SearchType type, String ident) {
		if (type == SearchType.EQUALS) {
			sb.append(" and " + ident + " = ?");
		} else if (type == SearchType.LIKE) {
			sb.append(" and " + ident + " like ?");
		}
	}

	public static Map<String, Long> countMsgs(String date, String slot, SearchFacet facet) throws SQLException {
		int counter = 1;
		Map<String, Long> toRet = new HashMap<>();
		PreparedStatement st = null;
		ResultSet rs = null;
		try {
			StringBuilder sb = new StringBuilder();
			sb.append("SELECT server, count(1) FROM log_message where log_date = ? and normalized_time = ?");
			buildPSqlForFacet(sb, facet.getMsgType(), "msg");
			buildPSqlForFacet(sb, facet.getStackTraceType(), "stack_trace");
			buildPSqlForFacet(sb, facet.getLevelType(), "level");
			buildPSqlForFacet(sb, facet.getCategoryType(), "category");

			sb.append("group by server");
			st = conn.prepareStatement(sb.toString());
			st.setString(counter++, date);
			st.setString(counter++, slot);
			if (facet.getMsgType() != SearchType.IGNORE)
				st.setString(counter++, facet.getMsgQuery());
			if (facet.getStackTraceType() != SearchType.IGNORE)
				st.setString(counter++, facet.getStackTraceQuery());
			if (facet.getLevelType() != SearchType.IGNORE)
				st.setString(counter++, facet.getLevelQuery());
			if (facet.getCategoryType() != SearchType.IGNORE)
				st.setString(counter++, facet.getCategoryQuery());
			if (LogReview.LOG_SQL)
				System.out.println(st);

			rs = st.executeQuery();
			while (rs.next()) {
				toRet.put(rs.getString(1), rs.getLong(2));
			}
		} finally {
			if (rs != null)
				rs.close();
			if (st != null)
				st.close();
		}
		return toRet;
	}

	public static Map<String, Long> countMsgsByLevel(String date, String slot, String level) throws SQLException {
		Map<String, Long> toRet = new HashMap<>();
		PreparedStatement st = null;
		ResultSet rs = null;
		try {
			st = conn.prepareStatement(
					"SELECT server, count(1) FROM log_message where log_date = ? and normalized_time = ? and level = ? group by server");
			st.setString(1, date);
			st.setString(2, slot);
			st.setString(3, level);
			if (LogReview.LOG_SQL)
				System.out.println(st);
			rs = st.executeQuery();
			while (rs.next()) {
				toRet.put(rs.getString(1), rs.getLong(2));
			}
		} finally {
			if (rs != null)
				rs.close();
			if (st != null)
				st.close();
		}
		return toRet;
	}

	public static long countMsgsByLevel(String level) throws SQLException {
		PreparedStatement st = null;
		ResultSet rs = null;
		try {
			st = conn.prepareStatement("SELECT count(1) FROM log_message where level = ?");
			st.setString(1, level);
			if (LogReview.LOG_SQL)
				System.out.println(st);
			rs = st.executeQuery();
			while (rs.next()) {
				return rs.getLong(1);
			}
		} finally {
			if (rs != null)
				rs.close();
			if (st != null)
				st.close();
		}
		return -1;
	}

	public static long countMsgsByFacet(SearchFacet facet) throws SQLException {
		PreparedStatement st = null;
		ResultSet rs = null;
		try {
			int counter = 1;
			StringBuilder sb = new StringBuilder();
			sb.append("SELECT count(1) FROM log_message where 1=1 ");
			buildPSqlForFacet(sb, facet.getMsgType(), "msg");
			buildPSqlForFacet(sb, facet.getStackTraceType(), "stack_trace");
			buildPSqlForFacet(sb, facet.getLevelType(), "level");
			buildPSqlForFacet(sb, facet.getCategoryType(), "category");

			st = conn.prepareStatement(sb.toString());

			if (facet.getMsgType() != SearchType.IGNORE)
				st.setString(counter++, facet.getMsgQuery());
			if (facet.getStackTraceType() != SearchType.IGNORE)
				st.setString(counter++, facet.getStackTraceQuery());
			if (facet.getLevelType() != SearchType.IGNORE)
				st.setString(counter++, facet.getLevelQuery());
			if (facet.getCategoryType() != SearchType.IGNORE)
				st.setString(counter++, facet.getCategoryQuery());

			if (LogReview.LOG_SQL)
				System.out.println(st);
			rs = st.executeQuery();
			while (rs.next()) {
				return rs.getLong(1);
			}
			return 0;
		} finally {
			if (rs != null)
				rs.close();
			if (st != null)
				st.close();
		}
	}

	public static long countUnknownMsgsByLevel(String level, List<SearchFacet> facets) throws SQLException {
		PreparedStatement st = null;
		ResultSet rs = null;
		try {
			int counter = 1;
			StringBuilder sb = new StringBuilder();
			sb.append("SELECT count(1) FROM log_message where level = ? ");
			for (SearchFacet facet : facets) {
				sb.append("and id not in (select id from log_message where 1=1 ");
				buildPSqlForFacet(sb, facet.getMsgType(), "msg");
				buildPSqlForFacet(sb, facet.getStackTraceType(), "stack_trace");
				buildPSqlForFacet(sb, facet.getLevelType(), "level");
				buildPSqlForFacet(sb, facet.getCategoryType(), "category");
				sb.append(") ");
			}
			st = conn.prepareStatement(sb.toString());
			st.setString(counter++, level);
			for (SearchFacet facet : facets) {
				if (facet.getMsgType() != SearchType.IGNORE)
					st.setString(counter++, facet.getMsgQuery());
				if (facet.getStackTraceType() != SearchType.IGNORE)
					st.setString(counter++, facet.getStackTraceQuery());
				if (facet.getLevelType() != SearchType.IGNORE)
					st.setString(counter++, facet.getLevelQuery());
				if (facet.getCategoryType() != SearchType.IGNORE)
					st.setString(counter++, facet.getCategoryQuery());

			}
			if (LogReview.LOG_SQL)
				System.out.println(st);

			rs = st.executeQuery();
			while (rs.next()) {
				return rs.getLong(1);
			}
		} finally {
			if (rs != null)
				rs.close();
			if (st != null)
				st.close();
		}
		return -1;
	}

	public static Map<String, Long> countUnknownMsgsByLevel(String date, String slot, String level,
			List<SearchFacet> facets) throws SQLException {
		Map<String, Long> toRet = new HashMap<>();
		PreparedStatement st = null;
		ResultSet rs = null;
		try {
			int counter = 1;
			StringBuilder sb = new StringBuilder();
			sb.append(
					"SELECT server, count(1) FROM log_message where log_date = ? and normalized_time = ? and level = ? ");
			for (SearchFacet facet : facets) {
				sb.append("and id not in (select id from log_message where 1=1 ");
				buildPSqlForFacet(sb, facet.getMsgType(), "msg");
				buildPSqlForFacet(sb, facet.getStackTraceType(), "stack_trace");
				buildPSqlForFacet(sb, facet.getLevelType(), "level");
				buildPSqlForFacet(sb, facet.getCategoryType(), "category");
				sb.append(") ");
			}
			sb.append("group by server ");

			st = conn.prepareStatement(sb.toString());
			st.setString(counter++, date);
			st.setString(counter++, slot);
			st.setString(counter++, level);
			for (SearchFacet facet : facets) {
				if (facet.getMsgType() != SearchType.IGNORE)
					st.setString(counter++, facet.getMsgQuery());
				if (facet.getStackTraceType() != SearchType.IGNORE)
					st.setString(counter++, facet.getStackTraceQuery());
				if (facet.getLevelType() != SearchType.IGNORE)
					st.setString(counter++, facet.getLevelQuery());
				if (facet.getCategoryType() != SearchType.IGNORE)
					st.setString(counter++, facet.getCategoryQuery());

			}
			if (LogReview.LOG_SQL)
				System.out.println(st);

			rs = st.executeQuery();
			while (rs.next()) {
				toRet.put(rs.getString(1), rs.getLong(2));
			}
		} finally {
			if (rs != null)
				rs.close();
			if (st != null)
				st.close();
		}
		return toRet;
	}

	public static void cleanOutKnownMessages(List<SearchFacet> facets) throws SQLException {
		PreparedStatement st = null;
		try {
			for (SearchFacet facet : facets) {
				int counter = 1;
				StringBuilder sb = new StringBuilder();
				sb.append("DELETE from log_message where id in (select id from log_message where 1=1 ");
				buildPSqlForFacet(sb, facet.getMsgType(), "msg");
				buildPSqlForFacet(sb, facet.getStackTraceType(), "stack_trace");
				buildPSqlForFacet(sb, facet.getLevelType(), "level");
				buildPSqlForFacet(sb, facet.getCategoryType(), "category");
				sb.append(") ");

				st = conn.prepareStatement(sb.toString());

				if (facet.getMsgType() != SearchType.IGNORE)
					st.setString(counter++, facet.getMsgQuery());
				if (facet.getStackTraceType() != SearchType.IGNORE)
					st.setString(counter++, facet.getStackTraceQuery());
				if (facet.getLevelType() != SearchType.IGNORE)
					st.setString(counter++, facet.getLevelQuery());
				if (facet.getCategoryType() != SearchType.IGNORE)
					st.setString(counter++, facet.getCategoryQuery());

				if (LogReview.LOG_SQL)
					System.out.println(st);
				Utils.outlnf("Deleted %s rows based on SearchFacet %s", st.executeUpdate(), facet);
			}
		} finally {
			if (st != null)
				st.close();
		}
	}

	public static long countUnknownMsgs(List<SearchFacet> facets) throws SQLException {
		PreparedStatement st = null;
		ResultSet rs = null;
		try {
			int counter = 1;
			StringBuilder sb = new StringBuilder();
			sb.append("SELECT count(1) FROM log_message where 1=1 ");

			for (SearchFacet facet : facets) {
				sb.append("and id not in (select id from log_message where 1=1 ");
				buildPSqlForFacet(sb, facet.getMsgType(), "msg");
				buildPSqlForFacet(sb, facet.getStackTraceType(), "stack_trace");
				buildPSqlForFacet(sb, facet.getLevelType(), "level");
				buildPSqlForFacet(sb, facet.getCategoryType(), "category");
				sb.append(") ");
			}

			st = conn.prepareStatement(sb.toString());

			for (SearchFacet facet : facets) {
				if (facet.getMsgType() != SearchType.IGNORE)
					st.setString(counter++, facet.getMsgQuery());
				if (facet.getStackTraceType() != SearchType.IGNORE)
					st.setString(counter++, facet.getStackTraceQuery());
				if (facet.getLevelType() != SearchType.IGNORE)
					st.setString(counter++, facet.getLevelQuery());
				if (facet.getCategoryType() != SearchType.IGNORE)
					st.setString(counter++, facet.getCategoryQuery());
			}
			if (LogReview.LOG_SQL)
				System.out.println(st);
			rs = st.executeQuery();
			while (rs.next()) {
				return rs.getLong(1);
			}
		} finally {
			if (rs != null)
				rs.close();
			if (st != null)
				st.close();
		}
		return -1;
	}

	public static List<LogMessage> getAllMessages() throws SQLException {
		List<LogMessage> msgs = new ArrayList<>();
		LogMessage lm;
		PreparedStatement st = null;
		ResultSet rs = null;
		try {
			st = conn.prepareStatement("SELECT * FROM log_message");
			if (LogReview.LOG_SQL)
				System.out.println(st);

			rs = st.executeQuery();
			while (rs.next()) {
				lm = new LogMessage();
				lm.setDate(rs.getString(2));
				lm.setServer(rs.getString(3));
				lm.setTimestamp(rs.getString(4));
				lm.setTimeSlot(rs.getString(5));
				lm.setCategory(rs.getString(6));
				lm.setContext(rs.getString(7));
				lm.setLevel(rs.getString(8));
				lm.setMessage(rs.getString(9));
				lm.setStackTrace(rs.getString(10));
				lm.setLogFilePath(rs.getString(11));
				msgs.add(lm);
			}
		} finally {
			if (rs != null)
				rs.close();
			if (st != null)
				st.close();
		}
		return msgs;
	}

	public static List<String> getAllDates() throws SQLException {
		List<String> msgs = new ArrayList<>();
		PreparedStatement st = null;
		ResultSet rs = null;
		try {
			st = conn.prepareStatement("SELECT distinct(log_date) FROM log_message order by log_date");
			if (LogReview.LOG_SQL)
				System.out.println(st);

			rs = st.executeQuery();
			while (rs.next()) {
				msgs.add(rs.getString(1));
			}
		} finally {
			if (rs != null)
				rs.close();
			if (st != null)
				st.close();
		}
		return msgs;
	}

	public static List<String> getAllServers() throws SQLException {
		List<String> msgs = new ArrayList<>();
		PreparedStatement st = null;
		ResultSet rs = null;
		try {
			st = conn.prepareStatement("SELECT distinct(server) FROM log_message order by server");
			if (LogReview.LOG_SQL)
				System.out.println(st);

			rs = st.executeQuery();
			while (rs.next()) {
				msgs.add(rs.getString(1));
			}
		} finally {
			if (rs != null)
				rs.close();
			if (st != null)
				st.close();
		}
		return msgs;
	}

	public static List<String> getAllTimeSlots() throws SQLException {
		List<String> msgs = new ArrayList<>();
		PreparedStatement st = null;
		ResultSet rs = null;
		try {
			st = conn.prepareStatement("SELECT distinct(normalized_time) FROM log_message order by normalized_time");
			if (LogReview.LOG_SQL)
				System.out.println(st);
			rs = st.executeQuery();
			while (rs.next()) {
				msgs.add(rs.getString(1));
			}
		} finally {
			if (rs != null)
				rs.close();
			if (st != null)
				st.close();
		}
		return msgs;
	}
}
