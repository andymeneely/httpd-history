package edu.rit.se.history.httpd.intro;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * CVE-2005-2088: modules/proxy/mod_proxy_http.c (formerly modules/proxy/proxy_http.c)
 * 
 * Fix commit: 01fce1eded3d0e78951125b4f26ff9e337907543
 * 
 * Origin commit: 5d855a48777529f38b148c19c021a01685677f79
 * 
 * Note: since there was a rename prior to fixing, we are doing a full-on checkout of the tree, not specific
 * to the branch
 * 
 * <pre>
 *  git bisect start d107d7d6af0fa59166ea547a09a3925fb74e498e^ 5d855a48777529f38b148c19c021a01685677f79^ 
 *  git bisect run java -cp ../httpd-history/src/main/java/ edu.rit.se.history.httpd.intro.GitBisectReturnCVE20052088
 * </pre>
 * 
 * @author Andy Meneely
 * 
 */
public class GitBisectReturnCVE20052088 {

	private static final int GOOD_RETURN_CODE = 0;
	private static final int BAD_RETURN_CODE = 1;
	private static final int SKIP_RETURN_CODE = 125;

	private static final String CVE = "CVE-2004-0885";
	private static final String FILE = " modules/proxy/mod_proxy_http.c";
	private static final String PRE_RENAME_FILE = "modules/proxy/proxy_http.c";

	public static void main(String[] args) {
		if (args.length > 0) {
			System.out.println("No arguments allowed to this script!");
			System.exit(SKIP_RETURN_CODE);
		}
		String file = FILE; // file from the fix
		if (!new File(file).exists())
			file = PRE_RENAME_FILE; // if the checkout was before the rename
		System.out.println("===Bisect check for " + CVE + ", " + FILE + "===");
		try {
			if (isVulnerable(file)) {
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
		if (has(sb, //
				"save_table);" + // context
						"}" + // context
						"/* strip connection listed hop-by-hop headers from response */" + // vuln fix was
																							// before here
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
