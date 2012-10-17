package edu.rit.se.history.httpd.intro;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * CVE-2006-5752: modules/generators/mod_status.c
 * 
 * Fix commit: 6f0c8008746b58eb0a59f59501163eea2921d03d
 * 
 * Origin commit: 5d855a48777529f38b148c19c021a01685677f79
 * 
 * <pre>
 *  git bisect start 6f0c8008746b58eb0a59f59501163eea2921d03d^ 5d855a48777529f38b148c19c021a01685677f79^ -- modules/generators/mod_status.c
 *  git bisect run java -cp ../httpd-history/src/main/java/ edu.rit.se.history.httpd.intro.GitBisectReturnCVE20065752a
 * </pre>
 * 
 * @author Alberto Rodriguez
 * 
 */
public class GitBisectReturnCVE20065752a {

	private static final int GOOD_RETURN_CODE = 0;
	private static final int BAD_RETURN_CODE = 1;
	private static final int SKIP_RETURN_CODE = 125;

	private static final String CVE = "CVE-2006-5752";
	private static final String FILE = "modules/generators/mod_status.c";

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
		if (has(sb, "ap_set_content_type(r, \"text/html\");")
		 && has(sb, "ap_set_content_type(r, \"text/plain\");")
		 && has(sb, "ws_record->request),")
		 && !has(sb, "ap_set_content_type(r, \"text/html; charset=ISO-8859-1\");")
		 && !has(sb, "ap_set_content_type(r, \"text/plain; charset=ISO-8859-1\");")
		 && !has(sb, "ap_escape_logitem(r->pool,")
		 && !has(sb, "ws_record->request)));")) {
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
