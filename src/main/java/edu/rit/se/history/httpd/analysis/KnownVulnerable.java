package edu.rit.se.history.httpd.analysis;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import org.chaoticbits.devactivity.DBUtil;

/**
 * 
 * Update the table to determine if the file was known to have been vulnerable in the past
 * 
 * @author Andy Meneely
 * 
 */
public class KnownVulnerable {
	private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(KnownVulnerable.class);

	public void compute(DBUtil dbUtil) throws Exception {
		Connection conn = dbUtil.getConnection();
		String query = "SELECT r.Commit, r.Filepath, " //
				+ "(SELECT IF(COUNT(DISTINCT CVE) > 0, \"Yes\",\"No\") "//
				+ "FROM CVEToGit c2g INNER JOIN RepoLog _r ON (c2g.CommitFixed = _r.commit) " //
				+ "WHERE r.Filepath=_r.Filepath "//
				+ "AND _r.AuthorDate < r.AuthorDate) as KnownPastVulnerable "//
				+ "FROM RepoLog r";
		PreparedStatement psQuery = conn.prepareStatement(query);
		log.debug("Executing query...");
		log.debug("\tQuery: " + query);
		ResultSet rs = psQuery.executeQuery();
		String upQuery = "UPDATE GitLogFiles SET KnownPastVulnerable = ? WHERE Commit = ? AND Filepath= ?";
		PreparedStatement psUpdate = conn.prepareStatement(upQuery);
		log.debug("Processing results...");
		while (rs.next()) {
			psUpdate.setString(1, rs.getString("KnownPastVulnerable"));
			psUpdate.setString(2, rs.getString("Commit"));
			psUpdate.setString(3, rs.getString("Filepath"));
			psUpdate.addBatch();
		}
		log.debug("Executing update...");
		psUpdate.executeBatch();
		conn.close();
	}
}
