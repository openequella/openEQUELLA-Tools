package com.pearson.equella.support.ping.report;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.Message.RecipientType;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.pearson.equella.support.ping.utils.Check;
import com.pearson.equella.support.ping.utils.Config;
import com.pearson.equella.support.ping.utils.Config.EmailReport;

/**
 * Borrowed heavily from the Equella email service impl.
 * 
 * 
 */
public class Email {
	private static final String HTML_MIME_TYPE = "text/html; charset=UTF-8";
	private static final String TEXT_MIME_TYPE = "text/plain; charset=UTF-8";
	
	private static final Logger logger = LogManager
			.getLogger(Email.class);


	public static boolean isValidAddress(String emailAddress) {
		try {
			new InternetAddress(emailAddress).validate();
			return true;
		} catch (AddressException e) {
			return false;
		}
	}

	public static List<String> parseAddresses(String emails) throws AddressException {
		String[] emailsList = new String[0];
		String rawEmail = emails;
		if (rawEmail != null) {
			rawEmail = rawEmail.replaceAll(";", " ");
			emailsList = rawEmail.split("\\s+");
		}

		List<String> addresses = new ArrayList<String>();
		for (String email : emailsList) {
			new InternetAddress(email).validate();
			addresses.add(email);
		}
		return addresses;
	}

	/**
	 * 
	 * @param subject
	 * @throws Exception
	 */
	public void send(String subject, String emailAddresses, String message) {
		if(Config.getInstance().getEmailReport() == EmailReport.NONE) {
			ReportManager.getInstance().addFatalError("Unable to send email.  Email settings not configured.");
			return;
		}
		final String senderEmail = Config.getInstance().getEmailSenderUsername();
		InternetAddress senderAddr;
		try {
			senderAddr = new InternetAddress(senderEmail,
					Config.getInstance().getEmailSenderDisplayName(), "UTF-8");
			final Properties props = System.getProperties();
			props.put("mail.smtp.auth", "true");
			props.put("mail.smtp.starttls.enable", "true");
			props.put("mail.smtp.host", Config.getInstance().getEmailSmtpServer());
			props.put("mail.smtp.port", Config.getInstance().getEmailSmtpServerPort());

			Session mailSession = null;
			Authenticator auth = getAuthenticator();
			if (auth != null) {
				props.put("mail.smtp.submitter", auth
						.getPasswordAuthentication().getUserName());
				mailSession = Session.getInstance(props, auth);
			} else {
				throw new Exception(String.format(
						"Unable to authenticate to [%s] as [%s]",
						Config.getInstance().getEmailSmtpServer(), Config.getInstance().getEmailSenderUsername()));
			}

			Message mimeMessage = new MimeMessage(mailSession);
			mimeMessage.setFrom(senderAddr);

			String[] toEmails = emailAddresses.split(";");
			for (String email : toEmails) {
				mimeMessage.addRecipient(RecipientType.TO, new InternetAddress(
						email));
			}
			mimeMessage.setSubject(subject);
			mimeMessage.setHeader("X-Mailer", "PingEquella");
			mimeMessage.setHeader("MIME-Version", "1.0");
			String type = TEXT_MIME_TYPE;
			// Hack
			if (message.contains("<html>")) {
				type = HTML_MIME_TYPE;
			}
			mimeMessage.setHeader("Content-Type", type);
			mimeMessage.setContent(message, type);
			logger.info("Sending report email to: {}", Config.getInstance().getEmailRecipients());
			Transport.send(mimeMessage);
			logger.info("Report email sent.");
		} catch (Exception e) {
			ReportManager.getInstance().addFatalError("Unable to send email:  "+e.getMessage());
		}
	}

	private Authenticator getAuthenticator() {
		String username = Config.getInstance().getEmailSenderUsername();
		String password = Config.getInstance().getEmailSenderPassword();
		if (!Check.isEmpty(username) && !Check.isEmpty(password)) {
			return new Authenticator(username, password);
		}
		return null;
	}

	private static class Authenticator extends javax.mail.Authenticator {
		private final PasswordAuthentication authentication;

		public Authenticator(String username, String password) {
			authentication = new PasswordAuthentication(username, password);
		}

		@Override
		public PasswordAuthentication getPasswordAuthentication() {
			return authentication;
		}
	}

}
