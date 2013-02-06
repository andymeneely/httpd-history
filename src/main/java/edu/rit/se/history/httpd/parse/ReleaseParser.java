package edu.rit.se.history.httpd.parse;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Scanner;


import org.chaoticbits.devactivity.DBUtil;

public class ReleaseParser {
	private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(ReleaseParser.class);
	private SimpleDateFormat format;

	public ReleaseParser() {
		format = new SimpleDateFormat("MM/DD/yyyy");
	}

	public void parse(DBUtil dbUtil, File relLog) throws Exception {
		Connection conn = dbUtil.getConnection();
		PreparedStatement ps = conn
				.prepareStatement("INSERT INTO ReleaseHistory(releaseVer, releaseDate) VALUES (?,?)");

		Scanner scanner = new Scanner(relLog);
		log.debug("Scanning the log...");

		String releaseVer;
		Timestamp releaseDate;

		while (scanner.hasNextLine()) {
			String line = scanner.nextLine();
			if (!line.startsWith("#")) { // ignore comments
				String split[] = line.split(",");
				releaseVer = split[0];
				releaseDate = new Timestamp(format.parse(split[1]).getTime()); // Timestamp.valueOf(split[1]);
				ps.setString(1, releaseVer);
				ps.setTimestamp(2, releaseDate);
				ps.addBatch();
			}
		}
		log.debug("Executing batch insert...");
		ps.executeBatch();
		scanner.close();
		conn.close();
	}
	
}
