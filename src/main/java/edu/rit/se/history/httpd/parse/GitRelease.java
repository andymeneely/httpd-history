package edu.rit.se.history.httpd.parse;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;

import org.chaoticbits.devactivity.DBUtil;

public class GitRelease {
	private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(GitRelease.class);
	private SimpleDateFormat format;

	public GitRelease() {
		format = new SimpleDateFormat("EEE MMM dd HH:mm:ss yyyy +SSSS");
	}

	/**
	 * This parser assumes that you have the following "pretty" format
	 * 
	 * <code>
	 *  git-log --pretty=format:":::%n%H%n%an%n%ae%n%ad%n%P%n%s%n%b" --stat --ignore-space-change > ../httpd-gitlog.txt
	 *  </code>
	 * 
	 * @param dbUtil
	 * @param gitLog
	 * @throws Exception
	 */
	public void load(DBUtil dbUtil) throws Exception {
		Connection conn = dbUtil.getConnection();
		log.debug("Executing query...");
		String query = "SELECT Commit,AuthorDate, "
				+ "(SELECT ReleaseVer "
				+ "            FROM ReleaseHistory rh "
				+ "            WHERE rh.ReleaseDate=(SELECT MIN(ReleaseDate) FROM ReleaseHistory rh2 WHERE rh2.ReleaseDate>AuthorDate) "
				+ "          ORDER BY rh.ReleaseDate ASC) ReleaseVer " + "    FROM GitLog g ";
		ResultSet rs = conn.createStatement().executeQuery(query);
		String update = "UPDATE gitlog SET releaseVer = ? WHERE Commit=?";
		PreparedStatement psUpdate = conn.prepareStatement(update);
		while (rs.next()) {
			psUpdate.setString(1, rs.getString("ReleaseVer"));
			psUpdate.setString(2, rs.getString("Commit"));
			psUpdate.addBatch();
		}
		log.debug("Executing batch update...");
		psUpdate.executeBatch();
		conn.close();
	}

}
