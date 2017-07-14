package com.pearson.equella.support.ping;

import java.io.File;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.pearson.equella.support.ping.direct.DirectPingHandler;
import com.pearson.equella.support.ping.direct.ResultsRow;
import com.pearson.equella.support.ping.report.ReportManager;
import com.pearson.equella.support.ping.report.ResultComparison;
import com.pearson.equella.support.ping.utils.Config;
import com.pearson.equella.support.ping.utils.PingUtils;
import com.pearson.equella.support.ping.webui.AttachmentPingHandler;

public class PingEquellaDriver {
	private static final Logger logger = LogManager
			.getLogger(PingEquellaDriver.class);

	public static void main(String[] args) {
		if((args.length > 0) && args[0].equals("-compare")) {
			compareReports(args);
		} else if (args.length > 0){
			logger.error("Unknown directive [{}]", args[0]);
		} else {
			runReport();
		}
	}
	
	private static void compareReports(String[] args) {
		logger.info("### Starting PingEquella Report Comparison...");
		endCompare: {
			if(args.length != 3) {
				logger.error("### Must invoke with two filenames/paths.");
				break endCompare;
			}
			File r1 = new File(args[1]);
			File r2 = new File(args[2]);
			if(!r1.exists()) {
				logger.error("### First file [{}] does not exist.", args[1]);
				break endCompare;
			}
			if(!r2.exists()) {
				logger.error("### Second file [{}] does not exist.", args[2]);
				break endCompare;
			}
			
			ResultComparison cr = PingUtils.looseCompare(r1, r2);
			
			if(cr == null) {
				logger.error("### Error comparing files.");
				break endCompare;
			}
			
			if(cr.areReportsEqual()) {
				logger.info("Reports ARE similar (in terms of resultant attachments)!");
			} else {
				logger.info("Reports are NOT similar (in terms of resultant attachments)!  Listing differences...");
				logger.info("Extra rows in the first report ({}):", cr.getFirstName());
				for(ResultsRow rr : cr.getOnlyInFirst()) {
					logger.info("\t- {}", rr);
				}logger.info("Extra rows in the second report ({}):", cr.getSecondName());
				for(ResultsRow rr : cr.getOnlyInSecond()) {
					logger.info("\t- {}", rr);
				}
			}
		}
		logger.info("### PingEquella Report Comparison ended.");
	}
	
	private static void runReport() {
		logger.info("### Starting PingEquella report run...");
		logger.info("### Checking configs from ping-equella.properties...");
		
		if (!setup("ping-equella.properties")) {
			logger.fatal("### Setup check failed.  Exiting...");
			return;
		}
		logger.info("### Config check passed.");

		if(!run()) {
			logger.fatal("### Something went wrong running the report.  Exiting...");
			ReportManager.getInstance().failFast();
			return;
		}
		
		if(!finalizeRun()) {
			logger.fatal("### Something went wrong finalizing the report.  Exiting...");
			return;
		}
		
		logger.info("### PingEquella {} report completed.",
				Config.getInstance().getPingType());
	}

	public static boolean setup(String propFile) {
		Config.reset();
		ReportManager.reset();
		Config.getInstance().setup(propFile);
		if(!Config.getInstance().isSetupValid()) {
			return false;
		}
		ReportManager.getInstance().setup();
		if (!ReportManager.getInstance().isValid()) {
			return false;
		}
		return true;
	}

	public static boolean run() {
		if (Config.getInstance().getPingType().equals(Config.PING_TYPE_ATTS)) {
			return (new AttachmentPingHandler()).execute();
		} else if (Config.getInstance().getPingType().equals(Config.PING_TYPE_DIRECT_ALL_ITEMS_ALL_ATTS) ||
				Config.getInstance().getPingType().equals(Config.PING_TYPE_DIRECT_BATCH_ITEMS_PERITEM_ATTS)) {
			return (new DirectPingHandler()).execute();
		}
		logger.error("Unknown ping.type...");
		return false;
	}

	public static boolean finalizeRun() {
		return ReportManager.getInstance().finalizeReporting();
	}
}
