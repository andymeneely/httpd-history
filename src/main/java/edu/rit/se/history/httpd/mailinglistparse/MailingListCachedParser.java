package edu.rit.se.history.httpd.mailinglistparse;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
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
		System.out.println("Starting parsing process...");
		MailingListCachedParser mailingListParser = new MailingListCachedParser();
		// mailingListParser.setUpDB();
		mailingListParser.loadFolder(PATH_TO_FILES);
		System.out.println(mailingListParser.emailData.get(1));

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

				} catch (IOException e) {
					fileLevelErrors++;
					// System.out.println("IOException on loadFolder: There is a problem reading file " +
					// file.getName()
					// + " - MSG: " + e.getMessage());
				}
			}

			System.out.println("Finished loading: " + file.getName());
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

		saveToDB(messages); // save the emails to the database.
	}

	private void saveToDB(MimeMessage[] messages) {

		for (int i = 0; i < messages.length; i++) {
			// BasicDBObject email = new BasicDBObject();
			HashMap<String, Object> email = new HashMap<String, Object>();
			try {

				email.put("messageID", messages[i].getMessageID());

				Set<String> from = new HashSet<String>();
				if (messages[i].getFrom() != null) {
					from = getEmailAdress(messages[i].getFrom());
					email.put("from", from);
				}

				ArrayList<String> replies = new ArrayList<String>();

				if (messages[i].getHeader("In-Reply-To") != null) {
					String inReplyTo = extractReplyID(messages[i].getHeader("In-Reply-To")[0]);
					email.put("inReplyTo", inReplyTo);
					replies.add(inReplyTo);
				}

				if (messages[i].getHeader("References") != null) {
					ArrayList<String> referenceList = getEmailAdress(messages[i].getHeader("References")[0]);
					email.put("references", referenceList);
					replies.addAll(referenceList);
				}

				if (!replies.isEmpty()) {
					recursiveProcessReplies(replies, from, true,0);
				}

				email.put("replies", replies);
				email.put("responders", new HashSet<String>());

				// email.put("sender", messages[i].getSender().toString());
				if (messages[i].getAllRecipients() != null) {
					email.put("allRecipients", getEmailAdress(messages[i].getAllRecipients()));
				}
				email.put("subject", messages[i].getSubject());
				//System.out.println(messages[i].getSubject());

				// email.put("content", getText(messages[i]));
				email.put("sentDate", messages[i].getSentDate());

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

	private void recursiveProcessReplies(ArrayList<String> emailList, Set<String> from, boolean direct,int level) {

		if (!emailList.isEmpty()) {

			for (Object object : emailList) {
				String emailId = (String) object;

				// BasicDBObject repliedQuery = new BasicDBObject();
				// repliedQuery.put("messageID", emailId);
				// BasicDBObject update = new BasicDBObject();

				HashMap<String, Object> email = emailData.get(emailId);

				if (email != null) {
					// if first recursion add an directReplies else add one indirect.
					if (direct) {

						int directRepliesCount = 0;
						if (email.containsKey("directRepliesCount")) {
							directRepliesCount = (Integer) email.get("directRepliesCount");
						}
						directRepliesCount++;
						email.put("directRepliesCount", directRepliesCount);

					} else {

						int indirectRepliesCount = 0;
						if (email.containsKey("indirectRepliesCount")) {
							indirectRepliesCount = (Integer) email.get("indirectRepliesCount");
						}
						indirectRepliesCount++;
						email.put("indirectRepliesCount", indirectRepliesCount);
					}

					// append individual responders.
					Set<String> responders = new HashSet<String>();

					for (String address : from) {
						responders.add(address);
					}

					email.put("responders", responders);

					// Find the specific reply.
					// DBObject email = emailCollection.findOne(repliedQuery);

					// List of replies of this reply.
					ArrayList<String> repliesList = new ArrayList<String>();

					// From
					Set<String> repliesFrom = new HashSet<String>();

					if (email != null && email.containsKey("inReplyTo") && email.get("inReplyTo") != null) {
						repliesList.add((String) email.get("inReplyTo"));
					}

					if (email != null && email.containsKey("references") && email.get("references") != null) {
						repliesList.addAll((ArrayList<String>) email.get("references"));
					}

					if (email != null && email.containsKey("from") && email.get("from") != null) {
						repliesFrom.addAll((Set<String>) email.get("from"));
					}

					if (email != null && email.containsKey("responders") && email.get("responders") != null) {
						repliesFrom.addAll((Set<String>) email.get("responders"));
					}

					if (!repliesList.isEmpty()) {
						level++;
						
						if (level > 50){							
							System.out.println("Recursion level is: "  + level);
						}
						recursiveProcessReplies(repliesList, repliesFrom, false, level);
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

	private ArrayList<String> getEmailAdress(String string) {

		ArrayList<String> result = new ArrayList<String>();

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
