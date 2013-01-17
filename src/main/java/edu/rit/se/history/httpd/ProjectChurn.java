package edu.rit.se.history.httpd;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Properties;

import org.chaoticbits.devactivity.DBUtil;

public class ProjectChurn {
	private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(ProjectChurn.class);

	public void load(DBUtil dbUtil, Properties props) throws Exception {
		log.info("in ProjectChurn.load()...");
		Connection conn = dbUtil.getConnection();

		String query = "SELECT g.filepath, g.commit,  " 
		  + "(SELECT SUM(g5.linesinserted+g5.linesdeleted)  "
		  + "		FROM gitlogfiles g5 INNER JOIN repolog r5 ON g5.commit = r5.commit AND g5.filepath = r5.filepath "
		  + "		WHERE r5.authordate <= r.authordate AND (r.authordate - r5.authordate) <= ? ) as projectChurn "
		  + "FROM gitlogfiles g INNER JOIN repolog r ON g.commit=r.commit AND g.filepath = r.filepath ";
		 
		
		//String query = "select * from projectchurn";
		// 2592000
		long recentPeriod = Long.parseLong(props.getProperty("history.timeline.step"));

		String upQuery = "UPDATE gitlogfiles SET projectChurn = ? WHERE commit = ? AND filepath = ?";
		PreparedStatement ps = conn.prepareStatement(query);

		PreparedStatement psUpdate = conn.prepareStatement(upQuery);

		ps.setLong(1, recentPeriod);
		log.info("Executing project churn query...");
		ResultSet rs = ps.executeQuery();
		log.info("done query...");
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
