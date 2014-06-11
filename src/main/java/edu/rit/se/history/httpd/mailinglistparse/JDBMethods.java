package edu.rit.se.history.httpd.mailinglistparse;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Set;

public class JDBMethods {

	private String ip;
	private String dbname;
	private String username;
	private String password;

	private Connection connect = null;

	public JDBMethods(String ip, String dbname, String username) {
		super();
		this.ip = ip;
		this.dbname = dbname;
		this.username = username;
		//this.password = password;
		setUpConection();
		
		PreparedStatement ps;
		try {
			ps = this.connect.prepareStatement("TRUNCATE TABLE `email`");
			ps.executeUpdate();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	private void setUpConection() {
		try {
			Class.forName("com.mysql.jdbc.Driver");
			connect = DriverManager.getConnection("jdbc:mysql://" + ip + "/" + dbname + "?" + "user=" + username);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
	
	public boolean insert(HashMap<String, Object> email) {
		
		try {
			
			String sql = "INSERT INTO `email`(`messageID`, `subject`, `inReplyTo`, `directRepliesCount`, `indirectRepliesCount`, `respondersCount`,`responders`) VALUES (?,?,?,?,?,?,?)";

			PreparedStatement ps = this.connect.prepareStatement(sql);
			
			ps.setString(1, (String) email.get("messageID"));
			ps.setString(2, (String) email.get("subject"));
			ps.setString(3, (String) email.get("inReplyTo"));
			
			Set<String> directReplies = (Set<String>) email.get("directReplies");
			ps.setInt(4, directReplies.size());
			
			Set<String> indirectReplies = (Set<String>) email.get("indirectReplies");
			ps.setInt(5, indirectReplies.size());
			
			Set<String> responders = (Set<String>) email.get("responders");
			ps.setInt(6, responders.size());
			
			ps.setString(7, ((Set<String>) email.get("responders")).toString());

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