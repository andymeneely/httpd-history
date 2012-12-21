package edu.rit.se.history.httpd;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import org.chaoticbits.devactivity.DBUtil;


public class RecentChurn {
	private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(RecentChurn.class);

	public void load(DBUtil dbUtil) throws Exception{
		Connection conn = dbUtil.getConnection();
		
		String query ="SELECT g.filepath, g.commit, "
                             +"(SELECT SUM(linesinserted+linesdeleted) "
                                  +"FROM gitlogfiles g1 INNER JOIN repolog r ON g1.commit = r.commit AND g1.filepath = r.filepath "
                                  +"WHERE g1.filepath = g.filepath AND r.authordate <= r1.authordate AND r1.authordate - r.authordate <= 30) as recentchurn " 
                      +"FROM gitlogfiles g INNER JOIN repolog r1 ON g.commit=r1.commit AND g.filepath = r1.filepath";
		
		String upQuery = "UPDATE gitlogfiles SET recentChurn = ? WHERE commit = ? AND filepath = ?";		
		PreparedStatement ps = conn
				.prepareStatement(query);
		
		PreparedStatement psUpdate = conn.prepareStatement(upQuery);
		
		ResultSet rs = ps.executeQuery();
		
		while(rs.next()){					
			psUpdate.setInt(1, rs.getInt("recentchurn"));
			psUpdate.setString(2,rs.getString("commit"));
			psUpdate.setString(3, rs.getString("filepath"));
			
			psUpdate.addBatch();
		
		}		
		
		log.debug("Executing batch update...");
		psUpdate.executeBatch();
		conn.close();
	
	}
	

}
