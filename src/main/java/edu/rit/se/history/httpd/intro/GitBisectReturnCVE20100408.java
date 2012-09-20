package edu.rit.se.history.httpd.intro;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * CVE-2010-0408: modules/proxy/mod_proxy_ajp.c
 * 
 * Fix commit: 2fe8ec85fa8ef1340a61e688f3bc43c799add78e
 * 
 * Origin commit: 5dbf830701af760e37e1e2c26212c34220516d85
 * 
 * <pre>
 *  git bisect start 2fe8ec85fa8ef1340a61e688f3bc43c799add78e^ 5dbf830701af760e37e1e2c26212c34220516d85 -- modules/proxy/mod_proxy_ajp.c
 *  git bisect run java -cp ../httpd-history/src/main/java/ edu.rit.se.history.httpgit sd.intro.GitBisectReturnCVE20100408
 * </pre>
 * 
 * @author Andy Meneely
 * 
 */
public class GitBisectReturnCVE20100408 {

	private static final int GOOD_RETURN_CODE = 0;
	private static final int BAD_RETURN_CODE = 1;
	private static final int SKIP_RETURN_CODE = 125;

	private static final String CVE = "CVE-2010-0408";
	private static final String FILE = "modules/proxy/mod_proxy_ajp.c";

	public static void main(String[] args) {
		if (args.length > 0) {
			System.out.println("No arguments allowed to this script!");
			System.exit(SKIP_RETURN_CODE);
		}
		System.out.println("===Bisect check for " + CVE + ", " + FILE + "===");
		try {
			if (isVulnerable(FILE)) {
				System.out.println("===VULNERABLE===");
				System.exit(BAD_RETURN_CODE); // vulnerable --> commit was "bad" --> abnormal termination
			} else {
				System.out.println("===NEUTRAL===");
				System.exit(GOOD_RETURN_CODE); // neutral --> commit was "good" --> normal termination
			}
		} catch (IOException e) {
			System.out.println("===IOException! See stack trace below===");
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
		if (//
		has(sb, "" + //
				"if (status != APR_SUCCESS) {" + //
				"/* We had a failure: Close connection to backend */" + //
				"conn->close++;" + //
				"ap_log_error(APLOG_MARK, APLOG_DEBUG, 0, r->server," + //
				"\"proxy: ap_get_brigade failed\");" + //
				"apr_brigade_destroy(input_brigade);" + //
				"return HTTP_INTERNAL_SERVER_ERROR;" + //
				"}" + //
				"")) {
			isVulnerable = true;
		} else {
			isVulnerable = false; // no such context is found, must have pre-dated the vulnerability
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
