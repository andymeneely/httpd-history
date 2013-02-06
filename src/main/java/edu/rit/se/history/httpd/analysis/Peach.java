package edu.rit.se.history.httpd.analysis;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import org.chaoticbits.devactivity.DBUtil;

public class Peach {
	private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(Peach.class);

	public void compute(DBUtil dbUtil, long recentPeriod) throws Exception {
		Connection conn = dbUtil.getConnection();
		/*String query = "SELECT g.filepath, g.commit, l.authordate, ( " +
									"( SELECT count(distinct effectiveauthor) FROM gitchurneffectiveauthors a "
    +     							" INNER JOIN gitlog _l ON a.commit = _l.commit "
    +           					" WHERE _l.authordate <= l.authordate AND DATEDIFF(l.authordate, _l.authordate) <= ? "
    +           					" AND (a.commit != g.commit OR a.filepath != g.filepath) AND effectiveauthor IN (SELECT effectiveauthor FROM gitchurneffectiveauthors b "
    +                                                    " WHERE b.commit = g.commit AND b.filepath = g.filepath) "
    +            					")"
    +            		"/"
    +            		"("
    +					" SELECT count(effectiveauthor) FROM gitchurneffectiveauthors a " 
    +					" WHERE a.commit = g.commit AND a.filepath = g.filepath "
    +					")"
    + 					")*100 as PEACh "
    +	"FROM gitlogfiles g INNER JOIN gitlog l ON l.commit = g.commit ";*/
		String query = "SELECT r.filepath, r.commit, r.authordate, ( "
							+ "	( SELECT count(distinct effectiveauthor) FROM gitchurneffectiveauthors a "
         					+ "	 INNER JOIN repolog _r ON a.commit = _r.commit AND a.filepath = _r.filepath "
               				+ "	 WHERE _r.authordate <= r.authordate AND DATEDIFF(r.authordate, _r.authordate) <= ? "
               				+ "	 AND (a.commit != r.commit OR a.filepath != r.filepath) AND effectiveauthor IN (SELECT effectiveauthor FROM gitchurneffectiveauthors b " 
                            + "                            WHERE b.commit = r.commit AND b.filepath = r.filepath)  "
                			+ "		)"
                		+"/"
                		+"("
    					+ "SELECT count(effectiveauthor) FROM gitchurneffectiveauthors c "  
    					+ " WHERE c.commit = r.commit AND c.filepath = r.filepath " 
    					+ ")"
     					+ ")*100 as PEACh " 
    	+ " FROM Repolog r";
			
   
		String upQuery = "UPDATE gitlogfiles SET peach = ? WHERE commit = ? AND filepath = ?";
		PreparedStatement ps = conn.prepareStatement(query);
		PreparedStatement psUpdate = conn.prepareStatement(upQuery);
		ps.setLong(1, recentPeriod);
		log.debug("Executing query...");
		ResultSet rs = ps.executeQuery();
		log.debug("Processing results...");
		while (rs.next()) {
			psUpdate.setInt(1, rs.getInt("peach"));
			psUpdate.setString(2, rs.getString("commit"));
			psUpdate.setString(3, rs.getString("filepath"));
			psUpdate.addBatch();
		}
		log.debug("Executing update...");
		psUpdate.executeBatch();
		conn.close();

	}

}
