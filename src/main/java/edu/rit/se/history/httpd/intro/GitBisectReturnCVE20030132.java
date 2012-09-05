package edu.rit.se.history.httpd.intro;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * CVE-2003-0132: server/protocol.c
 * 
 * Fix commit: 084e3181b9aaac1b73e7827f8a9c77b148de947c
 * 
 * Origin commit: deb7ee64ad5e4dccbc2f4a5241690782df58d241
 * 
 * <pre>
 *  git bisect start 084e3181b9aaac1b73e7827f8a9c77b148de947c^ deb7ee64ad5e4dccbc2f4a5241690782df58d241 -- server/protocol.c
 *  git bisect run java -cp ../httpd-history/src/main/java/ edu.rit.se.history.httpd.intro.GitBisectReturnCVE20030132
 * </pre>
 * 
 * @author Andy Meneely
 * 
 */
public class GitBisectReturnCVE20030132 {

	private static final int GOOD_RETURN_CODE = 0;
	private static final int BAD_RETURN_CODE = 1;
	private static final int SKIP_RETURN_CODE = 125;

	private static final String CVE = "CVE-2003-0132";
	private static final String FILE = "server/protocol.c";

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
		 * 
		 */
		/*
		 * This one is tough - tons of deletions means there were probably tons of tweaks along the way But,
		 * the fix involved removing the entire algorithm and rewriting it. So, the mistake must have been
		 * the algorithm itself (unless something else made this algorithm obsolete, but I don't know how to
		 * handle that case). So, the only piece of text that the writing must have had from the beginning
		 * was the comment.
		 * 
		 * So we are bisecting only the comment.
		 */
		if (has(stringBuffer, "" + "/* We likely aborted early before reading anything or we read no" + //
				"* data.  Technically, this might be success condition.  But," + //
				"* probably means something is horribly wrong.  For now, we'll" + //
				"* treat this as APR_SUCCESS, but it may be worth re-examining." + //
				"*/" + //
				"if (bytes_handled == 0) {" + //
				"") //
				&& has(stringBuffer, "/* If we didn't get a full line of input, try again. */")
				&& has(stringBuffer, "/* If we're doing the allocations for them, we have to" + //
						"* give ourselves a NULL and copy it on return." + //
						"")) {
			isVulnerable = true;
		} else {
			isVulnerable = false; // no such context is found, must have pre-dated the vulnerability
		}
		return isVulnerable;
	}

	private static boolean has(StringBuffer stringBuffer, String str) {
		return stringBuffer.indexOf(str) > 0;
	}
}
