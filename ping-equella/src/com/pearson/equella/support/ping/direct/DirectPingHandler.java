package com.pearson.equella.support.ping.direct;

import java.io.File;
import java.io.IOException;
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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.microsoft.sqlserver.jdbc.SQLServerPreparedStatement;
import com.pearson.equella.support.ping.report.ReportManager;
import com.pearson.equella.support.ping.utils.Config;

/**
 * Walk through the filestore / institionName / Attachments directory. For each
 * uuid / version, query the DB for the attachments. Then confirm each
 * attachment referenced in the DB is present in the uuid / version 's
 * filestore.
 * 
 * 
 */
public class DirectPingHandler {
	private static final Logger logger = LogManager
			.getLogger(DirectPingHandler.class);

	private Connection con;
	private Map<Integer, CollectionRow> collectionsById = new HashMap<Integer, CollectionRow>();
	private Map<String, InstitutionRow> institutionsByShortname = new HashMap<String, InstitutionRow>();
	private Map<Integer, InstitutionRow> institutionsById = new HashMap<Integer, InstitutionRow>();
	// item id is the key. First ResultsRow in the cache list is the item
	// details.
	private Map<Integer, List<ResultsRow>> attachmentsCache = new HashMap<Integer, List<ResultsRow>>();

	private List<WhereClauseExpression> whereClauseExpressions = new ArrayList<WhereClauseExpression>();

	public boolean execute() {
		// Setup database connection
		setupSqlServerConnection(Config.getInstance().getDatabaseUrl(),
				Config.getInstance().getDatabaseUsername(), Config
						.getInstance().getDatabasePassword());
		if(ReportManager.getInstance().hasFatalErrors()) {
			closeConnection();
			return false;
		}

		cacheCollections();
		if(ReportManager.getInstance().hasFatalErrors()) {
			closeConnection();
			return false;
		}

		cacheInstitutions();
		if(ReportManager.getInstance().hasFatalErrors()) {
			closeConnection();
			return false;
		}

		confirmFilterByCollection();
		if(ReportManager.getInstance().hasFatalErrors()) {
			closeConnection();
			return false;
		}

		confirmFilterByInstitution();
		if (ReportManager.getInstance().hasFatalErrors()) {
			closeConnection();
			return false;
		}

		confirmAllInstitutionFilestores();
		if (ReportManager.getInstance().hasFatalErrors()) {
			closeConnection();
			return false;
		}

		if (!ReportManager.getInstance().setupReportDetails()) {
			closeConnection();
			return false;
		}

		// Cache Items
		if (Config.getInstance().getPingType().contains("all-items")) {
			if (!cacheItemsAll()) {
				closeConnection();
				return false;
			}
		} else {
			if (!cacheItemsBatched()) {
				closeConnection();
				return false;
			}
		}

		// Cache Attachments
		if (Config.getInstance().getPingType().endsWith("all-attachments")) {
			if (!cacheAttachmentsAll()) {
				closeConnection();
				return false;
			}
		} else {
			if (!cacheAttachmentsPerItem()) {
				closeConnection();
				return false;
			}
		}

		// Check attachments one item at a time
		int counter = 0;
		for (Integer itemId : attachmentsCache.keySet()) {
			counter++;
			if (!processItem(itemId, counter)) {
				closeConnection();
				return false;
			}
		}

		closeConnection();
		return true;
	}

	private boolean closeConnection() {
		try {
			if (con != null) {
				con.close();
				logger.debug("Closed DB connection");
			}
			return true;
		} catch (SQLException e) {
			logger.error("Unable to close connection due to {}",
					e.getMessage(), e);
			return false;
		}
	}

