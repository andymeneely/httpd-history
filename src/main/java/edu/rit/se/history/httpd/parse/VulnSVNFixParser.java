package edu.rit.se.history.httpd.parse;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.sql.PreparedStatement;

import org.chaoticbits.devactivity.DBUtil;

import au.com.bytecode.opencsv.CSVReader;

import com.mysql.jdbc.Connection;

public class VulnSVNFixParser {

	public void parse(DBUtil dbUtil, File csv) throws Exception {
		Connection conn = dbUtil.getConnection();
		PreparedStatement ps = conn.prepareStatement("INSERT INTO CVESVNFix(CVE,SVNRevision) VALUES (?,?)");
		String line[];
		CSVReader reader = new CSVReader(new FileReader(csv));
		reader.readNext(); // skip the header
		String cve = "";
		while ((line = reader.readNext()) != null) {
			if (line[0].length() > 0)
				cve = line[0];
			ps.setString(1, cve);
			try {
				ps.setInt(2, Integer.valueOf(line[2]));
				ps.addBatch();
			} catch (NumberFormatException e) {/* skip it */}
		}
		ps.executeBatch();
		conn.close();
	}
}
