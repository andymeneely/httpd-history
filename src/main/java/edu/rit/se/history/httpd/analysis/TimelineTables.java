package edu.rit.se.history.httpd.analysis;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

import org.chaoticbits.devactivity.DBUtil;

import com.mysql.jdbc.Connection;

public class TimelineTables {
	private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(TimelineTables.class);

	private static String cveQuerySQL = "SELECT cveFixed.cve CVE FROM " //
			+ "CVEToGit AS cveFixed, " //
			+ "CVEToGit AS cveIntro, " //
			+ "RepoLog AS repo " //
			+ "WHERE " //
			+ "cveFixed.filepath LIKE ? " //
			+ "AND cveIntro.filepath LIKE ? " //
			+ "AND repo.filepath LIKE ? " //
			+ "AND (cveFixed.CommitIntroduced = repo.commit) " //
			+ "AND repo.authordate < ? " //
			+ "AND cveIntro.cve in (SELECT cve FROM CVEToGit AS cveFixed, " //
			+ "						RepoLog AS repo " //
			+ "						WHERE " //
			+ "            				cveFixed.commitFixed = repo.commit " //
			+ "            				AND (repo.authordate IS NULL OR repo.authordate > ?)) " //
			+ "GROUP BY cveFixed.cve";

	private final DBUtil dbUtil;

	public TimelineTables(DBUtil dbUtil) {
		this.dbUtil = dbUtil;
	}

	public void build(Timestamp start, Timestamp stop, long stepMillis) throws Exception {
		Connection conn = dbUtil.getConnection();
		PreparedStatement cveQuery = conn.prepareStatement(cveQuerySQL);
		PreparedStatement insertTimeline = conn
				.prepareStatement("INSERT INTO Timeline(Filepath, NumCVEs, AtTime, CVEs) " + "VALUES (?,?,?,?)");
		ResultSet filepaths = conn.createStatement().executeQuery("SELECT DISTINCT Filepath FROM CVEToGit " + //
				"WHERE Filepath LIKE '%.c' OR Filepath LIKE '%.h'");
		log.debug("Looking up timeline data points...");
		while (filepaths.next()) {
			String filepath = filepaths.getString("Filepath");
			for (long now = start.getTime(); now < stop.getTime(); now += stepMillis) {
				Timestamp nowSQL = new Timestamp(now);
				int i = 1;
				cveQuery.setString(i++, "%" + filepath + "%");
				cveQuery.setString(i++, "%" + filepath + "%");
				cveQuery.setString(i++, "%" + filepath + "%");
				cveQuery.setTimestamp(i++, nowSQL);
				cveQuery.setTimestamp(i++, nowSQL);
				ResultSet cveRS = cveQuery.executeQuery();
				int count = 0;
				String cves = "";
				while (cveRS.next()) {
					cves += cveRS.getString("CVE") + ", ";
					count++;
				}
				cveRS.close();
				int j = 1;
				insertTimeline.setString(j++, filepath);
				insertTimeline.setInt(j++, count);
				insertTimeline.setTimestamp(j++, nowSQL);
				insertTimeline.setString(j++, cves);
				insertTimeline.executeUpdate();
				insertTimeline.clearParameters();
			}
		}
		insertTimeline.close();
		conn.close();
	}
}