	/**
	 * 
	 * @param itemId
	 *            assumes this exists in the cache.
	 * @param counter
	 *            item counter
	 * @return
	 */
	private boolean processItem(int itemId, int itemCounter) {
		try {
			logger.info("Processing item [{}]...", itemCounter);
			ReportManager.getInstance().getStats().incNumTotalItems();

			List<ResultsRow> attachments = attachmentsCache.get(itemId);
			// Remember, the attachmentsCache for an item is always seeded with
			// an 'item' results row.
			// If there is only the seeded ResultsRow, there were no attachments
			// found.
			if (attachments.size() == 1) {
				attachments.get(0).setAttStatus(ResultsRow.NOATT);
				ReportManager.getInstance().dualWriteLn(
						attachments.get(0).toString());//TODO - this is a bug.  should not be writing out to the ERR file.
			} else {
				boolean allGood = true;
				int attTotal = attachments.size();
				// Start attCounter at 1 since the first element is always the
				// item framework.
				for (int attCounter = 1; attCounter < attachments.size(); attCounter++) {
					ResultsRow attRow = attachments.get(attCounter);
					ReportManager.getInstance().getStats()
							.incNumTotalAttachments();
					logger.info(
							"Processing item attachment [item#{}]-[att#{}] (out of [{}] attachments for this item)...",
							itemCounter, attCounter, attTotal);

					// Determine attachment status
					if (attRow.getAttStatus().equals(ResultsRow.IGNORED)) {
						// Consider not an error.
						logger.info("Attachment {} is ignored.",
								attRow.getAttFilePath());
						ReportManager.getInstance().getStats()
								.incNumTotalAttachmentsIgnored();
					} else if (doesAttachmentExist(attRow)) {
						logger.info("Attachment {} is present.",
								attRow.getAttFilePath());
						attRow.setAttStatus(ResultsRow.PRESENT);
					} else {
						logger.info("Attachment {} is missing.",
								attRow.getAttFilePath());
						ReportManager.getInstance().getStats()
								.incNumTotalAttachmentsMissing();
						attRow.setAttStatus(ResultsRow.MISSING);
						ReportManager.getInstance().errOutWriteln(
								attRow.toString());
						allGood = false;
					}
					// Always send the attachment report to the standard file
					// writer.
					ReportManager.getInstance()
							.stdOutWriteln(attRow.toString());
				}
				if (!allGood) {
					ReportManager.getInstance().getStats()
							.incNumTotalItemsAffected();
				}
			}
		} catch (IOException e) {
			logger.fatal("Unrecoverable error while processing item - {}",
					e.getMessage(), e);
			return false;
		}
		return true;
	}

	private boolean doesAttachmentExist(ResultsRow attRow) {
		int hash = attRow.getItemUuid().hashCode() & 127;
		String path = String.format("%s/%s/Attachments/%d/%s/%s/", Config
				.getInstance().getFilestoreDir(),
				institutionsById.get(attRow.getInstitutionId())
						.getFilestoreHandle(), hash, attRow.getItemUuid(),
				attRow.getItemVersion())
				+ attRow.getAttFilePath();
		logger.info("Using path [{}] to check attachment.", path);
		return (new File(path)).exists();
	}

	/**
	 * if there is a filter.by.institution.shortname specified, confirm there's
	 * a corresponding entry in the institution cache.
	 * 
	 * Sets a ReportManager fatal error if there isn't a corresponding entry.
	 * 
	 * @return
	 */
	private void confirmFilterByInstitution() {
		String shortname = Config.getInstance()
				.getFilterByInstitutionShortname();
		if (shortname.isEmpty()) {
			// non-existent / empty means check all institutions.
			return;
		}

		if (!institutionsByShortname.containsKey(shortname)) {
			String msg = String
					.format("The institution shortname to filter by [%s] is not in the institution cache.",
							shortname);
			logger.fatal(msg);
			ReportManager.getInstance().addFatalError(msg);
			return;
		}

		whereClauseExpressions.add(new WhereClauseExpression(
				"institution_id = ?", institutionsByShortname.get(shortname)
						.getId(), whereClauseExpressions.size() + 1));
	}

