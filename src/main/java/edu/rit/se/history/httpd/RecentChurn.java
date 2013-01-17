package edu.rit.se.history.httpd;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Properties;

import org.chaoticbits.devactivity.DBUtil;

public class RecentChurn {
	private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(RecentChurn.class);

	public void load(DBUtil dbUtil, Properties props) throws Exception {
		log.info("in RecentChurn.load()...");
		Connection conn = dbUtil.getConnection();

		/*
		 * String query ="SELECT g.filepath, g.commit, " +"(SELECT SUM(linesinserted+linesdeleted) "
		 * +"FROM gitlogfiles g1 INNER JOIN repolog r ON g1.commit = r.commit AND g1.filepath = r.filepath "
		 * +
		 * "WHERE g1.filepath = g.filepath AND r.authordate <= r1.authordate AND r1.authordate - r.authordate <= 30) as recentchurn "
		 * +"FROM gitlogfiles g INNER JOIN repolog r1 ON g.commit=r1.commit AND g.filepath = r1.filepath";
		 */

		/*
		String query = "SELECT g.filepath, g.commit, r.authordate, (g.linesinserted+g.linesdeleted) as totalchurn, "
		        + "(SELECT SUM(g1.linesinserted+g1.linesdeleted) "
		        + "FROM gitlogfiles g1 INNER JOIN repolog r1 ON g1.commit = r1.commit AND g1.filepath = r1.filepath "
		        + "WHERE g1.filepath = g.filepath AND r1.authordate <= r.authordate AND (r.authordate - r1.authordate) <= ? ) as recentchurn, "
		        
		        + "(SELECT (SUM(g2.linesDeletedOther)/SUM(g2.linesdeleted) ) * 100 "
		        + "FROM gitlogfiles g2 INNER JOIN repolog r2 ON g2.commit = r2.commit AND g2.filepath = r2.filepath "
		        + "WHERE g2.filepath = g.filepath AND r2.authordate <= r.authordate AND (r.authordate - r2.authordate) <= ? ) as RecentPercIntChurn, "
		        
		        + "(SELECT SUM(g3.AuthorsAffected) "
		        + "FROM gitlogfiles g3 INNER JOIN repolog r3 ON g3.commit = r3.commit AND g3.filepath = r3.filepath "
		        + "WHERE g3.filepath = g.filepath AND r3.authordate <= r.authordate AND (r.authordate - r3.authordate) <= ? ) as RecentAuthorsAffected, "
		        
		        + "(SELECT SUM(g4.EffectiveAuthors) "
		        + "FROM gitlogfiles g4 INNER JOIN repolog r4 ON g4.commit = r4.commit AND g4.filepath = r4.filepath "
		        + "WHERE g4.filepath = g.filepath AND r4.authordate <= r.authordate AND (r.authordate - r4.authordate) <= ? ) as RecentEffectiveAuthors, "
		        
		        + "(SELECT SUM(g5.linesinserted+g5.linesdeleted)  "
		        + "FROM gitlogfiles g5 INNER JOIN repolog r5 ON g5.commit = r5.commit AND g5.filepath = r5.filepath "
		        + "WHERE r5.authordate <= r.authordate AND (r.authordate - r5.authordate) <= ? ) as projectChurn "

		+ "FROM gitlogfiles g INNER JOIN repolog r ON g.commit=r.commit AND g.filepath = r.filepath ";
		*/
		String query = "SELECT g.filepath, g.commit, r.authordate, (g.linesinserted+g.linesdeleted) as totalchurn, "
		        + "(SELECT SUM(g1.linesinserted+g1.linesdeleted) "
		        + "FROM gitlogfiles g1 INNER JOIN repolog r1 ON g1.commit = r1.commit AND g1.filepath = r1.filepath "
		        + "WHERE g1.filepath = g.filepath AND r1.authordate <= r.authordate AND (r.authordate - r1.authordate) <= ? ) as recentchurn, "
		        
		        + "(SELECT (SUM(g2.linesDeletedOther)/SUM(g2.linesdeleted) ) * 100 "
		        + "FROM gitlogfiles g2 INNER JOIN repolog r2 ON g2.commit = r2.commit AND g2.filepath = r2.filepath "
		        + "WHERE g2.filepath = g.filepath AND r2.authordate <= r.authordate AND (r.authordate - r2.authordate) <= ? ) as RecentPercIntChurn, "
		        
		        + "(SELECT SUM(g3.AuthorsAffected) "
		        + "FROM gitlogfiles g3 INNER JOIN repolog r3 ON g3.commit = r3.commit AND g3.filepath = r3.filepath "
		        + "WHERE g3.filepath = g.filepath AND r3.authordate <= r.authordate AND (r.authordate - r3.authordate) <= ? ) as RecentAuthorsAffected, "
		        
		        + "(SELECT SUM(g4.EffectiveAuthors) "
		        + "FROM gitlogfiles g4 INNER JOIN repolog r4 ON g4.commit = r4.commit AND g4.filepath = r4.filepath "
		        + "WHERE g4.filepath = g.filepath AND r4.authordate <= r.authordate AND (r.authordate - r4.authordate) <= ? ) as RecentEffectiveAuthors "

		+ "FROM gitlogfiles g INNER JOIN repolog r ON g.commit=r.commit AND g.filepath = r.filepath ";
		
		// 2592000
		long recentPeriod = Long.parseLong(props.getProperty("history.timeline.step"));

		 
		String upQuery = "UPDATE gitlogfiles SET recentChurn = ?, RecentPercIntChurn = ? WHERE commit = ? AND filepath = ?";
		log.info("creating prepared statement 1...");
		PreparedStatement ps = conn.prepareStatement(query);
		log.info("creating prepared statement 2...");
		PreparedStatement psUpdate = conn.prepareStatement(upQuery);
		log.info("setting parameters...");
		ps.setLong(1, recentPeriod);
		ps.setLong(2, recentPeriod);
		ps.setLong(3, recentPeriod);
		ps.setLong(4, recentPeriod);
		//ps.setLong(5, recentPeriod);
		
		log.info("Executing recent churn query...");
		ResultSet rs = ps.executeQuery();
		log.info("done executing recent churn query...");
		while (rs.next()) {
			psUpdate.setInt(1, rs.getInt("recentchurn"));
			//psUpdate.setInt(2, rs.getInt("projectchurn"));
			psUpdate.setDouble(2, rs.getDouble("recentPercIntChurn"));
			psUpdate.setString(3, rs.getString("commit"));
			psUpdate.setString(4, rs.getString("filepath"));

			psUpdate.addBatch();

		}

		log.info("Executing recent churn batch update...");
		psUpdate.executeBatch();
		conn.close();

	}

}
