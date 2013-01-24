package edu.rit.se.history.httpd.parse;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;

import org.chaoticbits.devactivity.DBUtil;

public class GitRelease {
	private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(GitRelease.class);
	private SimpleDateFormat format;

	public GitRelease() {
		format = new SimpleDateFormat("EEE MMM dd HH:mm:ss yyyy +SSSS");
	}

	/**
	 * This parser assumes that you have the following "pretty" format
	 * 
	 * <code>
	 *  git-log --pretty=format:":::%n%H%n%an%n%ae%n%ad%n%P%n%s%n%b" --stat --ignore-space-change > ../httpd-gitlog.txt
	 *  </code>
	 * 
	 * @param dbUtil
	 * @param gitLog
	 * @throws Exception
	 */
	public void load(DBUtil dbUtil) throws Exception {
		Connection conn = dbUtil.getConnection();
		String query = "UPDATE gitlog g SET releaseVer = "
				+ "(SELECT r1.releaseVer FROM releasehistory r1 WHERE r1.releasedate = " +
				"(SELECT MIN(r2.releasedate) FROM releasehistory r2 WHERE r2.releasedate > g.authordate ))";	
		
		PreparedStatement ps = conn.prepareStatement(query);
		log.debug("Executing major release update...");
		ps.execute(); 		
		conn.close();		
	}
	
}
