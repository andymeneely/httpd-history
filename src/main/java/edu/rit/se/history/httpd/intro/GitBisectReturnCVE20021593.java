package edu.rit.se.history.httpd.intro;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * CVE-2002-1593: modules/dav/main/mod_dav.c
 * 
 * Fix commit: 618f31fcd3ab951204eeb98661c37ed1e765e316
 * 
 * Origin commit: 5430f8800f5fffd57e7421dee0ac9de8ca4f9573
 * 
 * <pre>
 *  git bisect start f56bd9c539367865c061d7b077a07fff6c615c9c^ 5430f8800f5fffd57e7421dee0ac9de8ca4f9573 -- modules/dav/main/mod_dav.c
 *  git bisect run java -cp ../httpd-history/src/main/java/ edu.rit.se.history.httpd.intro.GitBisectReturnCVE20021593
 * </pre>
 * 
 * @author Andy Meneely
 * 
 */
public class GitBisectReturnCVE20021593 {

	private static final int GOOD_RETURN_CODE = 0;
	private static final int BAD_RETURN_CODE = 1;
	private static final int SKIP_RETURN_CODE = 125;

	private static final String CVE = "CVE-2002-1593";
	private static final String FILE = "modules/dav/main/mod_dav.c";

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
		StringBuffer stringBuffer = new StringBuffer();
		// Read file line by line, removing newlines
		while ((strLine = br.readLine()) != null) {
			stringBuffer.append(strLine.trim());
		}
		// Close the input stream
		in.close();
		/**
		 * if the file contains this code, then it's vulnerable
		 */
		if (has(stringBuffer, "" + //
				"return dav_handle_err(r, err, NULL);" + // context
				"/* if unrecognized option, pass to versioning provider */" + // context
				"if (!core_option) {" + // wrong
				"if ((err = (*vsn_hooks->get_option)(resource, elem, &body))" + // context
				"!= NULL) {" + //
				"return dav_handle_err(r, err, NULL);" + //
				"" //
		)) {
			isVulnerable = true;
		} else {
			isVulnerable = false; // no such context is found
		}
		return isVulnerable;
	}

	private static boolean has(StringBuffer stringBuffer, String str) {
		return stringBuffer.indexOf(str) > 0;
	}
}
