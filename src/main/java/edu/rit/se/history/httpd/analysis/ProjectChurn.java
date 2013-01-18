package edu.rit.se.history.httpd.analysis;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import org.chaoticbits.devactivity.DBUtil;

public class ProjectChurn {
	private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(ProjectChurn.class);

	public void compute(DBUtil dbUtil, long recentPeriod) throws Exception {
		Connection conn = dbUtil.getConnection();

		String query = "SELECT g.filepath, g.commit,  " 
		  + "(SELECT SUM(g5.linesinserted+g5.linesdeleted)  "
		  + "		FROM gitlogfiles g5 INNER JOIN repolog r5 ON g5.commit = r5.commit AND g5.filepath = r5.filepath "
		  + "		WHERE r5.authordate <= r.authordate AND (r.authordate - r5.authordate) <= ? ) as projectChurn "
		  + "FROM gitlogfiles g INNER JOIN repolog r ON g.commit=r.commit AND g.filepath = r.filepath ";
		PreparedStatement ps = conn.prepareStatement(query);
		log.debug("Executing query...");
		ResultSet rs = ps.executeQuery();

		String upQuery = "UPDATE gitlogfiles SET projectChurn = ? WHERE commit = ? AND filepath = ?";
		PreparedStatement psUpdate = conn.prepareStatement(upQuery);
		ps.setLong(1, recentPeriod);
		
		log.debug("Processing results...");
		while (rs.next()) {
			psUpdate.setInt(1, rs.getInt("projectchurn"));
			psUpdate.setString(2, rs.getString("commit"));
			psUpdate.setString(3, rs.getString("filepath"));
			psUpdate.addBatch();
		}
		
		log.info("Executing project churn batch update...");
		psUpdate.executeBatch();
		conn.close();
	}

}
