package edu.rit.se.history.httpd.parse;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import org.chaoticbits.devactivity.DBUtil;

public class GitLogParser {
	private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(GitLogParser.class);
	private SimpleDateFormat format;

	public GitLogParser() {
		format = new SimpleDateFormat("EEE MMM dd HH:mm:ss yyyy +SSSS");
	}

	/**
	 * This parser assumes that you have the following "pretty" format
	 * 
	 * <code>
	 *  git-log --pretty=format:":::%n%H%n%an%n%ae%n%ad%n%P%n%s%n%b" --stat --ignore-space-change > ../httpd-gitlog.txt
	 *  </code>
	 * 
	 * @param dbUtil
	 * @param csv
	 * @throws Exception
	 */
	public void parse(DBUtil dbUtil, File csv) throws Exception {
		Connection conn = dbUtil.getConnection();
		PreparedStatement ps = conn.prepareStatement("INSERT INTO GitLog(Commit, AuthorName, AuthorEmail, "
				+ "AuthorDate, Parent, Subject, Body, NumSignedOffBys) " + "VALUES (?,?,?,?,?,?,?,?)");
		PreparedStatement ps2 = conn
				.prepareStatement("INSERT INTO GitLogFiles(Commit,Filepath,NumChanges,LinesInserted,LinesDeleted) "
						+ "VALUES (?,?,?,?,?)");
		Scanner scanner = new Scanner(csv);
		log.debug("Scanning the log...");
		scanner.nextLine();
		while (scanner.hasNextLine()) {
			String commit = scanner.nextLine();
			String authorName = scanner.nextLine();
			String authorEmail = scanner.nextLine();
			String authorDate = scanner.nextLine();
			String parent = scanner.nextLine();
			String subject = scanner.nextLine();
			// if the last line of the file has no file changes, then we're done here
			if (!scanner.hasNextLine())
				break;
			// Parse the body by going line-by-line until we get to the nextq ::: delimiter
			String body = "";
			String nextLine = scanner.nextLine();
			List<String> bodyLines = new ArrayList<String>(100);
			int numSignedOffBys = 0;
			while (scanner.hasNextLine() && //
					(nextLine.length() < 3 || !nextLine.startsWith(":::"))) {
				body += nextLine + "\n";
				bodyLines.add(nextLine);
				if (nextLine.contains("Signed-off-by:"))
					numSignedOffBys++;
				nextLine = scanner.nextLine();
			}
			parseFileChanges(conn, commit, bodyLines, ps2);
			int i = 1;
			ps.setString(i++, commit);
			ps.setString(i++, authorName);
			ps.setString(i++, authorEmail);
			ps.setTimestamp(i++, new java.sql.Timestamp(parseDate(authorDate).getTime()));
			ps.setString(i++, parent);
			ps.setString(i++, subject);
			ps.setString(i++, body);
			ps.setInt(i++, numSignedOffBys);
			ps.addBatch();

		}
		log.debug("Executing batch insert...");
		ps.executeBatch();
		ps2.executeBatch();
		scanner.close();
		conn.close();
	}

	private static void parseFileChanges(Connection conn, String commit, List<String> bodyLines, PreparedStatement ps2)
			throws Exception {
		if (bodyLines.size() < 3) // no changes to files - you can do this by changing file permissions in
									// git
			return;
		for (int i = bodyLines.size() - 3; i >= 0 && !"".equals(bodyLines.get(i)); i--) {
			String fileChange = bodyLines.get(i);
			int pipeLoc = fileChange.indexOf("|");
			int numInsertions = countOccurrences(fileChange.substring(pipeLoc), '+');
			int numDeletions = countOccurrences(fileChange.substring(pipeLoc), '-');
			int numChanges;
			try {
				numChanges = Integer.valueOf(fileChange.substring(pipeLoc + 1, pipeLoc + 6).trim());
			} catch (NumberFormatException e) {// Sometimes when there are no changes, there's text saying
												// there's no changes
				numChanges = 0;
			}
			String file = fileChange.substring(0, pipeLoc).trim();
			if(file.startsWith(".../")) //remove the .../tests filepath weirdness
				file = file.substring(4);
			ps2.setString(1, commit);
			ps2.setString(2, file);
			ps2.setInt(3, numChanges);
			ps2.setInt(4, numInsertions);
			ps2.setInt(5, numDeletions);
			ps2.addBatch();
		}
	}

	private static int countOccurrences(String str, char c) {
		int count = 0;
		for (int i = 0; i < str.length(); i++)
			if (str.charAt(i) == c)
				count++;
		return count;
	}

	public java.util.Date parseDate(String testStr) throws ParseException {
		if (testStr.startsWith("Sun Mar 12 02")) // stupid daylight savings bug
			testStr = "Sun Mar 12 01:00:00 2000 +0000";
		if (testStr.startsWith("Sun Mar 11 02")) // stupid daylight savings bug
			testStr = "Sun Mar 11 01:00:00 2001 +0000";
		if (testStr.startsWith("Sun Mar 10 02")) // stupid daylight savings bug
			testStr = "Sun Mar 10 01:00:00 2002 +0000";
		return format.parse(testStr);
	}
}
