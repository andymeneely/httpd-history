package edu.rit.se.history.httpd.parse;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.chaoticbits.devactivity.DBUtil;

import au.com.bytecode.opencsv.CSVReader;

import com.mysql.jdbc.Connection;

public class SLOCParser {
	public void parse(DBUtil dbUtil, File csv, String version) throws SQLException, IOException {
		Connection conn = dbUtil.getConnection();
		PreparedStatement ps = conn.prepareStatement("UPDATE Filepaths SET SLOC=?, SLOCType=? WHERE Filepath=?");
		String line[];
		CSVReader reader = new CSVReader(new FileReader(csv));
		while ((line = reader.readNext()) != null) {
			ps.setString(1, line[2]); // SLOC count
			ps.setString(2, line[1]); // SLOC language type
			ps.setString(3, line[0]); // Filepath
			ps.addBatch();
		}
		ps.executeBatch();
		reader.close();
		conn.close();
	}
}
