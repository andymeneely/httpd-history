package edu.rit.se.history.httpd.intro;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * CVE-2002-1156: modules/dav/main/mod_dav.c
 * 
 * Fix commit: a07d0bebc57562814b5949c964a847a2e56438bc
 * 
 * Origin commit: 92291b5ed38235ba0667769412f86e16cc1b3076
 * 
 * <pre>
 *  git bisect start a07d0bebc57562814b5949c964a847a2e56438bc^ 92291b5ed38235ba0667769412f86e16cc1b3076
 *  git bisect run java -cp ../httpd-history/src/main/java/ edu.rit.se.history.httpd.intro.GitBisectReturnCVE20021156
 * </pre>
 * 
 * @author Andy Meneely
 * 
 */
public class GitBisectReturnCVE20021156 {

	private static final int GOOD_RETURN_CODE = 0;
	private static final int BAD_RETURN_CODE = 1;
	private static final int SKIP_RETURN_CODE = 125;

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		if (args.length > 0) {
			System.out.println("No arguments allowed to this script!");
			System.exit(SKIP_RETURN_CODE);
		}
		System.out.println("===Bisect check for CVE-2002-1156===");
		try {
			if (isVulnerable("modules/dav/main/mod_dav.c")) {
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
		if (has(stringBuffer, //
				/* one line immediately after another - the fix is adding an if */
				"}" + /* fix has an if-statement right here */
				"/* We are going to be handling the response for this resource. */" //
						+ "r->handler = DAV_HANDLER_NAME;" //
						+ "return OK;")) {
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
