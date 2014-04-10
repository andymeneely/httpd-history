package edu.rit.se.history.httpd.mailinglistparse;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.MongoClient;

public class MakeThreads {

	public static void main(String[] args) {
		// TODO Auto-generated method stub

		MongoClient mongo;
		DBCollection OrginalCollection;
		DBCursor cursor1;
		DBCollection emailCollection;
		DBCursor cursor2 = null;
		ArrayList<String> messageIDS = new ArrayList<String>();
		try {
			mongo = new MongoClient("localhost", 27017);
			DB db = mongo.getDB("mailinglist");
			emailCollection = db.getCollection("OrignalEmail");
			BasicDBObject query = new BasicDBObject("messageID", new BasicDBObject("$ne", null)); 
			BasicDBObject fields = new BasicDBObject("messageID", true).append("_id", false);
			cursor1 = emailCollection.find(query, fields);

			try {
				while (cursor1.hasNext()) {
					if(//!cursor1.next().get("messageID").equals(null) || 
							!cursor1.next().get("messageID").toString().equalsIgnoreCase("<>")
							)
					{
						messageIDS.add(cursor1.next().get("messageID").toString());
						//System.out.println(cursor1.next().get("messageID"));
					}
				}
			//for(int i=0; i < messageIDS.size(); i++)
				//System.out.println(messageIDS.get(i));
			
			System.out.println(messageIDS.size());
				// db.email.find({$or:[{inReplyTo:"<m3ellaavmh.fsf@rdu163-40-092.nc.rr.com>"},{References:{$exists:true}}]}).pretty();

				OrginalCollection = db.getCollection("email");
				
				for (String string : messageIDS) {
					//System.out.println(string);
					BasicDBObject orQuery = new BasicDBObject();
					List<BasicDBObject> obj = new ArrayList<BasicDBObject>();
					obj.add(new BasicDBObject("inReplyTo", string));
					obj.add(new BasicDBObject("References", string));
					
					
					orQuery.put("$or", obj);
					
					cursor2 = OrginalCollection.find(orQuery);
					
					while (cursor2.hasNext()) {
							System.out.println(cursor2.next());
					}

				}

			} finally {
				cursor1.close();
				cursor2.close();
			}
		} catch (UnknownHostException e) {
			System.out.println("Error: There was an while setting up the database :" + e.getMessage());
		}

	}

}
