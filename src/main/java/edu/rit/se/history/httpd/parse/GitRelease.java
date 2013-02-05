package edu.rit.se.history.httpd.parse;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import org.chaoticbits.devactivity.DBUtil;

public class GitRelease {
	private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(GitRelease.class);

	public GitRelease() {}

	public void load(DBUtil dbUtil) throws Exception {
		Connection conn = dbUtil.getConnection();
		log.debug("Executing query...");
		String query = "SELECT Commit,AuthorDate, "
				+ "(SELECT ReleaseVer "
				+ "          FROM ReleaseHistory rh "
				+ "          WHERE rh.ReleaseDate=(SELECT MIN(ReleaseDate) FROM ReleaseHistory rh2 WHERE rh2.ReleaseDate>AuthorDate) "
				+ "          ORDER BY rh.ReleaseDate ASC) ReleaseVer " //
				+ "    FROM GitLog g ";
		ResultSet rs = conn.createStatement().executeQuery(query);
		String update = "UPDATE gitlog SET releaseVer=? WHERE Commit=?";
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