	/**
	 * Using the institution cache, determine if all filestores are accounted
	 * for.
	 * 
	 * Sets a ReportManager fatal error if there are any institutions without a
	 * corresponding filestore.
	 * 
	 * @return
	 */
	private void confirmAllInstitutionFilestores() {
		String filter = Config.getInstance().getFilterByInstitutionShortname();
		for (String shortname : institutionsByShortname.keySet()) {
			// filter == null >> looking at all institutions
			// filter == shortname >> only check that institution filestore
			// handle
			if ((filter.isEmpty()) || (filter.equals(shortname))) {
				InstitutionRow ir = institutionsByShortname.get(shortname);
				ir.setFilestoreHandle(shortname);
				File instFilestore = new File(Config.getInstance()
						.getFilestoreDir(), ir.getFilestoreHandle());
				if (!instFilestore.exists()) {
					logger.info(
							"Institution filestore [{}] does not exist.  Checking for an alias...",
							instFilestore.getAbsolutePath());
					// Might have changed the filestore handle. The config
					// should
					// specify.
					String handle = Config.getInstance().getFilestoreHandle(
							shortname);
					if (handle == null || handle.isEmpty()) {
						// Nothing specified. Fail.
						String msg = String
								.format("Institution [%s] filestore handle is different than shortname, "
										+ "but is not specified in the properties.",
										shortname);
						ReportManager.getInstance().addFatalError(msg);
						logger.error(msg);
						return;
					}
					ir.setFilestoreHandle(handle);
					instFilestore = new File(Config.getInstance()
							.getFilestoreDir(), ir.getFilestoreHandle());
					if (!instFilestore.exists()) {
						// Bad handle. Fail.
						String msg = String
								.format("Institution [%s] filestore handle is different than shortname, "
										+ "but the handle (directory) specified in the properties [%s] does not exist.",
										shortname, handle);
						ReportManager.getInstance().addFatalError(msg);
						logger.error(msg);
						return;
					}
				}

				// Check that the Attachments directory exists
				File attsHandle = new File(instFilestore, "Attachments");
				if (!attsHandle.exists()) {
					// Bad handle. Fail.
					String msg = String
							.format("Institution [%s] filestore attachments directory [%s] does not exist.",
									shortname, attsHandle.getAbsolutePath());
					ReportManager.getInstance().addFatalError(msg);
					logger.error(msg);
					return;
				}

				if (!attsHandle.isDirectory()) {
					// Bad handle. Fail.
					String msg = String
							.format("Institution [{}] filestore attachments directory [{}] is not a directory.",
									shortname, attsHandle.getAbsolutePath());
					ReportManager.getInstance().addFatalError(msg);
					logger.error(msg);
					return;
				}
			}
		}
	}

	private void setupSqlServerConnection(
			String sqlServerUrlDbNameInstanceName, String un, String pw) {
		try {
			Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
			String connectionUrl = String.format(
					"jdbc:sqlserver:%s;user=%s;password=%s",
					sqlServerUrlDbNameInstanceName, un, pw);
			con = DriverManager.getConnection(connectionUrl);
			logger.info(String.format("Connected to %s",
					sqlServerUrlDbNameInstanceName));
			con.setAutoCommit(false);
			// Check the connection works.
			String SQL = "SELECT TOP 1 id, uuid FROM item ";
			Statement stmt = con.createStatement();
			long start = System.currentTimeMillis();
			ResultSet rs = stmt.executeQuery(SQL);
			boolean hasResults = rs.next();
			rs.close();
			long dur = System.currentTimeMillis() - start;
			if (hasResults) {
				logger.info("Confirmed DB connection in 0 ms.");
				ReportManager.getInstance().getStats().queryRan(dur);
				return;
			} else {
				String msg = String.format(
						"Unable to confirm connection to the DB [%s] with username [%s] and a password of length [%d]",
						Config.getInstance().getDatabaseUrl(), Config
								.getInstance().getDatabaseUsername(), Config
								.getInstance().getDatabasePassword().length());
				logger.fatal(msg);
				ReportManager.getInstance().addFatalError(msg);
				return;
			}
		} catch (Exception e) {
			String msg = String.format(
					"Unable to connect to the DB [%s] with username [%s] and a password of length [%d]",
					Config.getInstance().getDatabaseUrl(), Config.getInstance()
							.getDatabaseUsername(), Config.getInstance()
							.getDatabasePassword().length(), e);
			logger.fatal(msg);
			ReportManager.getInstance().addFatalError(msg);
			return;
		}
	}

