package edu.rit.se.history.httpd.parse;

import java.sql.Connection;

import org.chaoticbits.devactivity.DBUtil;

public class GitRelease {
	private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(GitRelease.class);
	
	public GitRelease() {

	}

	public void load(DBUtil dbUtil) throws Exception {
		Connection conn = dbUtil.getConnection();
		
		String query = "UPDATE Gitlog g SET releaseVer = " +
				" (" +
				"SELECT ReleaseVer FROM ReleaseHistory rh " +
				  "            WHERE rh.ReleaseDate = (" +
				  "										SELECT MIN(ReleaseDate) FROM ReleaseHistory rh2 WHERE rh2.ReleaseDate > g.AuthorDate" +
				  "									  )" +
				  ") ";
				 
		log.debug("Executing Gitlog Major Release update...");
		conn.createStatement().executeUpdate(query);		
	}
	
}
