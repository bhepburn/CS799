package com.brenthepburn.cs799.mail;

import java.io.IOException;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class MailHelper {

	private static MailHelper instance;

	private Properties props;
	private Session session;

	private static final String ACCOUNT = "testemailbrent@gmail.com";
	private static final String PASSWORD = "brentt3st";
	private static final String PORT = "587";
	private static final String SERVER = "smtp.gmail.com";

	private MailHelper() {
		props = new Properties();
		props.put("mail.smtp.host", SERVER);
		props.put("mail.smtp.port", PORT);
		props.put("mail.from", ACCOUNT);
		props.put("mail.smtp.starttls.enable", "true");
		props.put("mail.smtp.auth", "true");

		session = Session.getDefaultInstance(props,
				new javax.mail.Authenticator() {
					protected PasswordAuthentication getPasswordAuthentication() {
						return new PasswordAuthentication(ACCOUNT, PASSWORD);
					}
				});
	}

	public static MailHelper getInstance() {
		if (instance == null)
			instance = new MailHelper();
		return instance;
	}

	public void mailMessage(String email, String subject, String value)
			throws IOException {
		try {

			MimeMessage message = new MimeMessage(session);
			message.setFrom(new InternetAddress(ACCOUNT));
			message.addRecipient(Message.RecipientType.TO, new InternetAddress(
					email));
			message.setSubject(subject);
			message.setContent(value, "text/plain");
			Transport.send(message);
		} catch (MessagingException mex) {
			System.out.println("send failed, exception: " + mex);
		}
	}

}
