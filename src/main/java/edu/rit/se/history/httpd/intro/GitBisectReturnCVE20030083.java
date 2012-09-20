package edu.rit.se.history.httpd.intro;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * CVE-2003-0083: server/gen_test_char.c
 * 
 * Fix commit: 7b1d2e3f234bb60948da4144e7bb4f7484e00511
 * 
 * Origin commit: 5430f8800f5fffd57e7421dee0ac9de8ca4f9573
 * 
 * <pre>
 *  git bisect start 7b1d2e3f234bb60948da4144e7bb4f7484e00511^ 5430f8800f5fffd57e7421dee0ac9de8ca4f9573 -- server/gen_test_char.c
 *  git bisect run java -cp ../httpd-history/src/main/java/ edu.rit.se.history.httpd.intro.GitBisectReturnCVE20030083
 * </pre>
 * 
 * @author Andy Meneely
 * 
 */
public class GitBisectReturnCVE20030083 {

	private static final int GOOD_RETURN_CODE = 0;
	private static final int BAD_RETURN_CODE = 1;
	private static final int SKIP_RETURN_CODE = 125;

	private static final String CVE = "CVE-2003-0083";
	private static final String FILE = "server/gen_test_char.c";

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
				"/* these are the \"tspecials\" from RFC2068 */" + // context
				"if (apr_iscntrl(c) || strchr(\" \\t()<>@,;:\\\\/[]?={}\", c)) {" + // context
				"flags |= T_HTTP_TOKEN_STOP;" + // context
				"}" + // context
				"printf(\"%u%c\", flags, (c < 255) ? ',' : ' ');" + // context
				"}" + // context (vulnerability was omitting code from before that last line)
				"") //
				|| has(sb, "" + //
						"/* these are the \"tspecials\" from RFC2068 */" + // context
						"if (ap_iscntrl(c) || strchr(\" \\t()<>@,;:\\\\/[]?={}\", c)) {" + // context
						"flags |= T_HTTP_TOKEN_STOP;" + // context
						"}" + // context
						"printf(\"%u%c\", flags, (c < 255) ? ',' : ' ');" + // context
						"}" + // context (vulnerability was omitting code from before that last line)
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
