package com.pearson.equella.support.loadTestResults.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import com.pearson.equella.support.loadTestResults.JtlXmlResult;

public class DbUtils {
	private static int counter = 1;
	private static Connection conn = null;
	private static PreparedStatement batchInserts = null;
	private static int batchCounter = 0;
	private static int batchLimit = 100;
	
	public static void primeConnection() throws SQLException {
		if (conn == null) {
			//TODO Externalize the url, username, and password
			String url = "jdbc:postgresql://my-db-host/my-db-name";
			Properties props = new Properties();
			props.setProperty("user", "YourUsername");
			props.setProperty("password", "NotARealPw");
			props.setProperty("ssl", "false");
			conn = DriverManager.getConnection(url, props);
		}
	}
	
	public static void resetDb(String round) throws Exception {
//		Statement st = conn.createStatement();
//		st.execute("DROP TABLE JTL_XML_RESULTS;");
//		st.close();
		
		PreparedStatement pst = conn.prepareStatement("DELETE FROM JTL_XML_RESULTS where ROUND = ?");
		pst.setString(1, round);
		pst.execute();
		pst.close(); 
		if(fetchDistinctTimeSlices(round).size() != 0) {
			throw new Exception("Something went wrong purging the DB of JTL XML results for round ["+round+".]");
		}
		
//		Statement st = conn.createStatement();
//		st.execute("CREATE TABLE JTL_XML_RESULTS (" +
//				"ID TEXT PRIMARY KEY NOT NULL, " +
//				"TEST_TYPE TEXT NOT NULL, " +
//				"ROUND TEXT NOT NULL, " +
//				"T TEXT NOT NULL, " +
//				"IT TEXT NOT NULL, " +
//				"LT TEXT NOT NULL, " +
//				"TS TEXT NOT NULL, " +
//				"S TEXT NOT NULL, " +
//				"LB TEXT NOT NULL, " +
//				"RM TEXT NOT NULL, " +
//				"TN TEXT NOT NULL, " +
//				"DT TEXT NOT NULL, " +
//				"BY TEXT NOT NULL, " +
//				"SC TEXT NOT NULL, " +
//				"EC TEXT NOT NULL, " +
//				"NG TEXT NOT NULL, " +
//				"NA TEXT NOT NULL);");
//		st.close(); 
	}
	
	public static void finalizeStore() throws SQLException {
		if(batchInserts != null) {
			batchInserts.executeBatch();
			batchInserts.close();
		}
	}

	public static void store(JtlXmlResult result, String round) throws Exception {
		if(batchInserts == null) {
			batchInserts = conn.prepareStatement(
				"INSERT INTO JTL_XML_RESULTS " +
				"(ID, TEST_TYPE, ROUND, T, IT, LT, TS, S, LB, RM, TN, DT, BY, SC, EC, NG, NA) " +
				"VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
		}
		batchInserts.setString(1, round+(counter++));
		batchInserts.setString(2, result.getTestType());
		batchInserts.setString(3, round);
		batchInserts.setString(4, ""+result.getElapsedMillis());
		batchInserts.setString(5, ""+result.getIdleMillis());
		batchInserts.setString(6, ""+result.getLatencyMillis());
		batchInserts.setString(7, ""+result.getTimeSlice());
		batchInserts.setString(8, ""+result.isSuccess());
		batchInserts.setString(9, ""+result.getLabel());
		batchInserts.setString(10, ""+result.getResponseMessage());
		batchInserts.setString(11, ""+result.getThreadName());
		batchInserts.setString(12, ""+result.getDataType());
		batchInserts.setString(13, ""+result.getBytes());
		batchInserts.setString(14, ""+result.getSampleCount());
		batchInserts.setString(15, ""+result.getErrorCount());
		batchInserts.setString(16, ""+result.getNumOfActiveGroupThreads());
		batchInserts.setString(17, ""+result.getNumOfActiveThreads());
		batchInserts.addBatch();
		batchCounter++;
		if(batchCounter >= batchLimit) {
			if(batchInserts.executeBatch().length != batchCounter) {
				throw new Exception("Expected "+batchCounter+" row to be inserted!");
			}
			batchCounter = 0;
		}
	}
	
	public static List<String> fetchDistinctTimeSlices(String round) throws SQLException {
		List<String> res = new ArrayList<String>();
		PreparedStatement st = conn.prepareStatement("SELECT distinct ts FROM JTL_XML_RESULTS where ROUND = ?");
		st.setString(1, round);
		ResultSet rs = st.executeQuery();
		while (rs.next())
		{
		   res.add(rs.getString(1));
		} 
		rs.close();
		st.close(); 
		
		return res;
	}

	public static List<JtlXmlResult> fetchJtlXmlResults(String timeSlice, String round) throws SQLException, ParseException {
		List<JtlXmlResult> res = new ArrayList<JtlXmlResult>();
		PreparedStatement st = conn.prepareStatement("SELECT * FROM JTL_XML_RESULTS WHERE ts = ? AND ROUND = ?");
		st.setString(1, timeSlice);
		st.setString(2, round);
		ResultSet rs = st.executeQuery();
		while (rs.next())
		{
			//ID, TEST_NAME, ROUND, T, IT, LT, TS, S, LB, RM, TN, DT, BY, SC, EC, NG, NA
			JtlXmlResult cachedRow = new JtlXmlResult();
			cachedRow.setNumOfActiveThreads(rs.getString(17));
			cachedRow.setTestType(rs.getString(2));
			cachedRow.setTimeSlice(rs.getString(7));
			cachedRow.setBytes(rs.getString(13));
			cachedRow.setSuccess(rs.getString(8));
			cachedRow.setLabel(rs.getString(9));
			cachedRow.setElapsedMillis(rs.getString(4));
			res.add(cachedRow);
		} rs.close();
		st.close(); 
		
		return res;
	}

}
