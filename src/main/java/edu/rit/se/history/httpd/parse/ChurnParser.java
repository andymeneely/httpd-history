package edu.rit.se.history.httpd.parse;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Scanner;

import org.chaoticbits.devactivity.DBUtil;

public class ChurnParser {
	private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(ChurnParser.class);

	/**
	 * This parser assumes the aggregated output of Developer Activity/src/main/ruby/git-interaction-churn.rb
	 * 
	 * @param dbUtil
	 * @param churnLog
	 * @throws Exception
	 */
	public void parse(DBUtil dbUtil, File churnLog) throws Exception {
		Connection conn = dbUtil.getConnection();
		PreparedStatement ps = conn.prepareStatement("UPDATE GitLogFiles SET "
				+ "LinesInserted=?, LinesDeleted=?, LinesDeletedSelf=?, LinesDeletedOther=?, "
				+ "AuthorsAffected=?, EffectiveAuthors=?, NewEffectiveAuthor=? WHERE Commit=? AND Filepath=?");
		PreparedStatement psAuthorAffected = conn
				.prepareStatement("INSERT INTO GitChurnAuthorsAffected(Commit, Filepath, AuthorAffected) VALUES (?,?,?) ");
		PreparedStatement psEffectiveAuthors = conn
				.prepareStatement("INSERT INTO GitChurnEffectiveAuthors(Commit, Filepath, EffectiveAuthor) VALUES (?,?,?) ");
		Scanner scanner = new Scanner(churnLog);
		log.debug("Scanning the churn log...");
		while (scanner.hasNextLine()) {
			String commit = scanner.nextLine();
			String filepath = scanner.nextLine();
			String nextLine = scanner.nextLine();
			while (isCommitHash(nextLine)) {
				filepath = scanner.nextLine();
				nextLine = scanner.nextLine();
				// must have been an error on running that last one
				// skip it, and keep going two-at-a-time until we get a "total churn" line
				// which means we're good to go on
			}
			Integer linesInserted = parseInt(scanner.nextLine(), "Lines Added:\t");
			Integer linesDeleted = parseInt(scanner.nextLine(), "Lines Deleted:\t");
			Integer linesDeletedSelf = parseInt(scanner.nextLine(), "Lines Deleted, self:\t");
			Integer linesDeletedOther = parseInt(scanner.nextLine(), "Lines Deleted, other:\t");
			Integer authorsAffected = parseInt(scanner.nextLine(), "Number of Authors Affected:\t");
			parseAuthorsAffected(psAuthorAffected, scanner.nextLine(), commit, filepath);
			Integer effectiveAuthors = parseInt(scanner.nextLine(), "Number of Effective Authors:\t");
			parseEffectiveAuthors(psEffectiveAuthors, scanner.nextLine(), commit, filepath);
			String isNewEffectiveAuthor = scanner.nextLine().substring("New effective author?\t".length() + 1);
			scanner.nextLine(); //skip the empty line
			int i = 1;
			ps.setInt(i++, linesInserted);
			ps.setInt(i++, linesDeleted);
			ps.setInt(i++, linesDeletedSelf);
			ps.setInt(i++, linesDeletedOther);
			ps.setInt(i++, authorsAffected);
			ps.setInt(i++, effectiveAuthors);
			ps.setString(i++, isNewEffectiveAuthor);
			ps.setString(i++, commit);
			ps.setString(i++, filepath);
			ps.addBatch();
		}
		scanner.close();
		log.debug("Executing batch update...");
		ps.executeBatch();
		log.debug("Executing batch insert...");
		psAuthorAffected.executeBatch();
		conn.close();
	}

	private boolean isCommitHash(String line) {
		return line.matches("[0-9a-f]{40}");
	}

	private String parseAuthorsAffected(PreparedStatement ps, String nextLine, String commit, String filepath)
			throws SQLException {
		String[] authorsAffected = nextLine.split("\t");
		for (String authorAffected : authorsAffected) {
			ps.setString(1, commit);
			ps.setString(2, filepath);
			ps.setString(3, authorAffected);
			ps.addBatch();
		}
		return null;
	}

	private String parseEffectiveAuthors(PreparedStatement ps, String nextLine, String commit, String filepath)
			throws SQLException {
		String[] effectiveAuthors = nextLine.split("\t");
		for (String author : effectiveAuthors) {
			ps.setString(1, commit);
			ps.setString(2, filepath);
			ps.setString(3, author);
			ps.addBatch();
		}
		return null;
	}

	private Integer parseInt(String line, String... removes) {
		String str = " " + line;
		for (String remove : removes) {
			str = str.replaceAll(remove, "");
		}
		
		return Integer.valueOf(str.trim());
	}
}
