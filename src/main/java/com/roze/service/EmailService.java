
package com.roze.service;

import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.springframework.stereotype.Service;

@Service
public class EmailService {

	public boolean sendEmail(String subject, String message, String to) {
		boolean f = false;
		String from = "roze6064@gmail.com";
		String host = "smtp.gmail.com";
		Properties properties = System.getProperties();
		System.out.println("Properties: " + properties);

		properties.put("mail.smtp.host", host);
		properties.put("mail.smtp.port", "465");
		properties.put("mail.smtp.ssl.enable", "true");
		properties.put("mail.smtp.auth", "true");

		// step 1: to get the session object

		Session session = Session.getInstance(properties, new Authenticator() {

			@Override
			protected PasswordAuthentication getPasswordAuthentication() {

				return new PasswordAuthentication("roze6064@gmail.com", "io06.Stream()$;");
			}
		});

		session.setDebug(true);

		MimeMessage m = new MimeMessage(session);

		try { // from email
			
			m.setFrom(from);
			
			//adding recipient to message
			m.addRecipient(Message.RecipientType.TO, new InternetAddress(to));

			// adding subject to message
			m.setSubject(subject);

			// adding text to message //
			//m.setText(message);
			
			m.setContent(message,"text/html");

			// step 3:Send the message using transport class 
			Transport.send(m);
			System.out.println("Sent Success....");
			f = true;

		} catch (Exception e) {
			e.printStackTrace();
		}
		return f;
	}

}
