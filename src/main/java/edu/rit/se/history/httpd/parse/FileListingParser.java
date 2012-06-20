package edu.rit.se.history.httpd.parse;

import java.io.File;
import java.io.FileNotFoundException;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Scanner;

import org.chaoticbits.devactivity.DBUtil;

import com.mysql.jdbc.Connection;

public class FileListingParser {

	public void parse(DBUtil dbUtil, File file, String version) throws SQLException, FileNotFoundException {
		Connection conn = dbUtil.getConnection();
		PreparedStatement ps = conn.prepareStatement("INSERT INTO Filepaths(Filepath,TomcatRelease) VALUES (?,?)");
		Scanner scanner = new Scanner(file);
		while (scanner.hasNextLine()) {
			String line = scanner.nextLine();
			ps.setString(1, line);
			ps.setString(2, version);
			ps.addBatch();
		}
		ps.executeBatch();
		scanner.close();
		conn.close();
	}

}
