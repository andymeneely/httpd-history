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

	HashMap<String, HashMap<String, Object>> emailData = new HashMap<String, HashMap<String, Object>>();

	public static void main(String[] args) {
		
		
		PrintStream out;
		try {
			out = new PrintStream(new FileOutputStream("C:\\mailinglist\\output.txt"));
			System.setOut(out);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		System.out.println("Starting parsing process...");
		MailingListCachedParser mailingListParser = new MailingListCachedParser();
		// mailingListParser.setUpDB();
		mailingListParser.loadFolder(PATH_TO_FILES);

		System.out.println("Starting recursive process... ");

		mailingListParser.recursiveProcess();
		
		mailingListParser.emailData.get("<199512031349.HAA13837@sierra.zyzzyva.com>");
		System.out.println("Saving to MySql... ");
		JDBMethods mysql = new JDBMethods("localhost", "mailinglist", "student");
		
		for (HashMap<String, Object> email : mailingListParser.emailData.values()) {
			mysql.insert(email);
		}
				
		System.out.println("Processed Emails based on \\n\\nFrom : " + mailingListParser.quantity);
		System.out.println("File level errors: " + mailingListParser.fileLevelErrors);
		System.out.println("File parsion errors: " + mailingListParser.fileParsingErrors);
		System.out.println("Email level errors: " + mailingListParser.emailLevelErrors);

	}

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
					// System.out.println("IOException on loadFolder: There is a problem reading file " +
					// file.getName()
					// + " - MSG: " + e.getMessage());
				}
			}
		}
	}

	private void loadFile(String content) {
		String[] textMessages = content.split("\n\nFrom ");
		quantity += textMessages.length;
		for (int i = 1; i < textMessages.length; i++) {
			textMessages[i] = "From " + textMessages[i];
		}
		this.parseEmails(textMessages);
	}

	private void parseEmails(String[] textMessages) {
		MimeMessage[] messages = new MimeMessage[textMessages.length];
		Session s = Session.getDefaultInstance(new Properties());

		for (int i = 0; i < textMessages.length; i++) {
			InputStream is = new ByteArrayInputStream(textMessages[i].getBytes());
			try {
				messages[i] = new MimeMessage(s, is);
			} catch (MessagingException e) {
				emailLevelErrors++;
				// System.out.println("MessagingException on parseEmails: Creating the MimeMessage: " +
				// e.getMessage());
			}
		}

		saveToCache(messages); // save the emails to the database.
	}

	private void saveToCache(MimeMessage[] messages) {

		for (int i = 0; i < messages.length; i++) {
			// BasicDBObject email = new BasicDBObject();
			HashMap<String, Object> email = new HashMap<String, Object>();
			try {

				email.put("messageID", messages[i].getMessageID());

				Set<String> from = new HashSet<String>();
				if (messages[i].getFrom() != null) {
					from = getEmailAdress(messages[i].getFrom());
				}
				email.put("from", from);

				Set<String> replies = new HashSet<String>();

				if (messages[i].getHeader("In-Reply-To") != null) {
					String inReplyTo = extractReplyID(messages[i].getHeader("In-Reply-To")[0]);
					email.put("inReplyTo", inReplyTo);
					replies.add(inReplyTo);
				}

				if (messages[i].getHeader("References") != null) {
					Set<String> referenceList = getEmailAdress(messages[i].getHeader("References")[0]);
					email.put("references", referenceList);
					replies.addAll(referenceList);
				}
							
				email.put("replies", replies);

				if (messages[i].getAllRecipients() != null) {
					email.put("allRecipients", getEmailAdress(messages[i].getAllRecipients()));
				}
				
				email.put("subject", messages[i].getSubject());

				// email.put("content", getText(messages[i]));
				email.put("sentDate", messages[i].getSentDate());
				
				
				email.put("directReplies",new HashSet<String>());
				email.put("indirectReplies",new HashSet<String>());
				email.put("responders",new HashSet<String>());
				
				
				emailData.put((String) email.get("messageID"), email);

				emailCount++;

				// emailCollection.insert(email);
				// } catch (IOException e) {
				// emailLevelErrors++;
				// System.out.println("IOException on saveToDB: getContents or  IOUtils.copy error: " +
				// e.getMessage());
			} catch (MessagingException e) {
				emailLevelErrors++;
				// System.out.println("MessagingException on saveToDB: getContents: " + e.getMessage());
			}
		}
	}

	private void recursiveProcess() {
		for (HashMap<String, Object> email : emailData.values()) {
			// System.out.println(email.get("messageID"));
			Set<String> repliedTo = (Set<String>) email.get("replies");
			// Set<String> from = (Set<String>) email.get("from");

			if (!repliedTo.isEmpty()) {
				recursiveProcessReplies(email, true, 0, ((String) email.get("messageID")));
			}
		}
	}

	private void recursiveProcessReplies(HashMap<String, Object> reply, boolean direct, int level,
			String textDisplay) {
		
		level++;
		System.out.println("Level: " + level + " " + textDisplay);
		
		Set<String> repliedTo = (Set<String>) reply.get("replies");
		
		if (!repliedTo.isEmpty()) {

			for (Object id : repliedTo) {
				String emailId = (String) id;

				// BasicDBObject repliedQuery = new BasicDBObject();
				// repliedQuery.put("messageID", emailId);
				// BasicDBObject update = new BasicDBObject();

				HashMap<String, Object> email = emailData.get(emailId);

				if (email != null) {
					// if first recursion add one to directReplies else add one indirectReplies.
					//if (direct) {
						((Set<String>)email.get("directReplies")).add((String) reply.get("messageID"));
						((Set<String>)email.get("indirectReplies")).addAll((Set<String>) reply.get("directReplies"));
						((Set<String>)email.get("indirectReplies")).addAll((Set<String>) reply.get("indirectReplies"));
						
						/*int directRepliesCount = 0;
						if (email.containsKey("directRepliesCount")) {
							directRepliesCount = (Integer) email.get("directRepliesCount");
						}
						directRepliesCount++;
						email.put("directRepliesCount", directRepliesCount);*/						

				//} else {
					//	((Set<String>)email.get("indirectReplies")).add((String) reply.get("messageID"));
					//	((Set<String>)email.get("indirectReplies")).addAll((Set<String>) reply.get("directReplies"));
					//	((Set<String>)email.get("indirectReplies")).addAll((Set<String>) reply.get("indirectReplies"));
						
						/*int indirectRepliesCount = 0;
						if (email.containsKey("indirectRepliesCount")) {
							indirectRepliesCount = (Integer) email.get("indirectRepliesCount");
						}
						indirectRepliesCount++;
						email.put("indirectRepliesCount", indirectRepliesCount);*/
					//}

					// append individual responders.
					Set<String> responders = (Set<String>) reply.get("responders");
					Set<String> from = (Set<String>) reply.get("from");
					
					for (String address : from) {
						responders.add(address);
					}

					email.put("responders", responders);

					// List of replies of this reply.
					Set<String> repliesList = new HashSet<String>();

					// From
					Set<String> repliesFrom = new HashSet<String>();

					repliesList.addAll((Set<String>) email.get("replies"));
					repliesFrom.addAll((Set<String>) email.get("from"));
					repliesFrom.addAll((Set<String>) email.get("responders"));
					
					if (!repliesList.isEmpty()) {
						
						if (level > 50) {

							// System.out.println("Recursion level is: " + level);
						}
						recursiveProcessReplies(email, false, level, textDisplay + " <- " + emailId);
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

	private Set<String> getEmailAdress(Address[] allRecipients) {

		Set<String> result = new HashSet<String>();

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

	private Set<String> getEmailAdress(String string) {

		Set<String> result = new HashSet<String>();

		if (string != null) {
			String[] allRecipients = string.split("\\s+");
			for (String address : allRecipients) {
				result.add(address);
			}
		}

		return result;
	}

	/*
	 * private void setUpDB() { // mongoDB set up MongoClient mongo; try { mongo = new
	 * MongoClient("localhost", 27017); DB db = mongo.getDB("mailinglist"); this.emailCollection =
	 * db.getCollection("email"); } catch (UnknownHostException e) { //
	 * System.out.println("UnknownHostException on setUpDB: There was an while setting up the database :" //
	 * + e.getMessage()); } }
	 */

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
}
