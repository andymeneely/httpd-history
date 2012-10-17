package edu.rit.se.history.httpd.parse;

import java.io.File;
import java.io.FileReader;
import java.sql.PreparedStatement;

import org.chaoticbits.devactivity.DBUtil;

import au.com.bytecode.opencsv.CSVReader;

import com.mysql.jdbc.Connection;

public class CVEsParser {

	public void parse(DBUtil dbUtil, File csv) throws Exception {
		Connection conn = dbUtil.getConnection();
		PreparedStatement ps = conn.prepareStatement("INSERT INTO CVE(CVE,CWE,CWETop25,CVSS,ConfidentialityImpact,"
				+ "IntegrityImpact,AvailabilityImpact,AccessComplexity,AuthRequired,GainedAccess) "
				+ "VALUES (?,?,?,?,?,?,?,?,?,?)");
		String line[];
		CSVReader reader = new CSVReader(new FileReader(csv));
		reader.readNext(); // skip the header
		while ((line = reader.readNext()) != null) {
			for (int i = 1; i <= 10; i++) {
				if (i == 4) { // CVSS score
					ps.setDouble(i, Double.valueOf(line[i - 1]));
				} else
					ps.setString(i, line[i - 1]);
			}
			ps.addBatch();
		}
		reader.close();
		ps.executeBatch();
		conn.close();
	}
}
