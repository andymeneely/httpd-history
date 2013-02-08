package edu.rit.se.history.httpd.analysis;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import org.chaoticbits.devactivity.DBUtil;

public class RecentPIC {
	private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(RecentPIC.class);

	public void compute(DBUtil dbUtil, long recentPeriod) throws Exception {
		Connection conn = dbUtil.getConnection();
		String query = "SELECT r0.filepath, r0.commit, r0.authordate, (r0.linesinserted+r0.linesdeleted) as totalchurn, " 
     			// Recent PercIntChurn
       			+ "(SELECT SUM(r2.linesDeletedOther)/SUM(r2.linesdeleted) "
        		+ "FROM repolog r2 WHERE r2.filepath = r0.filepath AND r2.authordate <= r0.authordate AND DATEDIFF(r0.authordate, r2.authordate) <= ? ) as RecentPercIntChurn "
        		+ "FROM repolog r0  ";
		
		String upQuery = "UPDATE gitlogfiles SET RecentPercIntChurn = ? WHERE commit = ? AND filepath = ?";
		PreparedStatement ps = conn.prepareStatement(query);
		PreparedStatement psUpdate = conn.prepareStatement(upQuery);
		ps.setLong(1, recentPeriod);
		
		log.debug("Executing query...");
		ResultSet rs = ps.executeQuery();
		log.debug("Processing results...");
		while (rs.next()) {
			psUpdate.setDouble(1, rs.getDouble("recentPercIntChurn"));
			psUpdate.setString(2, rs.getString("commit"));
			psUpdate.setString(3, rs.getString("filepath"));
			psUpdate.addBatch();
		}
		log.debug("Executing update...");
		psUpdate.executeBatch();
		conn.close();

	}

}
