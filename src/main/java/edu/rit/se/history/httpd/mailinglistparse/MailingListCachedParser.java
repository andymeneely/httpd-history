package edu.rit.se.history.httpd.mailinglistparse;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.StringWriter;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import javax.mail.Address;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.NewsAddress;

import org.apache.commons.io.IOUtils;

import com.mysql.jdbc.Statement;

public class MailingListCachedParser {

	final static String PATH_TO_FILES = "C:/mailinglist/downloads";

	// added variables to count success and error.
	int quantity = 0;
	int fileLevelErrors = 0;
	int fileParsingErrors = 0;
	int emailLevelErrors = 0;

	int emailCount = 0;

	// HashMap to contain the email data.
	HashMap<String, Email> emailData = new HashMap<String, Email>();

	public static void main(String[] args) {

		// Enable to Output log to file.
		// PrintStream out; try { out = new PrintStream(new FileOutputStream("C:\\mailinglist\\output.txt"));
		// System.setOut(out); } catch (FileNotFoundException e) { // TODO Auto-generated catch block
		// e.printStackTrace(); }

		MailingListCachedParser mailingListParser = new MailingListCachedParser();

		// Load emails to memory.
		System.out.println("Starting parsing process...");
		mailingListParser.loadFolder(PATH_TO_FILES);

		// Process email Replies
		System.out.println("Starting recursive process... ");
		mailingListParser.recursiveProcess();

		// Saves email to MySQL database.
		System.out.println("Saving to MySql... ");
		JDBMethods mysql = new JDBMethods("localhost", "mailinglist", "student");

		try {
			mysql.connect.setAutoCommit(false);

			for (Email email : mailingListParser.emailData.values()) {
				mysql.insert(email);
			}
			mysql.connect.commit();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// Prints out summary information.
		System.out.println("Processed Emails based on \\n\\nFrom : " + mailingListParser.quantity);
		System.out.println("File level errors: " + mailingListParser.fileLevelErrors);
		System.out.println("File parsion errors: " + mailingListParser.fileParsingErrors);
		System.out.println("Email level errors: " + mailingListParser.emailLevelErrors);

	}

	/*
	 * List all the files of a folder. Calls loadFile() on each file.
	 */
	private void loadFolder(String path) {
		File folder = new File(path);
		File[] listOfFiles = folder.listFiles();

		for (int i = 0; i < listOfFiles.length; i++) {
			File file = listOfFiles[i];
			if (file.isFile() && file.getName().endsWith(".txt")) {
				String content;
				try {
					content = readFile(file.getAbsolutePath(), StandardCharsets.UTF_8);
					this.loadFile(content); // Loads the contents of a file.
					System.out.println("Finished loading: " + file.getName());
				} catch (IOException e) {
					fileLevelErrors++;
				}
			}
		}
	}

	/*
	 * Splits files in to a String Array for parsing.
	 */

	private void loadFile(String content) {
		String[] textMessages = content.split("\n\nFrom ");
		quantity += textMessages.length;
		for (int i = 1; i < textMessages.length; i++) {
			textMessages[i] = "From " + textMessages[i];
		}
		this.parseEmails(textMessages);
	}

	/*
	 * Parses the the String email to a MimeMessage.
	 */
	private void parseEmails(String[] textMessages) {
		MimeMessage[] messages = new MimeMessage[textMessages.length];
		Session s = Session.getDefaultInstance(new Properties());

		for (int i = 0; i < textMessages.length; i++) {
			InputStream is = new ByteArrayInputStream(textMessages[i].getBytes());
			try {
				messages[i] = new MimeMessage(s, is);
			} catch (MessagingException e) {
				emailLevelErrors++;
			}
		}
		saveToCache(messages);
	}

	/*
	 * Saves the emails to the cache.
	 */

	private void saveToCache(MimeMessage[] messages) {

		for (int i = 0; i < messages.length; i++) {

			Email email = new Email();
			try {

				email.setMessageID(messages[i].getMessageID());

				HashSet<String> from = new HashSet<String>();
				if (messages[i].getFrom() != null) {
					from = getEmailAdress(messages[i].getFrom());
				}
				email.setFrom(from);

				HashSet<Email> replies = new HashSet<Email>();

				if (messages[i].getHeader("In-Reply-To") != null) {
					String inReplyTo = extractReplyID(messages[i].getHeader("In-Reply-To")[0]);

					email.setInReplyTo(inReplyTo);
					replies.add(emailData.get(inReplyTo));

				}

				if (messages[i].getHeader("References") != null) {
					HashSet<Email> referenceList = getEmailAdress(messages[i].getHeader("References")[0]);
					email.setReferences(referenceList);
					replies.addAll(referenceList);
				}

				email.setReplies(replies);

				if (messages[i].getAllRecipients() != null) {
					email.setAllRecipients(getEmailAdress(messages[i].getAllRecipients()));
				}

				email.setSubject(messages[i].getSubject());

				// email.put("content", getText(messages[i]));

				email.setSentDate(messages[i].getSentDate());

				emailData.put(email.getMessageID(), email);

				// recursiveProcessReplies(email);
				emailCount++;

			} catch (MessagingException e) {
				emailLevelErrors++;
				// System.out.println("MessagingException on saveToDB: getContents: " + e.getMessage());
			}
		}
	}

	private void recursiveProcess() {
		for (Email email : emailData.values()) {
			// String messageID =
			// email.getMessageID();
			// cap = 0;
			// recursiveProcessReplies(email, messageID);
			recursiveProcessReplies(email);
		}
	}

	int cap = 0;

	// private void recursiveProcessReplies(HashMap<String, Object> reply, String textDisplay) {
	private void recursiveProcessReplies(Email reply) {

		if (!reply.getReplies().isEmpty()) {

			for (Email email : reply.getReplies()) {

				// String textnew = textDisplay.concat(" <- " + (String) id);
				// System.out.println(cap + " " + textnew);

				if (email != null) {

					// Verify if the recursion is needed for this particular email.

					if (!email.directReplies.contains(reply)
							|| !email.indirectReplies.containsAll(reply.getDirectReplies())
							|| !email.indirectReplies.containsAll(reply.getIndirectReplies())) {

						email.addDirectReply(reply);
						email.addIndirectReply(reply.getDirectReplies());
						email.addIndirectReply(reply.getIndirectReplies());

						// append responders.
						email.addResponder(reply.getFrom());
						email.addResponder(reply.getResponders());

						if (!email.getReplies().isEmpty()) {
							// recursiveProcessReplies(email, textnew);
							recursiveProcessReplies(email);
						}
					}
				}
			}
		}
	}

	private String extractReplyID(String reply) {
		String result = "";
		try {
			result = reply.substring(reply.indexOf("<"), reply.indexOf(">") + 1);
		} catch (StringIndexOutOfBoundsException e) {
			result = reply;
		}
		return result;
	}

	private HashSet<String> getEmailAdress(Address[] allRecipients) {

		HashSet<String> result = new HashSet<String>();

		for (Address address : allRecipients) {

			if (address instanceof InternetAddress) {
				InternetAddress emailAddress = (InternetAddress) address;
				
				result.add(emailAddress.getAddress().toString());
				
			} else if (address instanceof NewsAddress) {
				NewsAddress newsAddress = (NewsAddress) address;
				result.add(newsAddress.toString());
			}
		}

		return result;
	}

	private HashSet<Email> getEmailAdress(String string) {

		HashSet<Email> result = new HashSet<Email>();

		if (string != null) {
			String[] allRecipients = string.split("\\s+");
			for (String address : allRecipients) {
				result.add(emailData.get(address));
			}
		}

		return result;
	}

	private static boolean textIsHtml = false;

	/**
	 * Return the primary text content of the message.
	 */
	private static String getText(Part p) throws MessagingException, IOException {

		if (p.isMimeType("text/enriched")) {
			InputStream is = (InputStream) p.getContent();
			StringWriter writer = new StringWriter();
			IOUtils.copy(is, writer);
			return writer.toString();
		}

		if (p.isMimeType("text/*") && !p.isMimeType("text/enriched")) {
			p.getContentType();

			try {
				String s = (String) p.getContent();
				textIsHtml = p.isMimeType("text/html");
				return s;
			} catch (ClassCastException e) {
				InputStream is = (InputStream) p.getContent();
				StringWriter writer = new StringWriter();
				IOUtils.copy(is, writer);
				textIsHtml = p.isMimeType("text/html");
				return writer.toString();
			}

		}

		if (p.isMimeType("multipart/alternative")) {
			// prefer html text over plain text
			Multipart mp = (Multipart) p.getContent();
			String text = null;
			for (int i = 0; i < mp.getCount(); i++) {
				Part bp = mp.getBodyPart(i);
				if (bp.isMimeType("text/plain")) {
					if (text == null)
						text = getText(bp);
					return text;
					// continue;
				} else if (bp.isMimeType("text/html")) {
					String s = getText(bp);
					if (s != null)
						return s;
				} else {
					return getText(bp);
				}
			}
			return text;
		} else if (p.isMimeType("multipart/*")) {
			Multipart mp = (Multipart) p.getContent();
			for (int i = 0; i < mp.getCount(); i++) {
				String s = getText(mp.getBodyPart(i));
				if (s != null)
					return s;
			}
		}

		return null;
	}

	static String readFile(String path, Charset encoding) throws IOException {
		byte[] encoded = Files.readAllBytes(Paths.get(path));
		return encoding.decode(ByteBuffer.wrap(encoded)).toString();
	}

	public class Email {
		// Email info
		private String messageID;
		private String subject;
		private Date sentDate;
		private HashSet<String> from = new HashSet<String>();
		private HashSet<String> allRecipients = new HashSet<String>();

		private String inReplyTo;
		private HashSet<Email> references = new HashSet<Email>();

		// Metrics
		private HashSet<String> responders = new HashSet<String>();
		private HashSet<Email> replies = new HashSet<Email>();
		
		private HashSet<Email> directReplies = new HashSet<Email>();
		private HashSet<Email> indirectReplies = new HashSet<Email>();
		
		

		public HashSet<String> getAllRecipients() {
			return allRecipients;
		}

		public void setAllRecipients(HashSet<String> allRecipients) {
			this.allRecipients = allRecipients;
		}

		public void addReferences(Email reference) {
			this.directReplies.add(reference);
		}

		public void setReplies(HashSet<Email> replies) {
			this.replies = replies;
		}

		public HashSet<Email> getReplies() {
			return replies;
		}

		public boolean processedDirectReply(Email reply) {

			if (this.directReplies.contains(reply)) {
				return true;
			}

			return false;
		}

		public boolean processedIndirectReply(Email reply) {

			if (this.indirectReplies.contains(reply)) {
				return true;
			}

			return false;
		}

		public void addDirectReply(Email reply) {
			this.directReplies.add(reply);
		}

		public void addIndirectReply(Email reply) {
			this.indirectReplies.add(reply);
		}

		public void addDirectReply(HashSet<Email> replies) {
			this.directReplies.addAll(replies);
		}

		public void addIndirectReply(HashSet<Email> replies) {
			this.indirectReplies.addAll(replies);
		}

		public void addResponder(String responder) {
			this.responders.add(responder);
		}

		public void addResponder(HashSet<String> responders) {
			this.responders.addAll(responders);
		}

		public String getMessageID() {
			return messageID;
		}

		public void setMessageID(String messageID) {
			this.messageID = messageID;
		}

		public String getSubject() {
			return subject;
		}

		public void setSubject(String subject) {
			this.subject = subject;
		}

		public Date getSentDate() {
			return sentDate;
		}

		public void setSentDate(Date sentDate) {
			this.sentDate = sentDate;
		}

		public HashSet<String> getFrom() {
			return from;
		}

		public void setFrom(HashSet<String> from) {
			this.from = from;
		}

		public String getInReplyTo() {
			return inReplyTo;
		}

		public void setInReplyTo(String inReplyTo) {
			this.inReplyTo = inReplyTo;
		}

		public HashSet<Email> getReferences() {
			return references;
		}

		public void setReferences(HashSet<Email> references) {
			this.references = references;
		}

		public HashSet<Email> getDirectReplies() {
			return directReplies;
		}

		public void setDirectReplies(HashSet<Email> directReplies) {
			this.directReplies = directReplies;
		}

		public HashSet<Email> getIndirectReplies() {
			return indirectReplies;
		}

		public void setIndirectReplies(HashSet<Email> indirectReplies) {
			this.indirectReplies = indirectReplies;
		}

		public HashSet<String> getResponders() {
			return responders;
		}

		public void setResponders(HashSet<String> responders) {
			this.responders = responders;
		}

	}

}
