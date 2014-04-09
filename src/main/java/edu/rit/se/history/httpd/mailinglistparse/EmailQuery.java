package edu.rit.se.history.httpd.mailinglistparse;

import java.net.UnknownHostException;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.WriteResult;

public class EmailQuery {

	private DBCollection emailCollection;

	public static void main(String[] args) {

		EmailQuery query = new EmailQuery();
		query.setUpDB();

		// query.getEmail("<199801092329.SAA09421@devsys.jaguNET.com>");
		// query.getEmailByContent("vulnerability");
		// query.getEmailByCommit("95b2e3783820974f7eaca442296a408052b3f396");
		//query.processDirectReplies();
		query.processReferences();

	}

	private void processDirectReplies() {
		BasicDBObject query = new BasicDBObject("inReplyTo", new BasicDBObject("$exists", true));

		DBCursor emails = emailCollection.find(query);
		if (emails != null) {
			System.out.println("Starting Process...");
			System.out.println("Emails to be processed: " + emails.count());
			for (DBObject email : emails) {

				String inReplyTo = email.get("inReplyTo").toString();
				BasicDBObject repliedQuery = new BasicDBObject();
				repliedQuery.put("messageID", inReplyTo);

				BasicDBObject increment = new BasicDBObject().append("$inc",
						new BasicDBObject().append("directReplies", 1));

				emailCollection.update(repliedQuery, increment);
			}
		}
	}

	private void processReferences() {
		BasicDBObject query = new BasicDBObject("references", new BasicDBObject("$exists", true));

		DBCursor emails = emailCollection.find(query);
		if (emails != null) {
			System.out.println("Starting Process...");
			System.out.println("Emails to be processed: " + emails.count());
			for (DBObject email : emails) {

				BasicDBList references = (BasicDBList) email.get("references");

				for (Object reference : references) {
					String emailId = (String) reference;

					BasicDBObject referenceQuery = new BasicDBObject();
					referenceQuery.put("messageID", emailId);

					BasicDBObject increment = new BasicDBObject().append("$inc",
							new BasicDBObject().append("referenceCount", 1));

					emailCollection.update(referenceQuery, increment);
				}
			}
		}
	}

	private void getEmailByCommit(String commitID) {
		BasicDBObject query = new BasicDBObject();
		query.put("commitID", commitID);

		DBCursor emails = emailCollection.find(query);

		if (emails != null) {
			System.out.println(emails.count());
			for (DBObject email : emails) {
				System.out.println(email);
			}
		}
	}

	private void getEmailByContent(String content) {
		BasicDBObject query = new BasicDBObject();
		query.put("content", java.util.regex.Pattern.compile(content));
		DBCursor emails = emailCollection.find(query);

		if (emails != null) {
			System.out.println(emails.count());
			for (DBObject email : emails) {
				System.out.println(email.get("content"));
			}
		}

	}

	private void getEmail(String emailID) {
		BasicDBObject query = new BasicDBObject();
		query.put("messageID", emailID);

		DBObject email = emailCollection.findOne(query);

		if (email != null) {
			System.out.println(email);
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

}
