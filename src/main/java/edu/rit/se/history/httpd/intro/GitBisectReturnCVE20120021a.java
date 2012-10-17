package edu.rit.se.history.httpd.intro;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * CVE-2012-0021: modules/loggers/mod_log_config.c
 * 
 * Fix commit: 4a1f888e0a2c478db5c97ffd3670058d28a42b0b
 * 
 * Origin commit: 5d855a48777529f38b148c19c021a01685677f79
 * 
 * <pre>
 *  git bisect start 4a1f888e0a2c478db5c97ffd3670058d28a42b0b^ 5d855a48777529f38b148c19c021a01685677f79^ -- modules/loggers/mod_log_config.c
 *  git bisect run java -cp ../httpd-history/src/main/java/ edu.rit.se.history.httpd.intro.GitBisectReturnCVE20120021a
 * </pre>
 * 
 * @author Alberto Rodriguez
 * 
 */
public class GitBisectReturnCVE20120021a {

	private static final int GOOD_RETURN_CODE = 0;
	private static final int BAD_RETURN_CODE = 1;
	private static final int SKIP_RETURN_CODE = 125;

	private static final String CVE = "CVE-2012-0021";
	private static final String FILE = "modules/loggers/mod_log_config.c";

	public static void main(String[] args) {
		if (args.length > 0) {
			System.out.println("No arguments allowed to this script!");
//			System.exit(SKIP_RETURN_CODE);
		}
		System.out.println("===Bisect check for " + CVE + ", " + FILE + "===");
		try {
			if (isVulnerable(FILE)) {
				System.out.println("===VULNERABLE===");
				System.exit(BAD_RETURN_CODE); // vulnerable --> commit was "bad"
												// --> abnormal termination
			} else {
				System.out.println("===NEUTRAL===");
				System.exit(GOOD_RETURN_CODE); // neutral --> commit was "good"
												// --> normal termination
			}
		} catch (IOException e) {
			System.out.println("===IOException! See stack trace below===");
			System.out.println(new java.io.File(FILE).getAbsolutePath());			
			e.printStackTrace();
			System.exit(SKIP_RETURN_CODE);
		}
	}

	/**
	 * 
	 * @param fileName
	 * @return boolean good or bad commit
	 * @throws IOException
	 */
	public static boolean isVulnerable(String fileName) throws IOException {
		boolean isVulnerable = false;
		// Open the file that is the first
		// command line parameter
		FileInputStream fstream = new FileInputStream(fileName);
		// Get the object of DataInputStream
		DataInputStream in = new DataInputStream(fstream);
		BufferedReader br = new BufferedReader(new InputStreamReader(in));
		String strLine;
		StringBuffer sb = new StringBuffer();
		// Read file line by line, removing newlines
		while ((strLine = br.readLine()) != null) {
			sb.append(strLine.trim());
		}
		// Close the input stream
		in.close();
		/**
		 * if the file contains this code, then it's vulnerable
		 * 
		 */
		if (!has(sb, "if (name) {")
		 &&  has(sb, "char *name = apr_strtok(cookie, \"=\", &last2);")) {
			isVulnerable = true;
		} else {
			isVulnerable = false; // no such context is found, must have
									// pre-dated the vulnerability
		}
		return isVulnerable;
	}

	private static boolean has(StringBuffer stringBuffer, String str) {
		boolean has = stringBuffer.indexOf(str) > 0;
		if (!has)
			System.out.println("\tContext not found: " + str);
		return has;
	}
}
