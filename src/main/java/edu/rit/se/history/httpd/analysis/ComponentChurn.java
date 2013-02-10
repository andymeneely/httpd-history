package edu.rit.se.history.httpd.analysis;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import org.chaoticbits.devactivity.DBUtil;

public class ComponentChurn {
	private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(ComponentChurn.class);

	

	public void compute(DBUtil dbUtil, long recentPeriod) throws Exception {
		Connection conn = dbUtil.getConnection();
	String query =	
		"UPDATE gitlogfiles g INNER JOIN gitlog l ON g.commit=l.commit SET componentChurn =    "    
		+"("
		+"	SELECT SUM(linesinserted+linesdeleted) FROM Repolog r "
		+"	WHERE r.component = g.component AND r.authordate <= l.authordate "
        +"    AND DATEDIFF(l.authordate,r.authordate) <= ? "
        +")	";
        
		
//		String query = "SELECT r0.commit, r0.filepath, r0.component, r0.authordate, "   
//		        + "(SELECT SUM(r1.linesinserted+r1.linesdeleted) " 
//		        + "FROM Repolog r1 " 
//		        + "WHERE r1.component = r0.component " 
//		        + "AND r1.authordate <= r0.authordate "
//		        + "AND DATEDIFF(r0.authordate,r1.authordate) <= ? ) as componentChurn  "
//		+"FROM repolog r0 ";
//		
		//String upQuery = "UPDATE gitlogfiles SET componentChurn = ? WHERE commit = ? AND filepath = ?";
		PreparedStatement ps = conn.prepareStatement(query);
		/*PreparedStatement psUpdate = conn.prepareStatement(upQuery);
		ps.setLong(1, recentPeriod);		
		log.debug("Executing query...");
		ResultSet rs = ps.executeQuery();
		log.debug("Processing results...");
		while (rs.next()) {
			psUpdate.setInt(1, rs.getInt("componentChurn"));			
			psUpdate.setString(2, rs.getString("commit"));
			psUpdate.setString(3, rs.getString("filepath"));
			psUpdate.addBatch();
		}
		log.debug("Executing update...");
		psUpdate.executeBatch();*/
		
		ps.setLong(1, recentPeriod);
		int rowsaffected=ps.executeUpdate();
		conn.close();
		log.info("update completed... " + rowsaffected + " rows affected" );

	}

}
