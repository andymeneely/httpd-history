package edu.rit.se.history.httpd.mailinglistparse;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.UnknownHostException;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import org.apache.commons.io.FileUtils;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;

public class MailingListLineMatcher {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		MailingListLineMatcher lineMatcher = new MailingListLineMatcher();
		lineMatcher.setUpDB();
		lineMatcher.readFile("C:/mailinglist/VulnIntro.csv");
	}

	private DBCollection emailCollection;

	private void readFile(String path) {

		final String DELIMITER = ",";
		String line = "";

		BufferedReader fileReader;
		try {
			fileReader = new BufferedReader(new FileReader(path));

			// Read the csv file line by line
			while ((line = fileReader.readLine()) != null) {
				// Get all tokens available in line
				String[] tokens = line.split(DELIMITER);
				getEmailID(tokens[0], tokens[1], tokens[2]);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private void getEmailID(String comitId, String filename, String lineNumber) {

		File file = new File("C:/mailinglist/downloads/" + filename + ".txt");
		int startLineNumber = Integer.valueOf(lineNumber);
		try {

			BufferedReader br = new BufferedReader(new FileReader(file));
			String line;

			for (int i = 1; i < startLineNumber; i++) {
				br.readLine();
			}

			while ((line = br.readLine()) != null) {
				if (line.toLowerCase().startsWith("message-id:")) {
					String emailID = line.split(" ")[1];
					System.out.println(comitId + "," + emailID);
					//saveToDB(emailID, comitId);
					break;
				}
			}
			br.close();

		} catch (FileNotFoundException e) {
			System.out.println("File not found");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private void setUpDB() {
		// mongoDB set up
		MongoClient mongo;
		try {
			mongo = new MongoClient("localhost", 27017);
			DB db = mongo.getDB("mailinglist");
			this.emailCollection = db.getCollection("email");
		} catch (UnknownHostException e) {
			System.out.println("Error: There was an while setting up the database :" + e.getMessage());
		}
	}

	private void saveToDB(String emailID, String commitId) {
		BasicDBObject query = new BasicDBObject();
		query.put("messageID", emailID);

		DBObject email = emailCollection.findOne(query);

		if (email != null) {
			email.put("commitID", commitId);
			email.put("mention", true);
			emailCollection.findAndModify(query, email);
		}
	}
}
