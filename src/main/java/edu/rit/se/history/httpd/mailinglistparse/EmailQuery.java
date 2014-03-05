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
		
		
		//query.getEmail("<6cca3db30911131110i1b93dd4bkce5371d959a54c90@mail.gmail.com>");
		//query.getEmailWithContent("index");

	}
	
	
	private void getEmailWithContent(String text) {
		BasicDBObject query = new BasicDBObject();
		query.put("content",  java.util.regex.Pattern.compile(text));
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
