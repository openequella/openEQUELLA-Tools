package com.pearson.equella.support;

import java.io.BufferedReader;
import java.io.FileReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class ReferencedUrlRemoverDriver {
	private static Connection con;
	private static List<Result> results;
	private static int line = 0;

	public static void main(String[] args) throws SQLException {
		info("Beginning general config checks...");
		if (!Config.initConfig()) {
			exit(false);
		}
		info("General config checks passed.");

		if (!setupSqlServerConnection(Config.getDbUrl(),
				Config.getDbUsername(), Config.getDbPassword())) {
			error("Unable to connect to the DB [%s] with username [%s] and a password of length [%d]",
					Config.getDbUrl(), Config.getDbUsername(), Config
							.getDbPassword().length());
			exit(false);
		}

		info("Beginning DB / ID checks on item and referenced url IDs from the 'todo' config...");
		results = new ArrayList<Result>();
		String orgGhostLine = null;
		boolean allGood = true;
		try {
			FileReader fileReader = new FileReader(Config.getTodoList());
			BufferedReader bufferedReader = new BufferedReader(fileReader);
			while ((orgGhostLine = bufferedReader.readLine()) != null) {
				//Ignore comments
				line++;
				String ghostline = orgGhostLine;
				if(orgGhostLine.contains("//")) {
					ghostline = orgGhostLine.substring(0, orgGhostLine.indexOf("//")).trim();	
				}
				if(ghostline.isEmpty()) {
					info("Ignoring comment: %s.", orgGhostLine);
				} else {
					String[] parts = ghostline.split(",");
					if (parts.length == 2) {
						info("Checking 'todo' item/url row itemId=[%s] and referencedUrlId=[%s].",
								parts[0], parts[1]);
						Result r = confirmRowIds(parts[0], parts[1]);
						if ((r.getStat() != Result.Status.DbConfirmedFullRemove)
								&& (r.getStat() != Result.Status.DbConfirmedPartialRemove)) {
							allGood = false;
						} else {
							info("Confirmed 'todo' item/url row itemId=[%s] and referencedUrlId=[%s].",
									parts[0], parts[1]);
							results.add(r);
						}
					} else {
						error("'todo' item/url row is malformed.  Expected 2 values, found [%d] in [%s]",
								parts.length, orgGhostLine);
						allGood = false;
					}
				}
			}
			bufferedReader.close();
		} catch (Exception ex) {
			error("Error reading the 'todo' file [%s].  Reason:  %s", Config
					.getTodoList().getAbsolutePath(), ex.getMessage());
			ex.printStackTrace();
			exit(false);
		}
		//Reset the line so log messages don't key off of it.
		line = 0;

		if (!allGood) {
			error("DB / ID checks failed.");
			exit(false);
		}
		info("DB / ID checks complete.  Confirmed [%d] sets of ghost urls to remove.", results.size());
		if(Config.isDryrun()) {
			info("User requested dryrun.  Not making any DB changes.");
			exit(false);
		}
		info("Deleting the needful...");
		
		try {
			if(deleteAssociations()) {
				deleteLonelyRUrls();
			}
			con.commit();
			con.close();
			info("Deletes complete.  Item resave list:");
			for(Result r : results) {
				System.out.println(r.getAffectItemUuidVersion());
			}
			info("Please use Equella Support's soap-item-resave script to resave the affected items (DO NOT use the web ui first!)...");
			info("Exiting...");
		} catch (Exception e) {
			error(e.getMessage());
			e.printStackTrace();
			info("Trying to rollback deletes...");
			con.rollback();
			con.close();
			info("Rollback appeared to work.");
			exit(false);
		}
	}

	private static void exit(boolean changes) {
		if (changes) {
			System.out
					.println("Exiting - some changes may have been made.  Please confirm DB integrity...");
		} else {
			System.out.println("Exiting - no changes were made.");
		}
		System.exit(9);
	}

	private static boolean setupSqlServerConnection(
			String sqlServerUrlDbNameInstanceName, String un, String pw) {
		boolean isGood = false;
		try {
			Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
			String connectionUrl = String.format(
					"jdbc:sqlserver:%s;user=%s;password=%s",
					sqlServerUrlDbNameInstanceName, un, pw);
			con = DriverManager.getConnection(connectionUrl);
			info("Connected to %s", sqlServerUrlDbNameInstanceName);
			con.setAutoCommit(false);
			// Check the connection works.
			String SQL = "SELECT TOP 1 id, uuid FROM item ";
			Statement stmt = con.createStatement();
			ResultSet rs = stmt.executeQuery(SQL);
			// Iterate through the data in the result set and display it.
			while (rs.next()) {
				if ((rs.getString(1) != null) && !rs.getString(1).isEmpty()) {
					info("Confirmed DB connection.");
					isGood = true;
				}
			}
			rs.close();
		} catch (Exception e) {
			error("Unable to setup / confirm DB connection - %s.", e.getMessage());
			isGood = false;
		}
		return isGood;
	}

	private static long parseId(String idStr, String type) {
		try {
			long id = Long.parseLong(idStr);
			return id;
		} catch (NumberFormatException e) {
			error("%s is not a number.  Failing %s check.", idStr, type);
		}
		return -1;
	}

	private static Result confirmRowIds(String iIdStr, String ruIdStr) {
		long iId = parseId(iIdStr, "Item ID");
		long ruId = parseId(ruIdStr, "Referenced URL ID");
		Result r = new Result(iId, ruId, Result.Status.IP);
		if (iId == -1) {
			r.setStat(Result.Status.BadItemId);
		} else if (ruId == -1) {
			r.setStat(Result.Status.BadUrlId);
		} else {
			// Confirm the item id is valid
			String uuidVer = confirmItemIdIsValid(iId);
			if (uuidVer.isEmpty()) {
				r.setStat(Result.Status.BadItemId);
				return r;
			}
			r.setAffectItemUuidVersion(uuidVer);
			// Confirm the referenced url id is valid
			if (!confirmReferencedUrlIdIsValid(ruId)) {
				r.setStat(Result.Status.BadUrlId);
				return r;
			}
			// Confirm there's a row in item / url join table and
			// see if there are any other items referencing the url
			confirmItemUrlJoinTable(r);
		}
		return r;
	}

	private static boolean confirmItemUrlJoinTable(Result r) {
		try {
			// Confirm the association is in the DB
			PreparedStatement stmt = con
					.prepareStatement("SELECT item_id FROM item_referenced_urls WHERE referenced_urls_id = ?");
			stmt.setLong(1, r.getReferencedUrlId());
			ResultSet rs = stmt.executeQuery();
			boolean confirmedAssociation = false;
			boolean otherAssociations = false;
			while (rs.next()) {
				if (rs.getLong(1) == r.getItemId()) {
					confirmedAssociation = true;
				} else {
					otherAssociations = true;
				}
			}
			rs.close();

			if (!confirmedAssociation) {
				error("Unable to confirm DB contains the item / url association");
				r.setStat(Result.Status.BadAssociations);
				return false;
			}
			info("Confirmed item id [%d] is associated to the referenced url id [%d]",
					r.getItemId(), r.getReferencedUrlId());

			if (otherAssociations) {
				info("Confirmed referenced url id [%d] is in use with other items.",
						r.getReferencedUrlId());
				r.setStat(Result.Status.DbConfirmedPartialRemove);
			} else {
				r.setStat(Result.Status.DbConfirmedFullRemove);
			}
			return true;
		} catch (SQLException e) {
			error("Unable to confirm DB contains the item / url association - %s", e.getMessage());
			r.setStat(Result.Status.BadAssociations);
			return false;
		}
	}

	private static boolean deleteAssociations() {
		try {
			PreparedStatement stmt = con
					.prepareStatement("DELETE FROM item_referenced_urls WHERE item_id = ? AND referenced_urls_id = ?");
			for(Result r : results) {
				stmt.setLong(1, r.getItemId());
				stmt.setLong(2, r.getReferencedUrlId());
				int res = stmt.executeUpdate();
				if(res != 1) {
					error("Something went wrong trying to delete the association item=[%d]/rurl=[%d].  Number of rows affected=[%d]", 
							r.getItemId(), r.getReferencedUrlId(), res);
				} else {
					info("Deleted association item=[%d]/rurl=[%d]", r.getItemId(), r.getReferencedUrlId());
				}
			}
		} catch (SQLException e) {
			error(e.getMessage());
			return false;
		}
		return true;
	}
	
	private static boolean deleteLonelyRUrls() {
		try {
			PreparedStatement stmt = con
					.prepareStatement("DELETE FROM referencedurl WHERE id = ?");
			for(Result r : results) {
				if(r.getStat() == Result.Status.DbConfirmedFullRemove) {
					stmt.setLong(1, r.getReferencedUrlId());
					int res = stmt.executeUpdate();
					if(res != 1) {
						error("Something went wrong trying to delete the referenced url=[%d].  Number of rows affected=[%d]", 
								r.getReferencedUrlId(), res);
					} else {
						info("Deleted referenced url=[%d]", r.getReferencedUrlId());
					}
				} else {
					info("NOT DELETING referenced url=[%d] since at least another item references it.", r.getReferencedUrlId());
				}
			}
		} catch (SQLException e) {
			error(e.getMessage());
			return false;
		}
		return true;
	}

	private static void error(String msg, Object... args) {
		log(false, msg, args);
	}

	private static void info(String msg, Object... args) {
		log(true, msg, args);
	}

	private static void log(boolean ok, String msg, Object... args) {
		String pre = "";
		if (line != 0) {
			pre = "Row[" + line + "] - ";
		}
		if (ok) {
			pre = "OK - " + pre;
		} else {
			pre = "ERROR - " + pre;
		}
		System.out.println(pre + String.format(msg, args));
	}

	private static String confirmItemIdIsValid(long id) {
		try {
			PreparedStatement stmt = con
					.prepareStatement("SELECT uuid, version FROM item WHERE id = ?");
			stmt.setLong(1, id);
			ResultSet rs = stmt.executeQuery();
			String uuid = "";
			String version = "";
			if (rs.next()) {
				uuid = rs.getString("uuid");
				version = rs.getString("version");
			}
			if (rs.next()) {
				error("Found more than 1 row for the item id.");
				uuid = "";
				version = "";
			}
			rs.close();
			if (uuid.isEmpty()) {
				error("Unable to confirm DB contains item id [%d].", id);
				return uuid;
			} else {
				String res = uuid + "/" + version;
				info("Confirmed item id [%d] belongs to item [%s]", id,
						res);
				return res;
			}
		} catch (SQLException e) {
			error("Unable to confirm DB contains item id [%d] - %s", id, e.getMessage());
			return "";
		}
	}

	private static boolean confirmReferencedUrlIdIsValid(long id) {
		try {
			PreparedStatement stmt = con
					.prepareStatement("SELECT url FROM referencedurl WHERE id = ?");
			stmt.setLong(1, id);
			ResultSet rs = stmt.executeQuery();
			String url = "";
			if (rs.next()) {
				url = rs.getString("url");
			}
			if (rs.next()) {
				error("Found more than 1 row for the referenced url id.");
				url = "";
			}
			rs.close();
			if (url.isEmpty()) {
				error("Unable to confirm DB contains referenced url id [%d].",
						id);
				return false;
			} else {
				info("Confirmed referenced url id [%d] belongs to rurl [%s]",
						id, url);
				return true;
			}
		} catch (SQLException e) {
			error("Unable to confirm DB contains referenced url id [%d] - %s", id, e.getMessage());
			return false;
		}
	}
}
