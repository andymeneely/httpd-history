package edu.rit.se.history.httpd.analysis;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import org.chaoticbits.devactivity.DBUtil;

public class RecentAuthorsAffected {
	private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(RecentAuthorsAffected.class);

	public void compute(DBUtil dbUtil, long recentPeriod) throws Exception {
		Connection conn = dbUtil.getConnection();
		String query = "SELECT r.filepath, r.commit,  "      	        		
			    + "(SELECT COUNT(DISTINCT authoraffected) FROM gitchurnauthorsaffected a " 
			    + "INNER JOIN repolog _r ON a.commit = _r.commit "
			    + "WHERE _r.authordate <= r.authordate AND DATEDIFF(r.authordate, _r.authordate) <= ? "
			    + "GROUP BY a.filepath HAVING a.filepath = r.filepath ) AS RecentAuthorsAffected "
			+ "FROM Repolog r";
		
		String upQuery = "UPDATE GitLogFiles SET RecentAuthorsAffected = ? WHERE commit = ? AND filepath = ?";
		PreparedStatement ps = conn.prepareStatement(query);
		PreparedStatement psUpdate = conn.prepareStatement(upQuery);
		ps.setLong(1, recentPeriod);
		log.debug("Executing query...");
		ResultSet rs = ps.executeQuery();
		log.debug("Processing results...");
		while (rs.next()) {
			psUpdate.setInt(1, rs.getInt("recentAuthorsAffected"));
			psUpdate.setString(2, rs.getString("commit"));
			psUpdate.setString(3, rs.getString("filepath"));
			psUpdate.addBatch();
		}
		log.debug("Executing update...");
		psUpdate.executeBatch();
		conn.close();

	}

}