	private boolean cacheAttachmentsPerItem() {
		long start = System.currentTimeMillis();
		int counter = 0;
		try {
			// For each item cached, query for the associated attachments.
			for (List<ResultsRow> itemRowSet : attachmentsCache.values()) {
				long batchStart = System.currentTimeMillis();

				int itemId = itemRowSet.get(0).getItemId();
				PreparedStatement stmt = con
						.prepareStatement("select a.type, a.url, a.value1, a.uuid, a.id "
								+ "from attachment a where a.item_id = ?");
				stmt.setInt(1, itemId);
				ResultSet rs = stmt.executeQuery();
				while (rs.next()) {
					counter++;
					ResultsRow attRow = inflateAttRow(rs.getString(1),
							rs.getString(2), rs.getString(3), rs.getString(4),
							rs.getInt(5), itemId);
					// Note - at this point, attRow will not be null.
					attachmentsCache.get(attRow.getItemId()).add(attRow);
					logger.info("Attachment ({}) gathered: {}", counter,
							attRow.toString());
				}
				rs.close();
				long batchDur = System.currentTimeMillis() - batchStart;
				ReportManager.getInstance().getStats().queryRan(batchDur);
				logger.info("Cached a batch of attachments.  Duration {} ms.",
						batchDur);

			}
		} catch (Exception e) {
			logger.error("Unable to cache attachments (query per item) - {}",
					e.getMessage(), e);
			return false;
		}
		long dur = System.currentTimeMillis() - start;
		logger.info("Cached {} attachments (query per item).  Duration {} ms.",
				counter, dur);
		return true;
	}

	private boolean cacheAttachmentsAll() {
		long start = System.currentTimeMillis();
		int counter = 0;
		try {
			PreparedStatement stmt = con
					.prepareStatement("select a.type, a.url, a.value1, a.uuid, a.id, a.item_id "
							+ "from attachment a");
			ResultSet rs = stmt.executeQuery();
			// For each attachment, access the appropriate item attachment list
			// and add the attachment
			while (rs.next()) {
				counter++;
				ResultsRow attRow = inflateAttRow(rs.getString(1),
						rs.getString(2), rs.getString(3), rs.getString(4),
						rs.getInt(5), rs.getInt(6));
				if (attRow != null) {
					attachmentsCache.get(attRow.getItemId()).add(attRow);
					logger.info("Attachment ({}) gathered: {}", counter,
							attRow.toString());
				}
			}
			rs.close();
		} catch (Exception e) {
			logger.error("Unable to cache attachments (single query) - {}",
					e.getMessage(), e);
			return false;
		}
		long dur = System.currentTimeMillis() - start;
		ReportManager.getInstance().getStats().queryRan(dur);
		logger.info("Cached {} attachments (single query).  Duration {} ms.",
				counter, dur);
		return true;
	}

	private ResultsRow inflateAttRow(String attType, String attUrl,
			String value1, String attUuid, int attId, int itemId) {
		// If an attachment was found without the parent item, log a
		// warning, but effectively ignore.
		if (!attachmentsCache.containsKey(itemId)) {
			logger.warn(
					"Found an attachment whose itemId DOES NOT EXIST in the item cache.  "
							+ "Ignoring (not caching): type=[{}], url=[{}], value1=[{}], uuid=[{}], attId=[{}], itemId=[{}]",
					attType, attUrl, value1, attUuid, attId, itemId);
			return null;
		}

		// Build out the general attributes
		ResultsRow attRow = ResultsRow.buildItemFrame(attachmentsCache.get(
				itemId).get(0));

		// inflate the static metadata
		attRow.setAttId(attId);
		attRow.setAttUuid(attUuid);
		attRow.setAttUrl(attUrl);
		attRow.setAttType(attType);

		// Determine the filestore
		if (attRow.getAttType().equals("file")) {
			attRow.setAttFilePath(attRow.getAttUrl());
		} else if (attRow.getAttType().equals("zip")) {
			attRow.setAttFilePath(attRow.getAttUrl());
		} else if (attRow.getAttType().equals("html")) {
			attRow.setAttFilePath(String.format("_mypages/%s/page.html",
					attRow.getAttUuid()));
		} else if (attRow.getAttType().equals("custom")
				&& value1.equals("scorm")) {
			attRow.setAttType("scorm");
			// Unsupported for now.
			// filepath = String.format("_IMS/%s", attRow.getAttUrl());
			attRow.setAttStatus(ResultsRow.IGNORED);
		} else {
			// Ignore all other attachments (URLs, Flickr, Equella resources,
			// etc)
			attRow.setAttStatus(ResultsRow.IGNORED);
		}

		return attRow;
	}

