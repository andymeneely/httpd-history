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
		// System.out.println("Starting recursive process... ");
		// mailingListParser.recursiveProcess();

		// Saves email to MySQL database.
		System.out.println("Saving to MySql... ");
		JDBMethods mysql = new JDBMethods("localhost", "mailinglist", "student");
		for (Email email : mailingListParser.emailData.values()) {
			mysql.insert(email);
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

				HashSet<String> replies = new HashSet<String>();

				if (messages[i].getHeader("In-Reply-To") != null) {
					String inReplyTo = extractReplyID(messages[i].getHeader("In-Reply-To")[0]);

					//if (!inReplyTo.equals("<no.id>")) {
						email.setInReplyTo(inReplyTo);
						replies.add(inReplyTo);
					//}
				}

				if (messages[i].getHeader("References") != null) {
					HashSet<String> referenceList = getEmailAdress(messages[i].getHeader("References")[0]);
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
				
				// Restart the cap of replies
				cap = 0;
				recursiveProcessReplies(email);
				emailCount++;

			} catch (MessagingException e) {
				emailLevelErrors++;
				// System.out.println("MessagingException on saveToDB: getContents: " + e.getMessage());
			}
		}
	}

	/*private void recursiveProcess() {
		for (Email email : emailData.values()) {
			//String messageID = email.getMessageID();
			// cap = 0;
			// recursiveProcessReplies(email, messageID);
			recursiveProcessReplies(email);
		}
	}*/

	int cap = 0;

	// private void recursiveProcessReplies(HashMap<String, Object> reply, String textDisplay) {
	private void recursiveProcessReplies(Email reply) {
		
		if (cap <= 500) {
			Set<String> repliedTo = reply.getReplies();

			if (!repliedTo.isEmpty()) {

				for (String id : repliedTo) {

					Email email = emailData.get(id);

					// String textnew = textDisplay.concat(" <- " + (String) id);
					// System.out.println(cap + " " + textnew);

					if (email != null) {

						email.addDirectReply(reply.getMessageID());
						email.addIndirectReply(reply.getDirectReplies());
						email.addIndirectReply(reply.getIndirectReplies());

						// append responders.
						email.addResponder(reply.getFrom());
						email.addResponder(reply.getResponders());

						if (!email.getReplies().isEmpty()) {
							cap++;
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
				//if (!emailAddress.getAddress().toString().equals("<no.id>")) {
					result.add(emailAddress.getAddress().toString());
				//}
			} else if (address instanceof NewsAddress) {
				NewsAddress newsAddress = (NewsAddress) address;
				result.add(newsAddress.toString());
			}
		}

		return result;
	}

	private HashSet<String> getEmailAdress(String string) {

		HashSet<String> result = new HashSet<String>();

		if (string != null) {
			String[] allRecipients = string.split("\\s+");
			for (String address : allRecipients) {
				//if (!address.equals("<no.id>")) {
					result.add(address);
				//}
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
		private HashSet<String> references = new HashSet<String>();

		// Metrics
		private HashSet<String> directReplies = new HashSet<String>();
		private HashSet<String> indirectReplies = new HashSet<String>();
		private HashSet<String> responders = new HashSet<String>();
		private HashSet<String> replies = new HashSet<String>();

		public HashSet<String> getAllRecipients() {
			return allRecipients;
		}

		public void setAllRecipients(HashSet<String> allRecipients) {
			this.allRecipients = allRecipients;
		}

		public void addReferences(String reference) {
			this.directReplies.add(reference);
		}

		public void setReplies(HashSet<String> replies) {
			this.replies = replies;
		}

		public HashSet<String> getReplies() {
			return replies;
		}

		public void addDirectReply(String reply) {
			this.directReplies.add(reply);
		}

		public void addIndirectReply(String reply) {
			this.indirectReplies.add(reply);
		}

		public void addDirectReply(HashSet<String> replies) {
			this.directReplies.addAll(replies);
		}

		public void addIndirectReply(HashSet<String> replies) {
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

		public HashSet<String> getReferences() {
			return references;
		}

		public void setReferences(HashSet<String> references) {
			this.references = references;
		}

		public HashSet<String> getDirectReplies() {
			return directReplies;
		}

		public void setDirectReplies(HashSet<String> directReplies) {
			this.directReplies = directReplies;
		}

		public HashSet<String> getIndirectReplies() {
			return indirectReplies;
		}

		public void setIndirectReplies(HashSet<String> indirectReplies) {
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
