package edu.rit.se.history.httpd.parse;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;

import org.chaoticbits.devactivity.DBUtil;

public class ReleaseParser {
	private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(ReleaseParser.class);
	private SimpleDateFormat format;

	public ReleaseParser() {
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
	public void parse(DBUtil dbUtil, File relLog) throws Exception {
		Connection conn = dbUtil.getConnection();
		PreparedStatement ps = conn.prepareStatement("INSERT INTO ReleaseHistory(releaseVer, releaseDate) VALUES (?,?)");
		
		Scanner scanner = new Scanner(relLog);
		log.debug("Scanning the log...");
		
		String releaseVer ;
		Timestamp releaseDate;
				
		 
		while (scanner.hasNextLine()) {
			String line = scanner.nextLine();
			System.out.println(line);
			String split[] = line.split(",");		
			
			System.out.println("Release: " + split[0] + " Date: " + split[1]);
			
			releaseVer = split[0];			
			releaseDate = new Timestamp( new Date(split[1]).getTime());  //Timestamp.valueOf(split[1]);  
			
			ps.setString(1, releaseVer);
			ps.setTimestamp(2, releaseDate);			
			ps.addBatch();

		}
		log.debug("Executing batch insert...");
		ps.executeBatch();
		scanner.close();
		conn.close();		
	}
	
}
