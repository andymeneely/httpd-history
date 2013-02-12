package edu.rit.se.history.httpd.analysis;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;

import org.chaoticbits.devactivity.DBUtil;

import com.mysql.jdbc.Connection;

public class TimelineTables {
	private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(TimelineTables.class);

	private static String cveQuerySQL = "SELECT DISTINCT c2g.cve CVE FROM " //
			+ "CVEToGit AS c2g, " //
			+ "RepoLog AS repoIntro, " //
			+ "RepoLog AS repoFixed " //
			+ "WHERE " //
			+ "c2g.filepath LIKE ? " //
			+ "AND repoIntro.filepath LIKE ? " //
			+ "AND repoFixed.filepath LIKE ? " //
			+ "AND c2g.CommitIntroduced = repoIntro.commit " //
			+ "AND repoIntro.authordate < ? " //
			+ "AND c2g.CommitFixed = repoFixed.commit " //
			+ "AND repoFixed.authordate >= ? " //
	;

	private final DBUtil dbUtil;

	public TimelineTables(DBUtil dbUtil) {
		this.dbUtil = dbUtil;
	}

	public void build(Timestamp start, Timestamp stop, long stepMillis) throws Exception {
		Connection conn = dbUtil.getConnection();
		log.debug("Clearing timeline table...");
		conn.createStatement().execute("DELETE FROM Timeline");
		PreparedStatement cveQuery = conn.prepareStatement(cveQuerySQL);
		PreparedStatement insertTimeline = conn
				.prepareStatement("INSERT INTO Timeline(Filepath, NumCVEs, AtTime, CVEs) VALUES (?,?,?,?)");
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
