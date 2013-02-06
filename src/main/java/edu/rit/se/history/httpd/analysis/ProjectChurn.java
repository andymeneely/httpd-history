package edu.rit.se.history.httpd.analysis;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import org.chaoticbits.devactivity.DBUtil;

public class ProjectChurn {
	private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(ProjectChurn.class);

	public void compute(DBUtil dbUtil, long recentPeriod) throws Exception {
		Connection conn = dbUtil.getConnection();

		String query = "SELECT r0.filepath, r0.commit,  " 
		  // ProjectChurn
        		+ "(SELECT SUM(r1.linesinserted+r1.linesdeleted)  "
        		+ "FROM repolog r1 WHERE r1.authordate <= r0.authordate AND DATEDIFF(r0.authordate, r1.authordate) <= ? ) as projectChurn "
				//
		  + "FROM repolog r0 ";
		
		PreparedStatement ps = conn.prepareStatement(query);
		log.debug("Executing query...");
		ps.setLong(1, recentPeriod);
		ResultSet rs = ps.executeQuery();

		String upQuery = "UPDATE gitlogfiles SET projectChurn = ? WHERE commit = ? AND filepath = ?";
		PreparedStatement psUpdate = conn.prepareStatement(upQuery);
				
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
