package edu.rit.se.history.httpd.analysis;

import java.io.File;
import java.io.FileReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import org.apache.log4j.Logger;
import org.chaoticbits.devactivity.DBUtil;

import au.com.bytecode.opencsv.CSVReader;

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
	private String[] vccsAndFiles = null;

	private int randSeed, maxCounterparts;
	private File vulnIntroCSV;
	
	// initialize with a specified number of counterparts to generate
	public Counterparts(DBUtil dbUtil, File vulnIntroCSV, int numCtrparts) {
		this.db = dbUtil;
		this.vulnIntroCSV = vulnIntroCSV;
		this.randSeed = new Random().nextInt();
		this.maxCounterparts = numCtrparts;
	}

	/**
	 * Generate counterparts for all OK'd VCCs using the given seed.
	 * 
	 * @param seed
	 *            Seed for java.util.Random, used in picking counterparts
	 * @throws SQLException
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
	 * @throws SQLException
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
	 * @throws SQLException
	 */
	public void generate(String commit) throws Exception {
		this.vccsAndFiles = getOkVCCsAndVulnFile();
		if (isVCC(commit)) {
			String file = null;
			String ctrparts = joinCSV(counterpartsForFile(commit, getFileForCommit(commit), maxCounterparts));
			String[][] newRows = new String[1][2];
			newRows[0][0] = commit + "," + file;
			newRows[0][1] = ctrparts;
			upsertNewRows(newRows);
		} else {
			log.error(String.format("Commit %s is not an OK'd VCC", commit));
		}
	}

	/**
	 * Generate (new) counterparts for all OK'd VCCs using a random seed.
	 * 
	 * @throws SQLException
	 */

	public void generate() throws Exception {
		this.vccsAndFiles = getOkVCCsAndVulnFile();
		int numVCCs = this.vccsAndFiles.length;
		String[][] newRows = new String[numVCCs][2];
		String[] commitAndFile = null;

		log.info("Querying for counterparts..");
		for (int i = 0; i < numVCCs; i++) {
			commitAndFile = this.vccsAndFiles[i].split(",");
			String ctrparts = joinCSV(counterpartsForFile(commitAndFile[0], commitAndFile[1], maxCounterparts));
			newRows[i][0] = this.vccsAndFiles[i];
			newRows[i][1] = ctrparts;
		}
		upsertNewRows(newRows);
	}

	private void upsertNewRows(String[][] rows) throws SQLException {
		String[] commitAndFile = null;
		String upQuery = "INSERT INTO Counterparts (Commit,Filepath,Ctrparts)"
				+ " VALUES (?,?,?) ON DUPLICATE KEY UPDATE Ctrparts = ?;";

		Connection conn = db.getConnection();
		PreparedStatement ps = conn.prepareStatement(upQuery);
		for (String[] row : rows) {
			commitAndFile = row[0].split(",");
			ps.setString(1, commitAndFile[0]);
			ps.setString(2, commitAndFile[1]);
			ps.setString(3, row[1]);
			ps.setString(4, row[1]);
			ps.addBatch();
		}

		log.info("Executing upsert..");
		ps.executeBatch();
		conn.close();
	}

	private String joinCSV(String[] arr) {
		String result = "";
		for (int i = 0; i < arr.length - 1; i++)
			result += arr[i] + ",";
		result += arr[arr.length - 1];
		return result;
	}

	private String[] getOkVCCsAndVulnFile() throws Exception {
		ArrayList<String> okVCCs = new ArrayList<String>();
		CSVReader csvIn = null;
		csvIn = new CSVReader(new FileReader(vulnIntroCSV));
		String[] line = null;
		String[] columns = csvIn.readNext();

		List<String> aslist = Arrays.asList(columns);
		int okColumn = aslist.indexOf("Verified (Alberto)");
		int commitColumn = aslist.indexOf("Git Introduced");
		int fileColumn = aslist.indexOf("Vulnerable File");

		while ((line = csvIn.readNext()) != null) {
			String lineAndFile = line[commitColumn] + "," + line[fileColumn];
			if (line[okColumn].equals("OK") && !line[commitColumn].equals("N/A") && !okVCCs.contains(lineAndFile))
				okVCCs.add(lineAndFile);
		}
		csvIn.close();

		String[] okVCCsArray = new String[okVCCs.size()];
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

	private boolean isVCC(String commit) {
		for (String commitAndFile : this.vccsAndFiles)
			if (commitAndFile.split(",")[0].equals(commit))
				return true;
		return false;
	}

	// precondition: `commit` is an OK'd VCC in this.vccsAndFiles
	private String getFileForCommit(String commit) {
		for (String commitAndFile : this.vccsAndFiles)
			if (commitAndFile.split(",")[0].equals(commit))
				return commitAndFile.split(",")[1];
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