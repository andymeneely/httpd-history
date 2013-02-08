package edu.rit.se.history.httpd.parse;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import org.chaoticbits.devactivity.DBUtil;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class GitLogLOC {

	private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(GitLogLOC.class);

	/**
	 * Updates the GitLogFiles with the LOC of each file at each commit
	 * 
	 * @param dbUtil
	 * @param gitLogLOCjson
	 * @throws Exception
	 */
	public void update(DBUtil dbUtil, File gitLogLOCjson) throws Exception {
		log.debug("Parsing JSON...");
		JSONObject gitLogLocData = parseJSONFromFile(gitLogLOCjson);

		Connection conn = dbUtil.getConnection();
		log.debug("Executing query...");
		ResultSet rs = conn.createStatement().executeQuery("SELECT Commit, Filepath FROM GitLogFiles");

		String update = "UPDATE GitLogFiles SET LinesOfCode = ? WHERE Commit = ? AND Filepath = ?";
		PreparedStatement psUpdate = conn.prepareStatement(update);
		while (rs.next()) {
			Integer loc = parseGitLogLoc(rs.getString("Filepath"), rs.getString("Commit"), gitLogLocData);
			psUpdate.setInt(1, loc);
			psUpdate.setString(2, rs.getString("Commit"));
			psUpdate.setString(3, rs.getString("Filepath"));
			psUpdate.addBatch();
		}

		log.debug("Executing batch update...");
		psUpdate.executeBatch();
	}

	/*
	 * by Brian Spates
	 * 
	 * Matches filepath and commit to said revision's SLOC and returns the SLOC value
	 */
	private int parseGitLogLoc(String filepath, String commit, JSONObject gitLogLocData) throws JSONException {
		JSONArray fileArr = gitLogLocData.getJSONArray(filepath);
		for (int i = 0; i < fileArr.length(); i++) {
			if (fileArr.getString(i).equals(commit)) {
				return fileArr.getInt(i + 1);
			}
		}
		throw new IllegalStateException("No such LOC for + " + filepath + " @ " + commit);
	}

	// Generic method to retrieve JSONObject that represents a whole JSON file
	private JSONObject parseJSONFromFile(File locFile) throws Exception {
		FileInputStream fis = new FileInputStream(locFile);
		InputStreamReader in = new InputStreamReader(fis);
		BufferedReader br = new BufferedReader(in);
		StringBuilder sb = new StringBuilder();
		String line = null;
		while ((line = br.readLine()) != null) {
			sb.append(line + "\n");
		}
		fis.close();
		in.close();
		br.close();
		return new JSONObject(sb.toString());
	}
	/*
	 * END methods contributed by Brian Spates
	 */

}
