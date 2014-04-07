package edu.rit.se.history.httpd.mailinglistparse;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream.GetField;
import java.io.StringWriter;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.mail.Address;
import javax.mail.Folder;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.URLName;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.NewsAddress;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.sun.mail.util.QPDecoderStream;

public class MailingListParser {

	final static String PATH_TO_FILES = "C:/mailinglist/downloads";
	DBCollection emailCollection;

	// added variables to count success and error.
	int quantity = 0;
	int fileLevelErrors = 0;
	int fileParsingErrors = 0;
	int emailLevelErrors = 0;

	public static void main(String[] args) {
		MailingListParser mailingListParser = new MailingListParser();
		mailingListParser.setUpDB();
		mailingListParser.loadFolder(PATH_TO_FILES);
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
					loadFile(content);
					System.out.println("Finished loading: " + file.getName());

				} catch (IOException e) {
					fileLevelErrors++;
					System.out.println("IOException on loadFolder: There is a problem reading file " + file.getName()
							+ " - MSG: " + e.getMessage());
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
		parseEmails(textMessages);

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
				System.out.println("MessagingException on parseEmails: Creating the MimeMessage: " + e.getMessage());
			}
		}
		saveToDB(messages); // save the emails to the database.

	}

	private void saveToDB(MimeMessage[] messages) {

		for (int i = 0; i < messages.length; i++) {
			BasicDBObject email = new BasicDBObject();
			try {

				email.put("messageID", messages[i].getMessageID());
				if (messages[i].getHeader("In-Reply-To") != null)
					email.put("inReplyTo", extractReplyID(messages[i].getHeader("In-Reply-To")[0]));
				if (messages[i].getHeader("References") != null)
					email.put("references", getEmailAdress(messages[i].getHeader("References")[0]));

				email.put("from", getEmailAdress(messages[i].getFrom()));
				// email.put("sender", messages[i].getSender().toString());
				email.put("allRecipients", getEmailAdress(messages[i].getAllRecipients()));
				email.put("subject", messages[i].getSubject());
				email.put("content", getText(messages[i]));
				email.put("sentDate", messages[i].getSentDate());
				emailCollection.insert(email);

			} catch (IOException e) {
				emailLevelErrors++;
				System.out.println("IOException on saveToDB: getContents or  IOUtils.copy error: " + e.getMessage());
			} catch (MessagingException e) {
				emailLevelErrors++;
				System.out.println("MessagingException on saveToDB: getContents: " + e.getMessage());
			}
		}
	}

	
	private String extractReplyID(String reply) {
		String result = "";
		try{
			result = reply.substring(reply.indexOf("<"), reply.indexOf(">")+1);
		}catch(StringIndexOutOfBoundsException e){
			result = reply;
		}
		
		return result;
	}
	
	
	private BasicDBList getEmailAdress(Address[] allRecipients) {

		BasicDBList result = new BasicDBList();

		if (allRecipients != null) {
			for (Address address : allRecipients) {

				if (address instanceof InternetAddress) {
					InternetAddress emailAddress = (InternetAddress) address;
					result.add(emailAddress.getAddress());
				} else if (address instanceof NewsAddress) {
					NewsAddress newsAddress = (NewsAddress) address;
					result.add(newsAddress.toString());
				}
			}
		}
		return result;
	}

	private BasicDBList getEmailAdress(String string) {

		BasicDBList result = new BasicDBList();

		if (string != null) {
			String[] allRecipients = string.split("\\s+");
			for (String address : allRecipients) {
				result.add(address);
			}
		}

		return result;
	}

	private void setUpDB() {
		// mongoDB set up
		MongoClient mongo;
		try {
			mongo = new MongoClient("localhost", 27017);
			DB db = mongo.getDB("mailinglist");
			this.emailCollection = db.getCollection("email");
		} catch (UnknownHostException e) {
			System.out.println("UnknownHostException on setUpDB: There was an while setting up the database :"
					+ e.getMessage());
		}
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
}
