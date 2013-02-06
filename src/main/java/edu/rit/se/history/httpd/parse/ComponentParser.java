package edu.rit.se.history.httpd.parse;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.Scanner;

import org.chaoticbits.devactivity.DBUtil;

public class ComponentParser {
	private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(ComponentParser.class);

	public void parse(DBUtil dbUtil, File compLog) throws Exception {
		Connection conn = dbUtil.getConnection();
		PreparedStatement ps = conn.prepareStatement("INSERT INTO Components(ComponentPath) VALUES (?)");

		Scanner scanner = new Scanner(compLog);
		log.debug("Scanning Component log...");

		while (scanner.hasNextLine()) {
			String line = scanner.nextLine();
			if (!line.startsWith("#")) { // ignore comments
				String split[] = line.split(",");
				String compPath = split[0];
				// compDesc = split[1]; //may be implemented in the future
				ps.setString(1, compPath);
				// ps.setString(2, compDesc); //may be implemented in the future
				ps.addBatch();
			}
		}
		log.debug("Executing batch insert...");
		ps.executeBatch();
		scanner.close();
		conn.close();
	}

}
