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
public class SoapCreateUserDriver {
	private final String endpoint;
	private final String username;
	private final String password;

	public static void main(String[] args) {
		SoapCreateUserDriver driver = new SoapCreateUserDriver(true);
		String uuid = "......";
		String username = "...";
		String password = "....";
		String fName = "....";
		String lName = "....";
		String email = "....@.....";
		driver.execute(uuid, username, password, fName, lName, email);
	}
	
	public SoapCreateUserDriver(boolean useproxy) {
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
	
	protected void execute(String uuid, String uname, String pw, String fName, String lName, String email) {
		System.out.printf("Using settings: url=[%s], username=[%s]\n", endpoint, this.username);
		
		final EQUELLASOAP equella = new EQUELLASOAP(endpoint, username, password);
		
		System.out.printf("Creating user uuid=[%s], username=[%s], password=[%s], first-name=[%s], last-name=[%s], email=[%s]\n", uuid, uname, pw, fName, lName, email);
		
		String result = equella.addUser(uuid, uname, pw, fName, lName, email);

		if(result.equals(uuid)) {
			System.out.printf("User created.\n");	
		} else {
			System.out.printf("User creation attempted, but result was not the expected uuid.  Expected uuid=[%s], actual result=[%s]\n", uuid, result);	
		}
		
		
		equella.logout();
		
	}
}
