package edu.rit.se.history.httpd.parse;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import org.chaoticbits.devactivity.DBUtil;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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
	 * @param gitLog
	 * @throws Exception
	 */
	public void parse(DBUtil dbUtil, File gitLog, File gitLogLoc) throws Exception {
		JSONObject gitLogLocData = parseJSONFromFile(gitLogLoc); 
		Connection conn = dbUtil.getConnection();
		PreparedStatement ps = conn.prepareStatement("INSERT INTO GitLog(Commit, AuthorName, AuthorEmail, "
				+ "AuthorDate, Parent, Subject, Body, NumSignedOffBys) " + "VALUES (?,?,?,?,?,?,?,?)");
		PreparedStatement ps2 = conn.prepareStatement("INSERT INTO GitLogFiles(Commit,Filepath,LinesOfCode) " + "VALUES (?,?,?)");
		Scanner scanner = new Scanner(gitLog);
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
			parseFileChanges(conn, commit, bodyLines, ps2, gitLogLocData);
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

	private static void parseFileChanges(Connection conn, String commit, List<String> bodyLines, PreparedStatement ps2, JSONObject gitLogLocData)
			throws Exception {
		if (bodyLines.size() < 3) // no changes to files - you can do this by changing file permissions in git
			return;
		for (int i = bodyLines.size() - 3; i >= 0 && !"".equals(bodyLines.get(i)); i--) {
			String fileChange = bodyLines.get(i);
			int pipeLoc = fileChange.indexOf("|");
			String file = fileChange.substring(0, pipeLoc).trim();
			if (file.startsWith(".../")) // remove the .../tests filepath weirdness
				file = file.substring(4);
			
			Integer loc = parseGitLogLoc(file, commit, gitLogLocData);
			
			ps2.setString(1, commit);
			ps2.setString(2, file);
			ps2.setInt(3, loc);
			
			ps2.addBatch();
		}
	}
	
	/* 
	 * Begin Methods contributed by Brian Spates
	 * Method matches filepath and commit to said revision's SLOC and returns the SLOC value
	 */
	private static int parseGitLogLoc(String filepath, String commit, JSONObject gitLogLocData)
	{
		JSONArray fileArr;
		try {
			fileArr = gitLogLocData.getJSONArray(filepath);
		
			for(int i = 0; i < fileArr.length(); i++)
			{
				if(fileArr.getString(i).equals(commit))
				{
					return fileArr.getInt(i+1);
				}
			}
			return 0;
		} catch (JSONException e) {
			log.info(e.getMessage());
			return 0;
		}
	}
	
	// Generic method to retrieve JSONObject that represents a whole JSON file
	private JSONObject parseJSONFromFile(File locFile)
	{
		String logs;
		try{
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
            logs = sb.toString();
            
            JSONObject gitLogLocData = new JSONObject(logs);
            
            return gitLogLocData;
	
		}
		catch(JSONException e)
		{
			log.error(e.getMessage());
			return null;
		}
		catch(IOException e)
		{
			log.error(e.getMessage());
			return null;
		}
		
	}
	/* 
	 * END methods contributed by Brian Spates
	 */

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
