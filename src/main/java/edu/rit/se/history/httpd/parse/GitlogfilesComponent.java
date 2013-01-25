package edu.rit.se.history.httpd.parse;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import org.chaoticbits.devactivity.DBUtil;

public class GitlogfilesComponent {
	private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(GitlogfilesComponent.class);
	
	public GitlogfilesComponent() {

	}

	public void load(DBUtil dbUtil) throws Exception {
		Connection conn = dbUtil.getConnection();
		
		String query = "select commit, filepath, componentpath from gitlogfiles" +
				" left outer join httpdcomponent on filepath like concat(componentpath,'%')";
		String upQuery = "UPDATE Gitlogfiles SET component = ? WHERE commit = ? AND filepath = ?";
		
		ResultSet rs = conn.createStatement().executeQuery(query);
		PreparedStatement ps = conn.prepareStatement(upQuery);
		
		
		while (rs.next()){
			ps.setString(1, rs.getString("componentpath"));
			ps.setString(2,rs.getString("commit"));
			ps.setString(3,rs.getString("filepath"));
			ps.addBatch();			
		}
		
		log.debug("Executing Gitlogfile Component batch update...");
		ps.executeBatch();
				
	}
	
}
