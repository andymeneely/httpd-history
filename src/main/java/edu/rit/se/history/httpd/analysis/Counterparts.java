package edu.rit.se.history.httpd.analysis;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Random;

import org.apache.log4j.Logger;
import org.chaoticbits.devactivity.DBUtil;

/**
 * Get counterparts for all VCCs marked "OK" in 'HTTPD Vulnerability Introduction.csv' A counterpart is a
 * commit that affected the vulnerable file from a VCC but has not been shown to have introduced a
 * vulnerability itself.
 * 
 * @author Matt Mokary
 */
public class Counterparts {
	private static Logger log = Logger.getLogger(Counterparts.class);
	
	private DBUtil db = null;
	private String[][] vccsAndFiles = null;

	private int randSeed, maxCounterparts;
	
	// initialize with a specified number of counterparts to generate
	public Counterparts(DBUtil dbUtil, int numCtrparts) {
		this.db = dbUtil;
		this.randSeed = new Random().nextInt();
		this.maxCounterparts = numCtrparts;
	}

	/**
	 * Generate counterparts for all VCCs using the given seed.
	 * 
	 * @param seed
	 *            Seed for java.util.Random, used in picking counterparts
	 * @throws Exception
	 */
	public void generate(int seed) throws Exception {
		this.randSeed = seed;
		generate();
	}

	/**
	 * Generate (new) counterparts for the given VCC, using the given seed.
	 * 
	 * @param commit
	 *            VCC for which to generate (new) counterparts
	 * @param seed
	 *            Seed for java.util.Random, using in picking counterparts
	 * @throws Exception
	 */
	public void generate(String commit, int seed) throws Exception {
		this.randSeed = seed;
		generate(commit);
	}

	/**
	 * Generate (new) counterparts for the given VCC using a random seed.
	 * 
	 * @param commit
	 *            VCC for which to generate (new) counterparts
	 * @throws Exception
	 */
	public void generate(String commit) throws Exception {
		this.vccsAndFiles = getVCCsAndVulnFile();
		if (isVCC(commit)) {
			String[] ctrparts = counterpartsForFile(commit, getFileForCommit(commit), maxCounterparts);
			String[][] newRows = new String[ctrparts.length][2];
			for ( int i = 0; i < ctrparts.length; i++ ) {
				newRows[i][0] = commit;
				newRows[i][1] = ctrparts[i];
			}
			insertNewRows(newRows);
		} else {
			log.error(String.format("Commit %s is not a VCC", commit));
		}
	}

	/**
	 * Generate (new) counterparts for all VCCs using a random seed.
	 * 
	 * @throws Exception
	 */

	public void generate() throws Exception {
		this.vccsAndFiles = getVCCsAndVulnFile();
		ArrayList<String[]> newRows = new ArrayList<String[]>();

		log.debug("Querying for counterparts..");
		for (String[] vccAndFile : this.vccsAndFiles) {
			String[] ctrparts = counterpartsForFile(vccAndFile[0], vccAndFile[1], maxCounterparts);
			for (String ctrpart : ctrparts) {
				String[] row = {vccAndFile[0], ctrpart};
				newRows.add( row );
			}
		}
		
		String[][] newRowsArray = new String[newRows.size()][2];
		newRows.toArray(newRowsArray);
		insertNewRows(newRowsArray);
	}

	private void insertNewRows(String[][] rows) throws SQLException {
		String upQuery = "INSERT INTO Counterparts (Commit,Counterpart) VALUES (?,?);";

		Connection conn = db.getConnection();
		PreparedStatement ps = conn.prepareStatement(upQuery);
		for (String[] row : rows) {
			ps.setString(1, row[0]);
			ps.setString(2, row[1]);
			ps.addBatch();
		}

		log.debug("Executing upsert..");
		ps.executeBatch();
		conn.close();
	}

	private String[][] getVCCsAndVulnFile() throws Exception {
		ArrayList<String[]> okVCCs = new ArrayList<String[]>();
		String query = "SELECT CommitIntroduced,Filepath FROM CVEToGit WHERE CommitIntroduced <> 'N/A';";
		
		Connection conn = db.getConnection();
		PreparedStatement ps = conn.prepareStatement(query);
		ResultSet rs = ps.executeQuery();
		
		while(rs.next()) {
			String[] row = { rs.getString("CommitIntroduced"), rs.getString("Filepath") };
			okVCCs.add( row );
		}
		
		String[][] okVCCsArray = new String[okVCCs.size()][2];
		okVCCs.toArray(okVCCsArray);
		return okVCCsArray;
	}

	private String[] counterpartsForFile(String originalCommit, String file, int numOfCounterparts) throws SQLException {

		String selectClause = "SELECT Commit FROM gitlog";
		String whereClause = String.format("WHERE body LIKE '%%%s%%'", file);
		String query = String.format("%s %s;", selectClause, whereClause);

		Connection conn = db.getConnection();
		PreparedStatement prep = conn.prepareStatement(query);
		ResultSet results = prep.executeQuery();

		results.last();
		int numRows = results.getRow() - 1;
		results.beforeFirst();

		ArrayList<String> counterparts = new ArrayList<String>();
		String commit = null;

		while (results.next()) {
			commit = results.getString("Commit");
			if (!commit.equals(originalCommit) && !isVCC(commit)) {
				counterparts.add(commit);
			}
		}
		conn.close();

		numOfCounterparts = numOfCounterparts > numRows ? numRows : numOfCounterparts;

		return selectNRandom(counterparts, numOfCounterparts);
	}

	private boolean isVCC(String commit) throws SQLException {
		for(String[] vccAndFile : this.vccsAndFiles )
			if (vccAndFile[0].equals(commit))
				return true;
		return false;
	}

	// precondition: `commit` is an OK'd VCC in this.vccsAndFiles
	private String getFileForCommit(String commit) {
		for (String[] commitAndFile : this.vccsAndFiles)
			if (commitAndFile[0].equals(commit))
				return commitAndFile[1];
		return null;
	}

	private String[] selectNRandom(ArrayList<String> arr, int num) {
		String[] result = null;
		try {
			result = new String[num];
		} catch (java.lang.NegativeArraySizeException e) {
			log.error("Table 'gitlog' is empty; cannot get counterparts. " + "Make sure loadGitlog is being called.");
			return null;
		}
		result = selNRandRec(arr, num, new Random(this.randSeed)).toArray(result);
		return result;
	}

	private ArrayList<String> selNRandRec(ArrayList<String> arr, int num, Random r) {
		if (num == 0)
			return new ArrayList<String>();
		int randIndex = r.nextInt(arr.size());
		String element = arr.get(randIndex);
		arr.remove(randIndex);

		ArrayList<String> result = selNRandRec(arr, num - 1, r);
		result.add(element);
		return result;
	}
}