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
		
		query.getEmailBySubject("Logging extensions to NCSA httpd 1.3");
		//query.getEmail("<9503011456.AA24767@volterra>");
		// query.getEmailByContent("vulnerability");
		// query.getEmailByCommit("95b2e3783820974f7eaca442296a408052b3f396");
		// query.processReplies();

	}

	private void processReplies() {
		// this.processDirectReplies();
		 this.processIndirectReplies();
		// this.processReferences();
		// this.processIndirectReferences();
	}

	private void processDirectReplies() {
		BasicDBObject query = new BasicDBObject("inReplyTo", new BasicDBObject("$exists", true));

		DBCursor emails = emailCollection.find(query);
		if (emails != null) {
			System.out.println("Starting Process...");
			System.out.println("Emails to be processed: " + emails.count());
			for (DBObject email : emails) {

				//String messageID = (String) email.get("messageID");
				BasicDBList from = (BasicDBList) email.get("from");
				String inReplyTo = email.get("inReplyTo").toString();

				BasicDBObject repliedQuery = new BasicDBObject();
				repliedQuery.put("messageID", inReplyTo);

				BasicDBObject update = new BasicDBObject();
				update.put("$inc", new BasicDBObject().append("repliesCount", 1));
				
				
				//update.put("$addToSet", new BasicDBObject().append("repliedIn", messageID));
				
				BasicDBObject responders = new BasicDBObject().append("$each", from);
				update.put("$addToSet", new BasicDBObject().append("responders", responders));

				System.out.println(emailCollection.update(repliedQuery, update));
			}
		}
	}

	private void processIndirectReplies() {
		BasicDBObject query = new BasicDBObject("responders", new BasicDBObject("$exists", true));
		query.append("inReplyTo", new BasicDBObject("$exists", true));

		DBCursor emails = emailCollection.find(query);
		if (emails != null) {
			System.out.println("Starting Process...");
			System.out.println("Emails to be processed: " + emails.count());
			for (DBObject email : emails) {
				
				
				String inReplyTo = email.get("inReplyTo").toString();
				int repliesCount = (Integer) email.get("repliesCount");
				//BasicDBList repliedIn = (BasicDBList) email.get("repliedIn");
				BasicDBList responders = (BasicDBList) email.get("responders");

				BasicDBObject repliedQuery = new BasicDBObject();
				repliedQuery.put("messageID", inReplyTo);

				BasicDBObject update = new BasicDBObject();
				update.append("$inc", new BasicDBObject().append("indirectRepliesCount", repliesCount));
				
				//BasicDBObject indirectRepliedIn = new BasicDBObject().append("$each", repliedIn);
				//update.append("$addToSet", new BasicDBObject().append("$indirectRepliedIn", indirectRepliedIn));
				
				BasicDBObject indirectRepliedBy = new BasicDBObject().append("$each", responders);
				update.append("$addToSet", new BasicDBObject().append("indirectResponders", indirectRepliedBy));

				System.out.println(emailCollection.update(repliedQuery, update));
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

				//String messageID = (String) email.get("messageID");
				BasicDBList from = (BasicDBList) email.get("from");
				BasicDBList references = (BasicDBList) email.get("references");

				for (Object reference : references) {
					String emailId = (String) reference;

					BasicDBObject referenceQuery = new BasicDBObject();
					referenceQuery.put("messageID", emailId);

					BasicDBObject update = new BasicDBObject();
					update.append("$inc", new BasicDBObject().append("referenceCount", 1));
					//update.append("$addToSet", new BasicDBObject().append("referencedIn", messageID));
					
					BasicDBObject referencedBy = new BasicDBObject().append("$each", from);
					update.put("$addToSet", new BasicDBObject().append("referencedBy", referencedBy));
					
					emailCollection.update(referenceQuery, update);
				}
			}
		}
	}

	private void processIndirectReferences() {
		BasicDBObject query = new BasicDBObject("references", new BasicDBObject("$exists", true));

		DBCursor emails = emailCollection.find(query);
		if (emails != null) {
			System.out.println("Starting Process...");
			System.out.println("Emails to be processed: " + emails.count());
			for (DBObject email : emails) {

				BasicDBList references = (BasicDBList) email.get("references");
				int referenceCount = (Integer) email.get("referenceCount");

				for (Object reference : references) {
					String emailId = (String) reference;

					BasicDBObject referenceQuery = new BasicDBObject();
					referenceQuery.put("messageID", emailId);

					BasicDBObject increment = new BasicDBObject().append("$inc",
							new BasicDBObject().append("indirectReferenceCount", referenceCount));

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
	
	private void getEmailBySubject(String subject) {
		BasicDBObject query = new BasicDBObject();
		query.put("subject", java.util.regex.Pattern.compile(subject));
		DBCursor emails = emailCollection.find(query);

		if (emails != null) {
			System.out.println(emails.count());
			for (DBObject email : emails) {
				System.out.println(email);
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
