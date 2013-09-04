package edu.rit.se.history.httpd.parse;

import java.io.File;
import java.io.FileReader;
import java.sql.PreparedStatement;

import org.chaoticbits.devactivity.DBUtil;

import au.com.bytecode.opencsv.CSVReader;

import com.mysql.jdbc.Connection;

import edu.rit.se.history.httpd.RebuildHistory;

public class CVEToGit {

	private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(RebuildHistory.class);
	
	public void parse(DBUtil dbUtil, File csv) throws Exception {
		Connection conn = dbUtil.getConnection();
		PreparedStatement ps = conn.prepareStatement("INSERT INTO CVEToGit(CVE,Filepath,CommitIntroduced,CommitFixed,"
				+ "IsBaselineCommit) VALUES (?,?,?,?,?)");
		String line[];
		CSVReader reader = new CSVReader(new FileReader(csv));
		reader.readNext(); // skip the header
		String cve = "", filepath = "", commitFixed = "", commitIntroduced = "", isBaselineCommit = "";
		while ((line = reader.readNext()) != null) {
			cve = line[0];
			filepath = line[2].trim();
			commitIntroduced = line[3].trim();
			commitFixed = line[4].trim();
			isBaselineCommit = line[8].trim();
			int i = 1;
			ps.setString(i++, cve);
			ps.setString(i++, filepath);
			ps.setString(i++, commitIntroduced);
			ps.setString(i++, commitFixed);
			ps.setString(i++, isBaselineCommit.equals("") ? null : isBaselineCommit );
			ps.addBatch();
		}
		reader.close();
		ps.executeBatch();
		conn.close();
	}
}
