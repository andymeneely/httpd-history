package edu.rit.se.history.httpd.mailinglistparse;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashMap;

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
			String sql = "INSERT INTO `email`(`messageID`, `subject`, `inReplyTo`, `directRepliesCount`, `directRepliesCount`, `respondersCount`) VALUES (?,?,?,?,?,?)";

			PreparedStatement ps = this.connect.prepareStatement(sql);
			
			ps.setString(1, (String) email.get("messageID"));
			ps.setString(2, (String) email.get("subject"));
			ps.setString(3, (String) email.get("inReplyTo"));
			ps.setInt(4, (Integer) email.get("directRepliesCount"));
			ps.setInt(5, (Integer)email.get("directRepliesCount"));
			ps.setInt(6, (Integer)email.get("respondersCount"));

			int affectedRows = ps.executeUpdate();

			if (affectedRows > 0) {
				return true;
			} else {
				return false;
			}

		} catch (Exception e) {
			System.out.println("Insert Error: " + e.getMessage());
		}

		return false;
	}
}