	private void cacheCollections() {
		long start = System.currentTimeMillis();
		try {
			PreparedStatement stmt = con
					.prepareStatement("select c.id, e.uuid, e.institution_id "
							+ "from item_definition c "
							+ "inner join base_entity e on c.id = e.id");
			ResultSet rs = stmt.executeQuery();
			while (rs.next()) {
				CollectionRow cr = new CollectionRow();
				cr.setId(rs.getInt(1));
				cr.setInstitutionId(rs.getInt(3));
				cr.setUuid(rs.getString(2));

				collectionsById.put(cr.getId(), cr);
				// collectionsByUuid.put(rs.getString(2), rs.getInt(1));
				logger.info("Cached collection:  {}", cr.toString());
			}
			rs.close();
		} catch (Exception e) {
			String msg = String.format("Unable to cache collections - %s",
					e.getMessage());
			logger.fatal(msg, e);
			ReportManager.getInstance().addFatalError(msg);
			return;
		}
		long dur = System.currentTimeMillis() - start;
		ReportManager.getInstance().getStats().queryRan(dur);
		logger.info("Cached collections.  Duration {} ms.", dur);
		return;
	}

	private void cacheInstitutions() {
		long start = System.currentTimeMillis();
		try {
			PreparedStatement stmt = con
					.prepareStatement("select i.id, i.short_name, i.name, i.unique_id, i.url "
							+ "from institution i");
			ResultSet rs = stmt.executeQuery();
			while (rs.next()) {
				InstitutionRow ir = new InstitutionRow();
				ir.setId(rs.getInt(1));
				ir.setShortname(rs.getString(2));
				ir.setName(rs.getString(3));
				ir.setUniqueId(rs.getString(4));
				ir.setUrl(rs.getString(5));
				institutionsByShortname.put(ir.getShortname(), ir);
				institutionsById.put(ir.getId(), ir);
				logger.info("Cached institution: {}", ir.toString());
			}
			rs.close();
		} catch (Exception e) {
			String msg = String.format("Unable to cache institutions - %s",
					e.getMessage());
			logger.fatal(msg, e);
			ReportManager.getInstance().addFatalError(msg);
			return;
		}
		long dur = System.currentTimeMillis() - start;
		ReportManager.getInstance().getStats().queryRan(dur);
		logger.info("Cached institutions.  Duration {} ms.", dur);
		return;
	}

	private void confirmFilterByCollection() {
		if (Config.getInstance().getFilterByCollectionDirect() == Integer.MIN_VALUE) {
			logger.info("Filter by collection ID not specified.  Not filtering by collection");
			return;
		}

		if (!collectionsById.containsKey(Config.getInstance()
				.getFilterByCollectionDirect())) {
			String msg = String
					.format("Unable to find the collection ID [%s].  Not a valid filter.",
							Config.getInstance().getFilterByCollectionDirect());
			logger.fatal(msg);
			ReportManager.getInstance().addFatalError(msg);
			return;
		}

		whereClauseExpressions.add(new WhereClauseExpression(
				"item_definition_id = ?", Config.getInstance()
						.getFilterByCollectionDirect(), whereClauseExpressions
						.size() + 1));
	}

