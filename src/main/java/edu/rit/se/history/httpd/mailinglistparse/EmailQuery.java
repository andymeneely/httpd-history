package edu.rit.se.history.httpd.mailinglistparse;

import java.net.UnknownHostException;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;

public class EmailQuery {
	
	private DBCollection emailCollection;

	public static void main(String[] args) {
		
		EmailQuery query = new EmailQuery();
		query.setUpDB();	
		
		
		query.getEmail("<377960000.1036532431@cite.ics.uci.edu>");
		//query.getEmailByContent("index");
		//query.getEmailByCommit("95b2e3783820974f7eaca442296a408052b3f396");

	}
	
	private void getEmailByCommit(String commitID){
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
		query.put("content",  java.util.regex.Pattern.compile(content));
		DBCursor emails = emailCollection.find(query);
		
		
		if (emails != null) {
			System.out.println(emails.count());
			for (DBObject email : emails) {
				System.out.println(email);
			}
		}
		
	}

	private void getEmail(String emailID){
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
