package edu.rit.se.history.httpd.parse;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;

import org.chaoticbits.devactivity.DBUtil;

public class GitRelease {
	private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(GitRelease.class);
	ResultSet rsRelHist;

	public GitRelease() {

	}

	public void load(DBUtil dbUtil) throws Exception {
		Connection conn = dbUtil.getConnection();
		log.debug("Executing query...");
		
		String query = "UPDATE Gitlog g SET releaseVer = " +
				" (" +
				"SELECT ReleaseVer FROM ReleaseHistory rh " +
				  "            WHERE rh.ReleaseDate = (" +
				  "										SELECT MIN(ReleaseDate) FROM ReleaseHistory rh2 WHERE rh2.ReleaseDate > g.AuthorDate" +
				  "									  )" +
				  ") ";
				 
		
		conn.createStatement().executeUpdate(query);


		/*
		 * String query = "SELECT Commit,AuthorDate, " + "(SELECT ReleaseVer " +
		 * "            FROM ReleaseHistory rh " +
		 * "            WHERE rh.ReleaseDate=(SELECT MIN(ReleaseDate) FROM ReleaseHistory rh2 WHERE rh2.ReleaseDate>AuthorDate) "
		 * + "          ORDER BY rh.ReleaseDate ASC) ReleaseVer " + "    FROM GitLog g ";
		 */

		/*String query = "SELECT commit, authordate FROM Gitlog";
		rsRelHist = conn.createStatement()
				.executeQuery("SELECT * FROM ReleaseHistory ORDER BY releaseDate ASC");

		PreparedStatement ps = conn.prepareStatement(query);
		// ResultSet rs = conn.createStatement().executeQuery(query);

		log.debug("Executing release history query...");

		ResultSet rs = ps.executeQuery();

		String update = "UPDATE gitlog SET releaseVer = ? WHERE Commit=?";
		PreparedStatement psUpdate = conn.prepareStatement(update);
		String releaseVer;
		String commit;
		Timestamp authordate;
		log.debug("looping Gitlog query...");
		int i = 0;
		while (rs.next()) {

			commit = rs.getString("Commit");
			authordate = rs.getTimestamp("authordate");
			log.debug("Getting releaseVer from private function.. Commit: " + commit + " Author date: " + authordate);
			releaseVer = getReleaseVer(authordate);
			log.debug("Loop #: " + (++i) + " Commit: " + commit + " Author date: " + authordate);
			if (releaseVer == null) {
				log.debug("releaseVer is null. skipping update");
				continue;
			}
			log.debug("updating Gitlog...");
			psUpdate.setString(1, releaseVer);
			psUpdate.setString(2, commit);
			psUpdate.addBatch();
		}

		log.debug("Executing batch update...");
		psUpdate.executeBatch();
		conn.close();
	*/
		
	}

	/*private String getReleaseVer(Timestamp authordate) {
		Timestamp releasedate;
		try {
			log.debug("moving cursor to beginning of row...");
			rsRelHist.beforeFirst();
			log.debug("Looping release history...");
			while (rsRelHist.next()) {
				releasedate = rsRelHist.getTimestamp("releasedate");
				log.debug("release date is: " + releasedate);
				if (releasedate.after(authordate)) {
					log.debug("releasedate is after authordate: " + authordate);
					log.debug("returning releasever " + rsRelHist.getString("releaseVer"));
					return rsRelHist.getString("releaseVer");
				}
			}
			log.debug("release date not found...");
		} catch (Exception e) {
			log.debug(e);
		}
		return null;
	}*/

}
