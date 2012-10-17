package edu.rit.se.history.httpd.intro;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * CVE-2006-3747
 * Vulnerable file: modules/mappers/mod_rewrite.c 
 * Fix commit: e2a9028db5b079e01eb6b9ac06299bd06dd59574
 * 
 * <pre>
 *  ./tryBisect.sh 20063747 modules/mappers/mod_rewrite.c e2a9028db5b079e01eb6b9ac06299bd06dd59574 GitBisectReturnCVE20063747a
 * </pre>
 * 
 * @author Alberto Rodriguez
 * 
 */
public class GitBisectReturnCVE20063747a {

	private static final int GOOD_RETURN_CODE = 0;
	private static final int BAD_RETURN_CODE = 1;
	private static final int SKIP_RETURN_CODE = 125;

	private static final String CVE = "CVE-2006-3747";
	private static final String FILE = "modules/mappers/mod_rewrite.c";

	public static void main(String[] args) {
		if (args.length > 0) {
			System.out.println("No arguments allowed to this script!");
			// System.exit(SKIP_RETURN_CODE);
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
		 * if the file contains mustHave and doesn't contain mustNotHave, then
		 * it's vulnerable
		 */
		String[] mustHave = { 
				"token[0] = cp = apr_pstrdup(p, cp);",
				"while (*cp && c < 5) {",
				"if (*cp == '?') {"};
		String[] mustNotHave = { 
				"while (*cp && c < 4) {"};

		if (hasAll(sb, mustHave) && hasNone(sb, mustNotHave)) {
			isVulnerable = true;
		} else {
			isVulnerable = false; // no such context is found, must have
									// pre-dated the vulnerability
		}
		return isVulnerable;
	}

	private static boolean hasNone(StringBuffer sb, String[] mustNotHave) {
		for (String text : mustNotHave) {
			if (has(sb, text)) {
				return false;
			}
		}
		return true;
	}

	private static boolean hasAll(StringBuffer sb, String[] mustHave) {
		for (String text : mustHave) {
			if (!has(sb, text)) {
				return false;
			}
		}
		return true;
	}

	private static boolean has(StringBuffer stringBuffer, String str) {
		boolean has = stringBuffer.indexOf(str) > 0;
		if (!has)
			System.out.println("\tContext not found: " + str);
		return has;
	}
}
