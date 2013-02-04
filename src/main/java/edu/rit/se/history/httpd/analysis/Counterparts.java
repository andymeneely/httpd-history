package edu.rit.se.history.httpd.analysis;

import java.io.FileReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.Random;

import org.apache.log4j.Logger;
import org.chaoticbits.devactivity.DBUtil;

import au.com.bytecode.opencsv.CSVReader;

public class Counterparts {
	private static Logger log = Logger.getLogger(Counterparts.class);
	private DBUtil db = null;
	private Properties props = null;
	private int randSeed = 0;
	
	public Counterparts( DBUtil dbUtil, Properties props ) {
		this.db = dbUtil;
		this.props = props;
		this.randSeed = new Random().nextInt();
	}
	
	public void generate( int seed ) throws SQLException {
		this.randSeed = seed;
		generate();
	}
	
	public void generate() throws SQLException {
		int defaultMaxCtrparts = 5;
		String[] vccs = getOkVCCsAndVulnFile();
		int numVCCs = vccs.length;
		String[][] newRows = new String[numVCCs][2];
		
		for ( int i = 0; i < numVCCs; i++ ) {
			String[] commitAndFile = vccs[i].split(",");
			log.info( commitAndFile[0] );
			String ctrparts = joinCSV(
					counterpartsForFile(commitAndFile[0], commitAndFile[1],
							defaultMaxCtrparts ) );
			newRows[i][0] = vccs[i];
			newRows[i][1] = ctrparts;
		}
		upsertNewRows( newRows );
	}
	
	private void upsertNewRows( String[][] rows ) {
		// TODO: upsert each row in `rows` into a table ("counterparts"?)
		for ( String[] row : rows ) log.info( row[0] + ", " + row[1] );
	}
	
	private String joinCSV( String[] arr ) {
		String result = "";
		for ( int i = 0; i < arr.length - 1; i++ ) result += arr[i] + ",";
		result += arr[arr.length - 1];
		return result;
	}
	
	private String[] getOkVCCsAndVulnFile() {
		String pathToCSV = props.getProperty("history.cveintro.local");
		ArrayList<String> okVCCs = new ArrayList<String>();
		CSVReader csvIn = null;
		try {
			csvIn = new CSVReader( new FileReader(pathToCSV) );
			String[] line = null;
			String[] columns = csvIn.readNext();
			
			List<String> aslist = Arrays.asList(columns);
			int okColumn = aslist.indexOf("Verified (Alberto)");
			int commitColumn = aslist.indexOf("Git Introduced");
			int fileColumn = aslist.indexOf("Vulnerable File");
			
			while( (line = csvIn.readNext()) != null ) {
				String lineAndFile = line[commitColumn] +","+ line[fileColumn];
				if ( line[okColumn].equals( "OK" ) &&
						!line[commitColumn].equals("N/A") &&
						!okVCCs.contains(lineAndFile) )
					okVCCs.add( lineAndFile );
			}
			csvIn.close();
		} catch ( Exception e ) {
			log.error( String.format("Problem reading local CSV file: %s",
					pathToCSV ) );
		}
		
		String[] okVCCsArray = new String[okVCCs.size()];
		okVCCs.toArray( okVCCsArray );
		return okVCCsArray;
	}
	
	private String[] counterpartsForFile( String originalCommit, String file,
			int numOfCounterparts ) throws SQLException {
		String selectClause = "SELECT Commit FROM gitlog";
		String whereClause = String.format( "WHERE body LIKE '%%%s%%'", file );
		String query = String.format( "%s %s;", selectClause, whereClause );
		
		Connection conn = db.getConnection();
		PreparedStatement prep = conn.prepareStatement( query );
		ResultSet results = prep.executeQuery();
		
		results.last();
		int numRows = results.getRow() - 1, index = 0;
		results.beforeFirst();
		
		String[] counterparts = new String[numRows];
		String commit = null;
		
		while( results.next() ) {
			commit = results.getString("Commit");
			if ( !commit.equals( originalCommit ) ) {
				counterparts[index] = commit;
				index++;
			}
		}
		conn.close();
		
		numOfCounterparts = numOfCounterparts > numRows ?
				numRows : numOfCounterparts;
		return selectNRandom( counterparts, numOfCounterparts );
	}
	
	private String[] selectNRandom( String[] arr, int num ) {
		String[] result = new String[num];
		result = selNRandRec( new ArrayList<String>(Arrays.asList(arr)), num,
				new Random( this.randSeed ) ).toArray(result);
		return result;
	}
	
	private ArrayList<String> selNRandRec( ArrayList<String> arr, int num,
			Random r ) {
		if ( num == 0 ) return new ArrayList<String>();
		int randIndex = r.nextInt(arr.size());
		String element = arr.get(randIndex);
		arr.remove(randIndex);
		
		ArrayList<String> result = selNRandRec( arr, num - 1, r );
		result.add(element);
		return result;
	}
}
