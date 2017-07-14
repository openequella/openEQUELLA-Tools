package com.pearson.equella.support.dupUsersAnalysis;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// Tested on a Equella 6.4-QA1 SQL Server DB
//
//does not handle:
//
//order*, payment*, pricing*,purchase*, sale*, store* tables
//

public class DupUserAnalysisDriver {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		//TODO externalize DB url, username, password
		String connectionUrl = "jdbc:sqlserver://the-db-server:52673;databaseName=the-db-name;user=the-db-username;password=the-db-password";

		try {
			Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
			Connection con = DriverManager.getConnection(connectionUrl);
			con.setAutoCommit(false);
			outf("Connected to %s", connectionUrl);

			outf("Retrieving duplicate users...");

			String SQL = "Select username, institution_id, count(*) as dupuser "
					+ "from tleuser  "
					+ "group by username, institution_id having count(*) > 1  ";
			Statement stmt = con.createStatement();
			ResultSet rs = stmt.executeQuery(SQL);
			boolean hasResults = rs.next();
			List<EqUser> dupUsers = new ArrayList<EqUser>();
			while (hasResults) {
				EqUser user = new EqUser();
				user.setUsername(rs.getString(1));
				user.setInstitutionId(rs.getString(2));

				dupUsers.add(user);
				outf("Found a duplicate user: " + user);
				hasResults = rs.next();
			}
			rs.close();

			outf("Checking details of duplicate users...");

			List<EqUser> allDupUserDetails = new ArrayList<EqUser>();
			Map<String, List<EqUser>> dupUserDetails = new HashMap<String, List<EqUser>>();
			Map<String, List<GenericEntity>> dupUserLmsLog = new HashMap<String, List<GenericEntity>>();
			Map<String, List<GenericEntity>> dupUserAuditLog = new HashMap<String, List<GenericEntity>>();
			Map<String, List<GenericEntity>> dupUserBaseEntities = new HashMap<String, List<GenericEntity>>();
			Map<String, List<GenericEntity>> dupUserBookmarks = new HashMap<String, List<GenericEntity>>();
			Map<String, List<GenericEntity>> dupUserComments = new HashMap<String, List<GenericEntity>>();
			Map<String, List<GenericEntity>> dupUserDrmAcceptances = new HashMap<String, List<GenericEntity>>();
			Map<String, List<GenericEntity>> dupUserEntityLocks = new HashMap<String, List<GenericEntity>>();
			Map<String, List<GenericEntity>> dupUserFavSearches = new HashMap<String, List<GenericEntity>>();
			Map<String, List<GenericEntity>> dupUserHistoryEvents = new HashMap<String, List<GenericEntity>>();
			Map<String, List<GenericEntity>> dupUserItems = new HashMap<String, List<GenericEntity>>();
			Map<String, List<GenericEntity>> dupUserItemLocks = new HashMap<String, List<GenericEntity>>();
			Map<String, List<GenericEntity>> dupUserNotifications = new HashMap<String, List<GenericEntity>>();
			Map<String, List<GenericEntity>> dupUserOAuthClients = new HashMap<String, List<GenericEntity>>();
			Map<String, List<GenericEntity>> dupUserOAuthTokens = new HashMap<String, List<GenericEntity>>();
			Map<String, List<GenericEntity>> dupUserPortletPreferences = new HashMap<String, List<GenericEntity>>();
			Map<String, List<GenericEntity>> dupUserTleGroupUsers = new HashMap<String, List<GenericEntity>>();
			Map<String, List<GenericEntity>> dupUserUserPreferences = new HashMap<String, List<GenericEntity>>();
			Map<String, List<GenericEntity>> dupUserWorkflowMessages = new HashMap<String, List<GenericEntity>>();
			
			for (EqUser dupUser : dupUsers) {
				List<EqUser> details = new ArrayList<EqUser>();
				SQL = "select id, uuid, username, first_name, last_name, email_address, password, institution_id"
						+ " from tleuser where username = ? and institution_id = ?";
				PreparedStatement pStmt = con.prepareStatement(SQL);
				pStmt.setString(1, dupUser.getUsername());
				pStmt.setInt(2, Integer.parseInt(dupUser.getInstitutionId()));
				rs = pStmt.executeQuery();
				hasResults = rs.next();
				while (hasResults) {
					EqUser user = new EqUser();
					user.setId(rs.getString(1));
					user.setUuid(rs.getString(2));
					user.setUsername(rs.getString(3));
					user.setFname(rs.getString(4));
					user.setLname(rs.getString(5));
					user.setEmail(rs.getString(6));
					user.setPassword(rs.getString(7));
					user.setInstitutionId(rs.getString(8));
					details.add(user);
					allDupUserDetails.add(user);
					hasResults = rs.next();
				}
				rs.close();
				dupUserDetails.put(dupUser.getUsername(), details);
			}
			for (EqUser dupUser : allDupUserDetails) {
				seed(dupUserLmsLog, dupUser.getUsername());
				seed(dupUserAuditLog, dupUser.getUsername());
				seed(dupUserBaseEntities, dupUser.getUsername());
				seed(dupUserBookmarks, dupUser.getUsername());
				seed(dupUserComments, dupUser.getUsername());
				seed(dupUserDrmAcceptances, dupUser.getUsername());
				seed(dupUserEntityLocks, dupUser.getUsername());
				seed(dupUserFavSearches, dupUser.getUsername());
				seed(dupUserHistoryEvents, dupUser.getUsername());
				seed(dupUserItems, dupUser.getUsername());
				seed(dupUserItemLocks, dupUser.getUsername());
				seed(dupUserNotifications, dupUser.getUsername());
				seed(dupUserOAuthClients, dupUser.getUsername());
				seed(dupUserOAuthTokens, dupUser.getUsername());
				seed(dupUserPortletPreferences, dupUser.getUsername());
				seed(dupUserTleGroupUsers, dupUser.getUsername());
				seed(dupUserUserPreferences, dupUser.getUsername());
				seed(dupUserWorkflowMessages, dupUser.getUsername());
				
				// Audit logs LMS
				SQL = "Select id, content_type, latest, resource, selected, session_id, timestamp, type, user_id, uuid, version, institution_id "
						+ "from audit_log_lms where uuid = ? and institution_id = ?";
				PreparedStatement pStmt = con.prepareStatement(SQL);
				pStmt.setString(1, dupUser.getUuid());
				pStmt.setInt(2, Integer.parseInt(dupUser.getInstitutionId()));
				rs = pStmt.executeQuery();
				hasResults = rs.next();
				while (hasResults) {
					AuditLogLms all = new AuditLogLms();
					all.setId(rs.getString(1));
					all.setContentType(rs.getString(2));
					all.setLatest(rs.getString(3));
					all.setResource(rs.getString(4));
					all.setSelected(rs.getString(5));
					all.setSessionId(rs.getString(6));
					all.setTimestamp(rs.getString(7));
					all.setType(rs.getString(8));
					all.setUserId(rs.getString(9));
					all.setUuid(rs.getString(10));
					all.setVersion(rs.getString(11));
					all.setInstitutionId(rs.getString(12));
					dupUserLmsLog.get(dupUser.getUsername()).add(all);
					hasResults = rs.next();
				}
				rs.close();

				// Audit log
				SQL = "Select id, user_id, institution_id, session_id, data1, data2, data3, data4, event_category, event_type, timestamp "
						+ "from audit_log_entry where user_id = ? and institution_id = ?";
				pStmt = con.prepareStatement(SQL);
				outf("Checking for audit logs with userId=[%s] and instId=[%s]",
						dupUser.getUuid(), dupUser.getInstitutionId());
				pStmt.setString(1, dupUser.getUuid());
				pStmt.setInt(2, Integer.parseInt(dupUser.getInstitutionId()));
				rs = pStmt.executeQuery();
				hasResults = rs.next();
				while (hasResults) {
					dupUserAuditLog.get(dupUser.getUsername()).add(
							new AuditLog(rs));
					hasResults = rs.next();
				}
				rs.close();

				// Base entity
				SQL = "Select id, date_created, date_modified, disabled, owner, system_type, uuid, description_id, "
						+ "institution_id, name_id from base_entity where owner = ? and institution_id = ?";
				pStmt = con.prepareStatement(SQL);
				outf("Checking base entities with owner=[%s] and instId=[%s]",
						dupUser.getUuid(), dupUser.getInstitutionId());
				pStmt.setString(1, dupUser.getUuid());
				pStmt.setInt(2, Integer.parseInt(dupUser.getInstitutionId()));
				rs = pStmt.executeQuery();
				hasResults = rs.next();
				while (hasResults) {
					dupUserBaseEntities.get(dupUser.getUsername()).add(
							new BaseEntity(rs));
					hasResults = rs.next();
				}
				rs.close();

				// Bookmark
				SQL = "select id, always_latest, date_modified, owner, institution_id, item_id "
						+ "from bookmark where owner = ? and institution_id = ?";
				pStmt = con.prepareStatement(SQL);
				outf("Checking bookmarks with owner=[%s] and instId=[%s]",
						dupUser.getUuid(), dupUser.getInstitutionId());
				pStmt.setString(1, dupUser.getUuid());
				pStmt.setInt(2, Integer.parseInt(dupUser.getInstitutionId()));
				rs = pStmt.executeQuery();
				hasResults = rs.next();
				while (hasResults) {
					dupUserBookmarks.get(dupUser.getUsername()).add(
							new Bookmark(rs));
					hasResults = rs.next();
				}
				rs.close();

				// Comment
				SQL = "select id, anonymous, comment, date_created, owner, rating, uuid, item_id "
						+ "from comments where owner = ?";
				pStmt = con.prepareStatement(SQL);
				outf("Checking comments with owner=[%s]", dupUser.getUuid());
				pStmt.setString(1, dupUser.getUuid());
				rs = pStmt.executeQuery();
				hasResults = rs.next();
				while (hasResults) {
					dupUserComments.get(dupUser.getUsername()).add(
							new Comment(rs));
					hasResults = rs.next();
				}
				rs.close();

				// DRM Acceptance
				SQL = "select id, \"date\", user, item_id "
						+ "from drm_acceptance where user = ?";
				pStmt = con.prepareStatement(SQL);
				outf("Checking DRM Acceptances with user=[%s]",
						dupUser.getUuid());
				pStmt.setString(1, dupUser.getUuid());
				rs = pStmt.executeQuery();
				hasResults = rs.next();
				while (hasResults) {
					dupUserDrmAcceptances.get(dupUser.getUsername()).add(
							new DrmAcceptance(rs));
					hasResults = rs.next();
				}
				rs.close();

				// Entity Lock
				SQL = "select id, userid, user_session, entity_id, institution_id "
						+ "from entity_lock where userid = ? and institution_id = ?";
				pStmt = con.prepareStatement(SQL);
				outf("Checking Entity Locks with userid=[%s] and instId=[%s]",
						dupUser.getUuid(), dupUser.getInstitutionId());
				pStmt.setString(1, dupUser.getUuid());
				pStmt.setInt(2, Integer.parseInt(dupUser.getInstitutionId()));
				rs = pStmt.executeQuery();
				hasResults = rs.next();
				while (hasResults) {
					dupUserEntityLocks.get(dupUser.getUsername()).add(
							new EntityLock(rs));
					hasResults = rs.next();
				}
				rs.close();

				// Fav Search
				SQL = "select id, criteria, date_modified, \"name\", owner, query, url, within, institution_id "
						+ "from favourite_search where owner = ? and institution_id = ?";
				pStmt = con.prepareStatement(SQL);
				outf("Checking Fav Searches with owner=[%s] and instId=[%s]",
						dupUser.getUuid(), dupUser.getInstitutionId());
				pStmt.setString(1, dupUser.getUuid());
				pStmt.setInt(2, Integer.parseInt(dupUser.getInstitutionId()));
				rs = pStmt.executeQuery();
				hasResults = rs.next();
				while (hasResults) {
					dupUserFavSearches.get(dupUser.getUsername()).add(
							new FavSearch(rs));
					hasResults = rs.next();
				}
				rs.close();

				// History Event
				SQL = "select id, applies, comment, \"date\", state, "
						+ "step, step_name, to_step, to_step_name, \"type\", user "
						+ "from history_event where user = ?";
				pStmt = con.prepareStatement(SQL);
				outf("Checking History Events with user=[%s]",
						dupUser.getUuid());
				pStmt.setString(1, dupUser.getUuid());
				rs = pStmt.executeQuery();
				hasResults = rs.next();
				while (hasResults) {
					dupUserHistoryEvents.get(dupUser.getUsername()).add(
							new HistoryEvent(rs));
					hasResults = rs.next();
				}
				rs.close();
				
				// Items
				SQL = "select id, date_created, date_for_index, date_modified, owner, status, " +
						"uuid, version, description_id, institution_id, item_xml_id, name_id "
						+ "from item where owner = ? and institution_id = ?";
				pStmt = con.prepareStatement(SQL);
				outf("Checking Items with owner=[%s] and instId=[%s]",
						dupUser.getUuid(), dupUser.getInstitutionId());
				pStmt.setString(1, dupUser.getUuid());
				pStmt.setInt(2, Integer.parseInt(dupUser.getInstitutionId()));
				rs = pStmt.executeQuery();
				hasResults = rs.next();
				while (hasResults) {
					dupUserItems.get(dupUser.getUsername()).add(
							new Item(rs));
					hasResults = rs.next();
				}
				rs.close();
				
				// Item Lock
				SQL = "select id, userid, user_session, institution_id, item_id "
						+ "from item_lock where userid = ? and institution_id = ?";
				pStmt = con.prepareStatement(SQL);
				outf("Checking Item Locks with userid=[%s] and instId=[%s]",
						dupUser.getUuid(), dupUser.getInstitutionId());
				pStmt.setString(1, dupUser.getUuid());
				pStmt.setInt(2, Integer.parseInt(dupUser.getInstitutionId()));
				rs = pStmt.executeQuery();
				hasResults = rs.next();
				while (hasResults) {
					dupUserItemLocks.get(dupUser.getUsername()).add(
							new ItemLock(rs));
					hasResults = rs.next();
				}
				rs.close();
				
				// Notification
				SQL = "select id, attempt_id, batched, \"date\", itemid, itemid_only, last_attempt, " +
						"processed, reason, user_to, institution_id "
						+ "from notification where user_to = ? and institution_id = ?";
				pStmt = con.prepareStatement(SQL);
				outf("Checking Notification with userTo=[%s] and instId=[%s]",
						dupUser.getUuid(), dupUser.getInstitutionId());
				pStmt.setString(1, dupUser.getUuid());
				pStmt.setInt(2, Integer.parseInt(dupUser.getInstitutionId()));
				rs = pStmt.executeQuery();
				hasResults = rs.next();
				while (hasResults) {
					dupUserNotifications.get(dupUser.getUsername()).add(
							new Notification(rs));
					hasResults = rs.next();
				}
				rs.close();
				
				// OAuth Client
				SQL = "select id, user_id, client_id, redirect_url, requires_approval "
						+ "from oauth_client where user_id = ?";
				pStmt = con.prepareStatement(SQL);
				outf("Checking OAuth Client with userId=[%s]", dupUser.getUuid());
				pStmt.setString(1, dupUser.getUuid());
				rs = pStmt.executeQuery();
				hasResults = rs.next();
				while (hasResults) {
					dupUserOAuthClients.get(dupUser.getUsername()).add(
							new OAuthClient(rs));
					hasResults = rs.next();
				}
				rs.close();
				
				// OAuth Token
				SQL = "select id, code, created, expiry, token, user_id, username, client_id, institution_id "
						+ "from oauth_token where user_id = ? and institution_id = ?";
				pStmt = con.prepareStatement(SQL);
				outf("Checking OAuth Token with userId=[%s] and instId=[%s]",
						dupUser.getUuid(), dupUser.getInstitutionId());
				pStmt.setString(1, dupUser.getUuid());
				pStmt.setInt(2, Integer.parseInt(dupUser.getInstitutionId()));
				rs = pStmt.executeQuery();
				hasResults = rs.next();
				while (hasResults) {
					dupUserOAuthTokens.get(dupUser.getUsername()).add(
							new OAuthToken(rs));
					hasResults = rs.next();
				}
				rs.close();
				
				// Portlet Preferences
				SQL = "select id, closed, minimised, \"order\", position, user_id, portlet_id "
						+ "from portlet_preference where user_id = ?";
				pStmt = con.prepareStatement(SQL);
				outf("Checking Portlet Preferences with userId=[%s]", dupUser.getUuid());
				pStmt.setString(1, dupUser.getUuid());
				rs = pStmt.executeQuery();
				hasResults = rs.next();
				while (hasResults) {
					dupUserPortletPreferences.get(dupUser.getUsername()).add(
							new PortletPreference(rs));
					hasResults = rs.next();
				}
				rs.close();
				
				// TLE Group Users
				SQL = "select tlegroup_id, element "
						+ "from tlegroup_users where element = ?";
				pStmt = con.prepareStatement(SQL);
				pStmt.setString(1, dupUser.getUuid());
				outf("Checking TLE Group Users with user (element)=[%s]", dupUser.getUuid());
				rs = pStmt.executeQuery();
				hasResults = rs.next();
				while (hasResults) {
					dupUserTleGroupUsers.get(dupUser.getUsername()).add(
							new TleGroupUser(rs));
					hasResults = rs.next();
				}
				rs.close();
				
				// User Preference
				SQL = "select institution, preferenceid, userid, data "
						+ "from user_preference where userid = ? and institution = ?";
				pStmt = con.prepareStatement(SQL);
				outf("Checking User Preference with userId=[%s] and instId=[%s]",
						dupUser.getUuid(), dupUser.getInstitutionId());
				pStmt.setString(1, dupUser.getUuid());
				pStmt.setInt(2, Integer.parseInt(dupUser.getInstitutionId()));
				rs = pStmt.executeQuery();
				hasResults = rs.next();
				while (hasResults) {
					dupUserUserPreferences.get(dupUser.getUsername()).add(
							new UserPreference(rs));
					hasResults = rs.next();
				}
				rs.close();
				
				// Workflow Message
				SQL = "select id, \"date\", message, type, user, node_id "
						+ "from workflow_message where user = ?";
				pStmt = con.prepareStatement(SQL);
				pStmt.setString(1, dupUser.getUuid());
				outf("Checking Workflow Messages with user=[%s]", dupUser.getUuid());
				rs = pStmt.executeQuery();
				hasResults = rs.next();
				while (hasResults) {
					dupUserWorkflowMessages.get(dupUser.getUsername()).add(
							new WorkflowMessage(rs));
					hasResults = rs.next();
				}
				rs.close();
				
			}

			outf("\n--------------------------");
			outf("- DUPLICATE USER DETAILS -");
			outf("--------------------------\n");
			for (String user : dupUserLmsLog.keySet()) {
				outf("--------------------");
				outf("User [%s]: ", user);
				outf("> Duplicate users:");
				for (EqUser eu : dupUserDetails.get(user)) {
					outf(">>> %s", eu);
				}
				displayDetailSection("LMS Logs", dupUserLmsLog, user);
				displayDetailSection("Audit Logs", dupUserAuditLog, user);
				displayDetailSection("Base Entities", dupUserBaseEntities, user);
				displayDetailSection("Bookmarks", dupUserBookmarks, user);
				displayDetailSection("Comments", dupUserComments, user);
				displayDetailSection("DRM Acceptances", dupUserDrmAcceptances, user);
				displayDetailSection("Entity Locks", dupUserEntityLocks, user);
				displayDetailSection("Fav Searches", dupUserFavSearches, user);
				displayDetailSection("History Events", dupUserHistoryEvents, user);
				displayDetailSection("Items", dupUserItems, user);
				displayDetailSection("Item Locks", dupUserItemLocks, user);
				displayDetailSection("Notifications", dupUserNotifications, user);
				displayDetailSection("OAuth Clients", dupUserOAuthClients, user);
				displayDetailSection("OAuth Tokens", dupUserOAuthTokens, user);
				displayDetailSection("Portlet Preferences", dupUserPortletPreferences, user);
				displayDetailSection("TLE Group-Users", dupUserTleGroupUsers, user);
				displayDetailSection("User Preference", dupUserUserPreferences, user);
			}
			outf("\n--------------------------");
			outf("- OTHER TABLES TO REVIEW -");
			outf("--------------------------\n");

			displaytable(
					con,
					"select id, attachment, citation, description, \"from\", location_id, "
							+ "location_name, override_reason, status, \"time\", type, until, user, "
							+ "uuid, course_info_id, item_id from activate_request",
					16);
			displaytable(con,
					"select author, buttons, client_js, config, extra, plugin_id, server_js, "
							+ "\"type\", id from html_editor_plugin", 9);
			displaytable(con,
					"select item_id, element from item_collaborators", 2);
			displaytable(
					con,
					"select id, age_days, query, user_id, portlet_id from portlet_recent_contrib",
					5);
			displaytable(con,
					"select lms_instance_id, resource_link_id, test_session_state, user_id, "
							+ "id, test_id from qti_assessment_result", 6);
			displaytable(
					con,
					"select id, activated, creator, email_address, expiry, privilege, started, institution_id, item_id from share_pass",
					9);
			displaytable(
					con,
					"select workflow_node_status_id, user from workflow_node_status_accepted",
					2);

			return;
		} catch (Exception e) {
			String msg = String.format("Unable to connect to the DB via %s",
					connectionUrl);
			System.out.println(msg);
			e.printStackTrace();
			return;
		}
	}

	private static void displayDetailSection(String header,
			Map<String, List<GenericEntity>> objs, String key) {
		outf("%s:", header);
		if (objs.get(key).isEmpty()) {
			outf("- No %s for this user.", header);
		}
		for (Object obj : objs.get(key)) {
			outf("- %s", obj.toString());
		}
	}

	private static void seed(Map<String, List<GenericEntity>> objs, String key) {
		if (!objs.containsKey(key)) {
			objs.put(key, new ArrayList<GenericEntity>());
		}
	}

	private static void outf(String msg, Object... args) {
		System.out.println(String.format(msg, args));
	}

	private static void displaytable(Connection con, String sql, int colSize)
			throws SQLException {
		PreparedStatement pStmt = con.prepareStatement(sql);
		ResultSet rs = pStmt.executeQuery();
		boolean hasResults = rs.next();
		outf("Displaying results of: %s", sql);
		if (!hasResults) {
			outf("No rows found.");
		}
		while (hasResults) {
			StringBuffer sb = new StringBuffer();
			for (int i = 1; i <= colSize; i++) {
				sb.append(String.format("[%s]", rs.getString(i)));
			}
			hasResults = rs.next();
			outf("Row: %s", sb.toString());
		}
		rs.close();
	}
}