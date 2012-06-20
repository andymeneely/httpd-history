package edu.rit.se.history.httpd.parse;

import java.io.File;
import java.io.FileReader;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.chaoticbits.devactivity.DBUtil;

import au.com.bytecode.opencsv.CSVReader;

import com.mysql.jdbc.Connection;

public class GroundedTheoryResultsParser {

	public void parse(DBUtil dbUtil, File csv) throws Exception {
		Connection conn = dbUtil.getConnection();
		PreparedStatement ps = conn.prepareStatement("INSERT INTO CVEGroundedTheory(CVE,FixNewCode,Cascades,InputValidation,OutputCleansing,"
				+ "NonIOImprovedLogic, DomainSpecific, Regression, SourceCode, ConfigFile) " + "VALUES (?,?,?,?,?,?,?,?,?,?)");
		PreparedStatement psAsset = conn.prepareStatement("INSERT INTO CVEGroundedTheoryAssets(CVE,Asset) VALUES (?,?)");
		String line[];
		CSVReader reader = new CSVReader(new FileReader(csv));
		reader.readNext(); // skip the header
		while ((line = reader.readNext()) != null) {
			if (line.length < 5)
				continue;
			ps.setString(1, line[0]); // CVE
			ps.setString(2, line[1]); // FixNewCode
			ps.setString(3, line[2]); // Cascades
			ps.setString(4, includes(line[3], "Input", "Both")); // InputValidation
			ps.setString(5, includes(line[3], "Output", "Both")); // OutputCleansing
			ps.setString(6, includes(line[3], "Better Logic")); // NonIOImprovedLogic
			ps.setString(7, line[4]); // DomainSpecific
			ps.setString(8, line[5]); // Regression
			insertAssets(psAsset, line[0], line[6].toLowerCase());
			ps.setString(9, includes(line[7], "Source", "Both")); // Source?
			ps.setString(10, includes(line[7], "Config", "Both")); // Config?
			ps.addBatch();
		}
		psAsset.executeBatch();
		ps.executeBatch();
		conn.close();
	}

	private String includes(String string, String... substrs) {
		for (String substr : substrs) {
			if (string.contains(substr))
				return "Yes";
		}
		return "No";
	}

	private void insertAssets(PreparedStatement ps, String cve, String assetString) throws SQLException {
		String[] assets = assetString.split(",");
		for (String asset : assets) {
			ps.setString(1, cve);
			ps.setString(2, asset.trim());
			ps.addBatch();
		}
	}
}
