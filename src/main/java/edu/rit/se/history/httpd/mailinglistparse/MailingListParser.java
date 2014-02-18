package edu.rit.se.history.httpd.mailinglistparse;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream.GetField;
import java.io.StringWriter;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Properties;

import javax.mail.Folder;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.URLName;
import javax.mail.internet.MimeMessage;

import org.apache.commons.io.IOUtils;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.MongoClient;

public class MailingListParser {
final static String FILE_NAME = "C:/mailinglist/downloads/199511.txt";

	public static void main(String[] args) {
				
		try {
			String content = readFile(FILE_NAME, StandardCharsets.UTF_8);
			Session s = Session.getDefaultInstance(new Properties());
			InputStream is = new ByteArrayInputStream(content.getBytes());
			
			StringWriter writer = new StringWriter();
			IOUtils.copy(is, writer, StandardCharsets.UTF_8);
			String theString = writer.toString();
			
			String[] textMessages = theString.split("\nFrom ");
			
			for (int i = 1; i < textMessages.length; i++) {
				textMessages[i] = "From " + textMessages[i];
			}
			
			MimeMessage[] messages = new MimeMessage[textMessages.length];			
			for (int i = 0; i < textMessages.length; i++) {
				messages [i] = new MimeMessage(s, IOUtils.toInputStream(textMessages[i], "UTF-8"));
			}
			
			// mongoDB set up					
			MongoClient mongo = new MongoClient( "localhost" , 27017 );
			DB db = mongo.getDB("mailinglist");
			DBCollection emailCollection = db.getCollection("email");
			
			
			for (int i = 0; i < messages.length; i++) {
				BasicDBObject email = new BasicDBObject();
				email.put("MessageID", messages[i].getSender());
				email.put("MessageID", messages[i].getMessageID());
				email.put("subject", messages[i].getSubject());
				email.put("content", getText(messages[i]));
				email.put("sentDate", messages[i].getSentDate());
				emailCollection.insert(email);
			}
			
			System.out.println("done loading " + FILE_NAME );
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	
	private static boolean textIsHtml = false;

    /**
     * Return the primary text content of the message.
     */
    private static String getText(Part p) throws
                MessagingException, IOException {
        if (p.isMimeType("text/*")) {
            String s = (String)p.getContent();
            textIsHtml = p.isMimeType("text/html");
            return s;
        }

        if (p.isMimeType("multipart/alternative")) {
            // prefer html text over plain text
            Multipart mp = (Multipart)p.getContent();
            String text = null;
            for (int i = 0; i < mp.getCount(); i++) {
                Part bp = mp.getBodyPart(i);
                if (bp.isMimeType("text/plain")) {
                    if (text == null)
                        text = getText(bp);
                    continue;
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
            Multipart mp = (Multipart)p.getContent();
            for (int i = 0; i < mp.getCount(); i++) {
                String s = getText(mp.getBodyPart(i));
                if (s != null)
                    return s;
            }
        }

        return null;
    }
	
	static String readFile(String path, Charset encoding) 
			  throws IOException 
			{
			  byte[] encoded = Files.readAllBytes(Paths.get(path));
			  return encoding.decode(ByteBuffer.wrap(encoded)).toString();
			}
}
