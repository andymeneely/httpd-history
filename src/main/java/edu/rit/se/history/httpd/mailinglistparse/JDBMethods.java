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
		// this.password = password;
		setUpConection();

		// CREATE TABLE IF NOT EXISTS
		PreparedStatement ps;
		try {

			String sql = " CREATE TABLE IF NOT EXISTS `email` ( " + "  `messageID` varchar(500) NOT NULL, "
					+ "  `subject` text, " + "  `inReplyTo` varchar(500) DEFAULT NULL, "
					+ "  `repliesCount` int(9) NOT NULL DEFAULT '0', "
					+ "  `directRepliesCount` int(9) NOT NULL DEFAULT '0', "
					+ "  `indirectRepliesCount` int(9) NOT NULL DEFAULT '0', "
					+ "  `respondersCount` int(9) NOT NULL DEFAULT '0', " + "  `responders` text, "
					+ "  `VCC` tinyint(1) DEFAULT '0', " + "  `preCommit` tinyint(1) DEFAULT NULL, "
					+ "  `discussion` varchar(8) DEFAULT NULL, " + " `security` tinyint(1) DEFAULT NULL, "
					+ " `securityCVE` tinyint(1) DEFAULT NULL, " + " `securityGeneral` tinyint(1) DEFAULT NULL, "
					+ " PRIMARY KEY (`messageID`), " + " KEY `messageID` (`messageID`) "
					+ " ) ENGINE=InnoDB DEFAULT CHARSET=latin1; ";

			ps = this.connect.prepareStatement(sql);
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

	public boolean updateWithCSV(String messageId, boolean vcc, boolean preCommit, String discussion, boolean security,
			boolean securityCVE, boolean securityGeneral) {

		try {

			String sql = "UPDATE `email` SET `VCC`=?,`preCommit`=?,`discussion`=?,`security`=?,`securityCVE`=?,`securityGeneral`=? WHERE `messageID`=?";

			PreparedStatement ps = this.connect.prepareStatement(sql);

			ps.setBoolean(1, vcc);
			ps.setBoolean(2, preCommit);
			ps.setString(3, discussion);
			ps.setBoolean(4, security);
			ps.setBoolean(5, securityCVE);
			ps.setBoolean(6, securityGeneral);
			ps.setString(7, messageId);

			int affectedRows = ps.executeUpdate();

			if (affectedRows > 0) {
				return true;
			} else {
				System.out.println("Error with messageId: " + messageId);
				return false;
			}

		} catch (SQLException e) {
			System.out.println("Insert Error: " + e.getMessage());
		}

		return false;
	}

	public boolean insert(Email email) {

		try {

			String sql = "INSERT INTO `email`(`messageID`, `subject`, `inReplyTo`,`repliesCount`, `directRepliesCount`, `indirectRepliesCount`, `respondersCount`,`responders`) VALUES (?,?,?,?,?,?,?,?)";

			PreparedStatement ps = this.connect.prepareStatement(sql);

			// add fields from the email HashTable

			ps.setString(1, email.getMessageID());
			ps.setString(2, email.getSubject());
			ps.setString(3, email.getInReplyTo());

			HashSet<Email> directReplies = email.getDirectReplies();
			HashSet<Email> indirectReplies = email.getIndirectReplies();

			// merge the directReplies and indirectReplies
			HashSet<Email> replies = new HashSet<Email>();
			replies.addAll(directReplies);
			replies.addAll(indirectReplies);

			ps.setInt(4, replies.size());
			ps.setInt(5, directReplies.size());
			ps.setInt(6, indirectReplies.size());

			// count the distinct responders
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