	private boolean cacheItemsAll() {
		long start = System.currentTimeMillis();
		try {
			PreparedStatement stmt = con
					.prepareStatement(String
							.format("select id, uuid, version, status, item_definition_id, institution_id from item %s order by id",
									WhereClauseExpression
											.makeWhereClause(whereClauseExpressions)));
			WhereClauseExpression.setParms(stmt, whereClauseExpressions);

			ResultSet rs = stmt.executeQuery();
			while (rs.next()) {
				ResultsRow itemRow = new ResultsRow();
				itemRow.setItemId(rs.getInt(1));
				itemRow.setItemUuid(rs.getString(2));
				itemRow.setItemVersion("" + rs.getInt(3));
				itemRow.setItemStatus(rs.getString(4));
				int collId = rs.getInt(5);
				if (collectionsById.containsKey(collId)) {
					itemRow.setCollectionUuid(collectionsById.get(collId)
							.getUuid());
				} else {
					logger.warn(
							"Expected collection ID [{}] not cached for item {}/{}.",
							collId, itemRow.getItemUuid(),
							itemRow.getItemVersion());
					itemRow.setCollectionUuid("" + collId);
				}
				itemRow.setInstitutionId(rs.getInt(6));
				itemRow.setInstitutionShortname(institutionsById.get(
						itemRow.getInstitutionId()).getShortname());
				List<ResultsRow> seeder = new ArrayList<ResultsRow>();
				seeder.add(itemRow);
				attachmentsCache.put(itemRow.getItemId(), seeder);

				logger.info("Found [{}] item:  {}", (attachmentsCache.size()),
						itemRow.toString());
			}
			rs.close();
		} catch (Exception e) {
			logger.error("Unable to cache items (single query) - {}",
					e.getMessage(), e);
			return false;
		}
		long dur = System.currentTimeMillis() - start;
		ReportManager.getInstance().getStats().queryRan(dur);
		logger.info("Cached items (single query).  Duration {} ms.", dur);
		return true;
	}

	/**
	 * Fails in SQL Server 2008. Works in SQL Server 2012.
	 * 
	 * @return
	 */
	private boolean cacheItemsBatched() {
		long start = System.currentTimeMillis();
		try {
			String sql = String
					.format("select id, uuid, version, status, item_definition_id, institution_id from item %s order by id OFFSET ? ROWS FETCH NEXT ? ROWS ONLY",
							WhereClauseExpression
									.makeWhereClause(whereClauseExpressions));
			logger.debug("SQL:  [{}]", sql);

			int offsetCounter = 0;
			boolean hasMore = false;
			do {
				// Assume no rows are returned.
				hasMore = false;
				long batchStart = System.currentTimeMillis();

				PreparedStatement stmt = con.prepareStatement(sql);
				WhereClauseExpression.setParms(stmt, whereClauseExpressions);
				stmt.setInt(whereClauseExpressions.size() + 1, offsetCounter
						* Config.getInstance().getNumItemsPerQuery());
				stmt.setInt(whereClauseExpressions.size() + 2, Config
						.getInstance().getNumItemsPerQuery());
				ResultSet rs = stmt.executeQuery();
				while (rs.next()) {
					hasMore = true;
					ResultsRow itemRow = new ResultsRow();
					itemRow.setItemId(rs.getInt(1));
					itemRow.setItemUuid(rs.getString(2));
					itemRow.setItemVersion("" + rs.getInt(3));
					itemRow.setItemStatus(rs.getString(4));
					int collId = rs.getInt(5);
					if (collectionsById.containsKey(collId)) {
						itemRow.setCollectionUuid(collectionsById.get(collId)
								.getUuid());
					} else {
						logger.warn(
								"Expected collection ID [{}] not cached for item {}/{}.",
								collId, itemRow.getItemUuid(),
								itemRow.getItemVersion());
						itemRow.setCollectionUuid("" + collId);
					}
					itemRow.setInstitutionId(rs.getInt(6));
					itemRow.setInstitutionShortname(institutionsById.get(
							itemRow.getInstitutionId()).getShortname());

					List<ResultsRow> seeder = new ArrayList<ResultsRow>();
					seeder.add(itemRow);
					attachmentsCache.put(itemRow.getItemId(), seeder);

					logger.info("Found the [{}]th item:  {}",
							(attachmentsCache.size()), itemRow.toString());

				}
				rs.close();
				offsetCounter++;
				long batchDur = System.currentTimeMillis() - batchStart;
				ReportManager.getInstance().getStats().queryRan(batchDur);
				if (hasMore) {
					logger.info("Cached a batch of items.  Duration {} ms.",
							batchDur);
				} else {
					logger.info(
							"No more batches of items found to cache.  Duration {} ms.",
							batchDur);
				}

			} while (hasMore);
		} catch (Exception e) {
			logger.error("Unable to cache items (batched queries) - {}",
					e.getMessage(), e);
			return false;
		}
		long dur = System.currentTimeMillis() - start;
		logger.info("Cached items (batched queries).  Total duration {} ms.",
				dur);
		return true;
	}
}
