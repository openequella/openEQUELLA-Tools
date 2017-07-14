package com.pearson.equella.support.oauthtester.jetty;

import java.util.logging.LogManager;

import org.apache.logging.log4j.Logger;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

import com.pearson.equella.support.oauthtester.servlets.EntryServlet;
import com.pearson.equella.support.oauthtester.servlets.PrimaryServlet;
import com.pearson.equella.support.oauthtester.util.Config;
 
public class EmbeddedJetty {
	private static final Logger logger = LogManager.getLogger(EmbeddedJetty.class);

	public void startServer() {
		try {
			Server server = new Server();
			ServerConnector c = new ServerConnector(server);
			c.setIdleTimeout(1000);
			c.setAcceptQueueSize(10);
			c.setPort(Config.getPort());
			c.setHost(Config.getHostname());
			ServletContextHandler handler = new ServletContextHandler(server,
					"/eqsupport", true, false);
			ServletHolder servletHolder = new ServletHolder(EntryServlet.class);
			handler.addServlet(servletHolder, "/entry");
			ServletHolder servletHolder2 = new ServletHolder(PrimaryServlet.class);
			handler.addServlet(servletHolder2, "/primary");
			server.addConnector(c);
			server.start();
			logger.info(String.format("To access the tester, please browse to http://%s:%d/eqsupport/entry", Config.getHostname(), Config.getPort()));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}