package edu.rit.se.history.httpd.analysis;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import org.chaoticbits.devactivity.DBUtil;

public class RecentChurn {
	private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(RecentChurn.class);

	public void compute(DBUtil dbUtil, long recentPeriod) throws Exception {
		Connection conn = dbUtil.getConnection();
		/*String query = "SELECT r0.filepath, r0.commit, r0.authordate, (r0.linesinserted+r0.linesdeleted) as totalchurn, " 
        		// Recent TotalChurn
        		+ "(SELECT SUM(r1.linesinserted+r1.linesdeleted) "
        		+ "FROM Repolog r1 WHERE r1.filepath = r0.filepath AND r1.authordate <= r0.authordate AND (r0.authordate - r1.authordate) <= ? ) as recentchurn,  "
    			// Recent PercIntChurn
       			+ "(SELECT (SUM(r2.linesDeletedOther)/SUM(r2.linesdeleted) ) * 100 "
        		+ "FROM repolog r2 WHERE r2.filepath = r0.filepath AND r2.authordate <= r0.authordate AND (r0.authordate - r2.authordate) <= ? ) as RecentPercIntChurn, "
        		// Recent AuthorsAffected
        		+ "(SELECT COUNT(*) FROM gitchurnauthorsaffected a INNER JOIN repolog _r ON a.commit = _r.commit AND a.filepath = _r.filepath "
        	    + "WHERE a.commit = r0.commit AND a.filepath = r0.filepath  "
        	    + "AND _r.authordate <= r0.authordate AND (r0.authordate - _r.authordate) <= ?  ) AS RecentAuthorsAffected, "        	
        		// Recent EffectiveAuthors
        	    + "(SELECT COUNT(*) FROM gitchurneffectiveauthors a INNER JOIN repolog _r ON a.commit = _r.commit AND a.filepath = _r.filepath "
        	    + "WHERE a.commit = r0.commit AND a.filepath = r0.filepath  "
        	    + "AND _r.authordate <= r0.authordate AND (r0.authordate - _r.authordate) <= ?  ) AS RecentEffectiveAuthors "  
        		+ "FROM repolog r0  "*/;
        
        //seperated the different subqueries to individual queries for performance
        		// Recent TotalChurn
		String query = "SELECT r0.filepath, r0.commit, r0.authordate, (r0.linesinserted+r0.linesdeleted) as totalchurn, "        		
        		+ "(SELECT SUM(r1.linesinserted+r1.linesdeleted) "
        		+ "FROM Repolog r1 WHERE r1.filepath = r0.filepath AND r1.authordate <= r0.authordate AND DATEDIFF(r0.authordate, r1.authordate) <= ? ) as recentchurn  "
        		+ "FROM repolog r0  ";
		//String upQuery = "UPDATE gitlogfiles SET recentChurn = ?, RecentPercIntChurn = ? authorsAffected = ? effectiveAuthors = ? WHERE commit = ? AND filepath = ?";
		String upQuery = "UPDATE gitlogfiles SET recentChurn = ? WHERE commit = ? AND filepath = ?";
		PreparedStatement ps = conn.prepareStatement(query);
		PreparedStatement psUpdate = conn.prepareStatement(upQuery);
		ps.setLong(1, recentPeriod);
		//ps.setLong(2, recentPeriod);
		//ps.setLong(3, recentPeriod);
		//ps.setLong(4, recentPeriod);
		log.debug("Executing query...");
		ResultSet rs = ps.executeQuery();
		log.debug("Processing results...");
		while (rs.next()) {
			psUpdate.setInt(1, rs.getInt("recentchurn"));
			//psUpdate.setDouble(2, rs.getDouble("recentPercIntChurn"));
			//psUpdate.setInt(3, rs.getInt("recentAuthorsAffected"));
			//psUpdate.setInt(4, rs.getInt("recentEffectiveAuthors"));
			psUpdate.setString(2, rs.getString("commit"));
			psUpdate.setString(3, rs.getString("filepath"));
			psUpdate.addBatch();
		}
		log.debug("Executing update...");
		psUpdate.executeBatch();
		conn.close();

	}

}
