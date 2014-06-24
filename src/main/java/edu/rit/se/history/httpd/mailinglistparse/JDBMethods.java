package edu.rit.se.history.httpd.mailinglistparse;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import edu.rit.se.history.httpd.mailinglistparse.MailingListCachedParser.Email;

public class JDBMethods {

	private String ip;
	private String dbname;
	private String username;
	private String password;

	public Connection connect = null;

	public JDBMethods(String ip, String dbname, String username) {
		super();
		this.ip = ip;
		this.dbname = dbname;
		this.username = username;
		//this.password = password;
		setUpConection();
		
		
		//enable these lines to clear the table on every run. 
		/*PreparedStatement ps;
		try {
			ps = this.connect.prepareStatement("TRUNCATE TABLE `email`");
			ps.executeUpdate();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
		
	}

	private void setUpConection() {
		try {
			Class.forName("com.mysql.jdbc.Driver");
			connect = DriverManager.getConnection("jdbc:mysql://" + ip + "/" + dbname + "?" + "user=" + username);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
	
	public boolean insert(Email email) {
		
		try {
			
			String sql = "INSERT INTO `emailTest`(`messageID`, `subject`, `inReplyTo`,`repliesCount`, `directRepliesCount`, `indirectRepliesCount`, `respondersCount`,`responders`) VALUES (?,?,?,?,?,?,?,?)";

			PreparedStatement ps = this.connect.prepareStatement(sql);
			
			//add fields from the email HashTable
			
			ps.setString(1, email.getMessageID());
			ps.setString(2, email.getSubject());
			ps.setString(3, email.getInReplyTo());
			
			Set<String> directReplies = email.getDirectReplies();			
			Set<String> indirectReplies = email.getIndirectReplies();
			
			
			//merge the directReplies and indirectReplies
			Set<String> replies = new HashSet<String>();
			replies.addAll(directReplies);
			replies.addAll(indirectReplies);
			
			
			ps.setInt(4, replies.size());			
			ps.setInt(5, directReplies.size());
			ps.setInt(6, indirectReplies.size());
			
			//count the distinct responders
			ps.setInt(7, email.getResponders().size());
			
			ps.setString(8, email.getResponders().toString());

			int affectedRows = ps.executeUpdate();
			

			if (affectedRows > 0) {
				return true;
			} else {
				return false;
			}

		} catch (SQLException e) {
			System.out.println("Insert Error: " + e.getMessage());
		}

		return false;
	}
}