package edu.rit.se.history.httpd.analysis;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import org.chaoticbits.devactivity.DBUtil;

public class RecentChurn {
	private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(RecentChurn.class);

	/*
	 * String query ="SELECT g.filepath, g.commit, " +"(SELECT SUM(linesinserted+linesdeleted) "
	 * +"FROM gitlogfiles g1 INNER JOIN repolog r ON g1.commit = r.commit AND g1.filepath = r.filepath " +
	 * "WHERE g1.filepath = g.filepath AND r.authordate <= r1.authordate AND r1.authordate - r.authordate <= 30) as recentchurn "
	 * +"FROM gitlogfiles g INNER JOIN repolog r1 ON g.commit=r1.commit AND g.filepath = r1.filepath";
	 */

	/*
	 * String query =
	 * "SELECT g.filepath, g.commit, r.authordate, (g.linesinserted+g.linesdeleted) as totalchurn, " +
	 * "(SELECT SUM(g1.linesinserted+g1.linesdeleted) " +
	 * "FROM gitlogfiles g1 INNER JOIN repolog r1 ON g1.commit = r1.commit AND g1.filepath = r1.filepath " +
	 * "WHERE g1.filepath = g.filepath AND r1.authordate <= r.authordate AND (r.authordate - r1.authordate) <= ? ) as recentchurn, "
	 * 
	 * + "(SELECT (SUM(g2.linesDeletedOther)/SUM(g2.linesdeleted) ) * 100 " +
	 * "FROM gitlogfiles g2 INNER JOIN repolog r2 ON g2.commit = r2.commit AND g2.filepath = r2.filepath " +
	 * "WHERE g2.filepath = g.filepath AND r2.authordate <= r.authordate AND (r.authordate - r2.authordate) <= ? ) as RecentPercIntChurn, "
	 * 
	 * + "(SELECT SUM(g3.AuthorsAffected) " +
	 * "FROM gitlogfiles g3 INNER JOIN repolog r3 ON g3.commit = r3.commit AND g3.filepath = r3.filepath " +
	 * "WHERE g3.filepath = g.filepath AND r3.authordate <= r.authordate AND (r.authordate - r3.authordate) <= ? ) as RecentAuthorsAffected, "
	 * 
	 * + "(SELECT SUM(g4.EffectiveAuthors) " +
	 * "FROM gitlogfiles g4 INNER JOIN repolog r4 ON g4.commit = r4.commit AND g4.filepath = r4.filepath " +
	 * "WHERE g4.filepath = g.filepath AND r4.authordate <= r.authordate AND (r.authordate - r4.authordate) <= ? ) as RecentEffectiveAuthors, "
	 * 
	 * + "(SELECT SUM(g5.linesinserted+g5.linesdeleted)  " +
	 * "FROM gitlogfiles g5 INNER JOIN repolog r5 ON g5.commit = r5.commit AND g5.filepath = r5.filepath " +
	 * "WHERE r5.authordate <= r.authordate AND (r.authordate - r5.authordate) <= ? ) as projectChurn "
	 * 
	 * + "FROM gitlogfiles g INNER JOIN repolog r ON g.commit=r.commit AND g.filepath = r.filepath ";
	 */

	public void compute(DBUtil dbUtil, long recentPeriod) throws Exception {
		Connection conn = dbUtil.getConnection();
		String query = "SELECT r0.filepath, r0.commit, r0.authordate, (r0.linesinserted+r0.linesdeleted) as totalchurn, " 
        		// Recent TotalChurn
        		+ "(SELECT SUM(r1.linesinserted+r1.linesdeleted) "
        		+ "FROM Repolog r1 WHERE r1.filepath = r0.filepath AND r1.authordate <= r0.authordate AND (r0.authordate - r1.authordate) <= ? ) as recentchurn,  "
    			// Recent PercIntChurn
       			+ "(SELECT (SUM(r2.linesDeletedOther)/SUM(r2.linesdeleted) ) * 100 "
        		+ "FROM repolog r2 WHERE r2.filepath = r0.filepath AND r2.authordate <= r0.authordate AND (r0.authordate - r2.authordate) <= ? ) as RecentPercIntChurn, "
        		// Recent AuthorsAffected
        		+ "(SELECT SUM(r3.AuthorsAffected) "
        		+ "FROM repolog r3 WHERE r3.filepath = r0.filepath AND r3.authordate <= r0.authordate AND (r0.authordate - r3.authordate) <= ? ) as RecentAuthorsAffected, "
        		// Recent EffectiveAuthors
        		+ "(SELECT SUM(r4.EffectiveAuthors) "
        		+ "FROM repolog r4 WHERE r4.filepath = r0.filepath AND r4.authordate <= r0.authordate AND (r0.authordate - r4.authordate) <= ? ) as RecentEffectiveAuthors, "
        		+ "FROM repolog r0  ";
		
		String upQuery = "UPDATE gitlogfiles SET recentChurn = ?, RecentPercIntChurn = ? WHERE commit = ? AND filepath = ?";
		PreparedStatement ps = conn.prepareStatement(query);
		PreparedStatement psUpdate = conn.prepareStatement(upQuery);
		ps.setLong(1, recentPeriod);
		ps.setLong(2, recentPeriod);
		ps.setLong(3, recentPeriod);
		ps.setLong(4, recentPeriod);
		log.debug("Executing query...");
		ResultSet rs = ps.executeQuery();
		log.debug("Processing results...");
		while (rs.next()) {
			psUpdate.setInt(1, rs.getInt("recentchurn"));
			psUpdate.setDouble(2, rs.getDouble("recentPercIntChurn"));
			psUpdate.setString(3, rs.getString("commit"));
			psUpdate.setString(4, rs.getString("filepath"));
			psUpdate.addBatch();
		}
		log.debug("Executing update...");
		psUpdate.executeBatch();
		conn.close();

	}

}
