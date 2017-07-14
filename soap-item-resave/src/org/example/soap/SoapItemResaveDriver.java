package org.example.soap;

import java.io.FileInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.Authenticator;
import java.net.InetSocketAddress;
import java.net.PasswordAuthentication;
import java.net.ProxySelector;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.sql.Savepoint;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

@SuppressWarnings("nls")
public class SoapItemResaveDriver {
	private final String endpoint;
	private final String username;
	private final String password;

	private final String itemUuid;
	private final int itemVersion;

	public static void main(String[] args) {
		SoapItemResaveDriver driver = new SoapItemResaveDriver(true);
		driver.execute();
	}
	
	public SoapItemResaveDriver(boolean useproxy) {
		Properties settings = new Properties();
		InputStream propStream = null;
		try {
			try {
				settings.load(new FileInputStream("settings.properties"));
			} catch (Exception e) {
				throw new Exception(String.format(
						"Unable to find / read settings.properties: %s \n",
						e.getMessage()));
			}

			endpoint = settings.getProperty("endpoint");
			username = settings.getProperty("username");
			password = settings.getProperty("password");

			itemUuid = settings.getProperty("item.uuid");
			if((itemUuid == null) || (itemUuid.length() != 36)) {
				String msg = String.format("item.uuid needs to be provided and should be 36 characters long.");
				throw new Exception(msg);
			}
			String itemVersionStr = settings.getProperty("item.version");
			try {
				itemVersion = Integer.parseInt(itemVersionStr);
			} catch (Exception e) {
				System.out.printf("item.version cannot be parsed as a number [%s].\n",itemVersionStr);
				throw e;
			}
			
			if(useproxy) {
				setupProxy(settings.getProperty("proxyHost"),
						settings.getProperty("proxyPort"),
						settings.getProperty("proxyUsername"),
						settings.getProperty("proxyPassword"));
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		} finally {
			if (propStream != null) {
				try {
					propStream.close();
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}
		}
	}

	private void setupProxy(String proxyHost, String proxyPort,
			final String proxyUser, final String proxyPassword) {
		if (proxyHost != null && proxyHost.trim().length() > 0) {
			Properties sysprops = System.getProperties();

			// Older JVMs
			sysprops.put("proxySet", "true");
			sysprops.put("proxyHost", proxyHost);
			sysprops.put("proxyPort", proxyPort);

			// Java 1.4 and up
			sysprops.put("http.proxyHost", proxyHost);
			sysprops.put("http.proxyPort", proxyPort);
			sysprops.put("https.proxyHost", proxyHost);
			sysprops.put("https.proxyPort", proxyPort);
			sysprops.put("ftp.proxyHost", proxyHost);
			sysprops.put("ftp.proxyPort", proxyPort);

			// Setup any authentication
			if (proxyUser != null && proxyUser.length() > 0) {
				// JDK 1.4
				Authenticator.setDefault(new Authenticator() {
					@Override
					protected PasswordAuthentication getPasswordAuthentication() {
						return new PasswordAuthentication(proxyUser,
								proxyPassword.toCharArray());
					}
				});

				// Older JVMs
				sysprops.put("http.proxyUser", proxyUser);
				sysprops.put("http.proxyPassword", proxyPassword);
			}

			System.setProperties(sysprops);
		}
	}
	
	protected void execute() {
		System.out.printf("Getting item %s/%d/\n", itemUuid,itemVersion);
		
		final EQUELLASOAP equella = new EQUELLASOAP(endpoint, username, password);

		final XMLWrapper result = equella.getItem(itemUuid, itemVersion);

		System.out.printf("Item retrieved [%s].  Saving item...\n", result.toString());
		
		equella.saveItem(result, true);
		equella.logout();
		
		System.out.println("Item saved");
	}
}
