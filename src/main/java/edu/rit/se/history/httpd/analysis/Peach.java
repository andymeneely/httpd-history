package edu.rit.se.history.httpd.analysis;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import org.chaoticbits.devactivity.DBUtil;

public class Peach {
	private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(Peach.class);

	public void compute(DBUtil dbUtil, long recentPeriod) throws Exception {
		Connection conn = dbUtil.getConnection();
		
		/*String query = "SELECT r.filepath, r.commit, r.authordate, ( "
				+ "( SELECT count(distinct effectiveauthor) FROM gitchurneffectiveauthors a "
				+ " INNER JOIN repolog _r ON a.commit = _r.commit AND a.filepath = _r.filepath "
				+ "	 WHERE effectiveauthor IN (SELECT effectiveauthor FROM gitchurneffectiveauthors b "
				+ "                     WHERE b.commit = r.commit AND b.filepath = r.filepath) "
				+ "  AND (a.commit != r.commit OR a.filepath != r.filepath) "
				+ " AND _r.authordate <= r.authordate AND DATEDIFF(r.authordate, _r.authordate) <= ? " + ")"
				+ "/"
				+ "(" + " SELECT count(effectiveauthor) FROM gitchurneffectiveauthors c "
				+ " WHERE c.commit = r.commit AND c.filepath = r.filepath " + " ) " + "	)*100 as PEACh "
				+ " FROM Repolog r";
*/
		
		String query ="UPDATE Gitlogfiles g SET peach = " +  
				"( SELECT count(distinct effectiveauthor) FROM gitchurneffectiveauthors a "+ 
				"			WHERE a.effectiveauthor IN ( "+
				"								SELECT effectiveauthor FROM gitchurneffectiveauthors b "+
				"									WHERE b.commit = g.commit AND b.filepath = g.filepath "+ 
				"								) "+
				"			AND (a.commit != g.commit OR a.filepath != g.filepath) "+
                "            AND a.authordate <= g.authordate AND DATEDIFF(g.authordate, a.authordate) <= ? "+
				") / EffectiveAuthors ";
		
		String upEAQuery = "UPDATE Gitlogfiles g SET effectiveAuthors = ( " +
    					"SELECT count(effectiveauthor) FROM gitchurneffectiveauthors c " +
    					 "WHERE c.commit = g.commit AND c.filepath = g.filepath " +    					
     					")";
		
		String peachQuery = "SELECT g.filepath, g.commit, g.authordate,  "+
								"( SELECT count(distinct effectiveauthor) FROM gitchurneffectiveauthors a "+
								"			WHERE a.effectiveauthor IN ( "+
								"								SELECT effectiveauthor FROM gitchurneffectiveauthors b "+
								"									WHERE b.commit = g.commit AND b.filepath = g.filepath  "+
								"								) "+
								"			AND (a.commit != g.commit OR a.filepath != g.filepath) "+
                                "           AND a.authordate <= g.authordate AND DATEDIFF(g.authordate, a.authordate) <= ? "+
								") /  EffectiveAuthors as PEACh "+
                		        "FROM Gitlogfiles g ";

		String upQuery = "UPDATE gitlogfiles SET peach = ? WHERE commit = ? AND filepath = ?";
		PreparedStatement psEAUpdate = conn.prepareStatement(upEAQuery);
		PreparedStatement psPeachQuery = conn.prepareStatement(peachQuery);
		PreparedStatement psUpdate = conn.prepareStatement(upQuery);
		
		log.debug("Updating effective authors...");
		psEAUpdate.executeUpdate();
		/*log.debug("Executing Peach Query...");
		psPeachQuery.setLong(1, recentPeriod);
		ResultSet rs = psPeachQuery.executeQuery();		 
		log.debug("Processing results...");
		while (rs.next()) {
			psUpdate.setInt(1, rs.getInt("peach"));
			psUpdate.setString(2, rs.getString("commit"));
			psUpdate.setString(3, rs.getString("filepath"));
			psUpdate.addBatch();
		}
		log.debug("Executing update...");
		psUpdate.executeBatch();*/
		log.debug("Executing Peach update...");
		PreparedStatement psPeachUpdate = conn.prepareStatement(query);
		psPeachUpdate.setLong(1, recentPeriod);
		psPeachUpdate.executeUpdate();
		conn.close();

	}

}
