package edu.rit.se.history.httpd.intro;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * CVE-2002-0661: server/util.c
 * 
 * Fix commit: c45d52a8ffeb21a8eaecda00fde3bb0e9ad50454
 * 
 * Origin commit: 5430f8800f5fffd57e7421dee0ac9de8ca4f9573
 * 
 * <pre>
 *  git bisect start c45d52a8ffeb21a8eaecda00fde3bb0e9ad50454^ 5430f8800f5fffd57e7421dee0ac9de8ca4f9573^ -- server/util.c
 *  git bisect run java -cp ../httpd-history/src/main/java/ edu.rit.se.history.httpd.intro.GitBisectReturnCVE20020661
 * </pre>
 * 
 * @author Andy Meneely
 * 
 */
public class GitBisectReturnCVE20020661 {

	private static final int GOOD_RETURN_CODE = 0;
	private static final int BAD_RETURN_CODE = 1;
	private static final int SKIP_RETURN_CODE = 125;

	private static final String CVE = "CVE-2002-0661";
	private static final String FILE = "server/util.c";

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
		if (has(sb, "while (name[l] != '\0') {" + // context
				"if (name[l] == '.' && name[l + 1] == '/' && (l == 0 || name[l - 1] == '/'))")// wrong code
				|| has(sb, "w--;" + // context
						"else if (w > 1 && name[w - 1] == '.' && name[w - 2] == '/')") // wrong code
				|| has(sb, "while (name[l] != '\0') {" + // context
						"if (name[l] == '.' && name[l + 1] == '.' && name[l + 2] == '/' &&" + //
						"(l == 0 || name[l - 1] == '/')) {")// wrong code
				|| has(sb, "if (l >= 0) {" + // context
						"while (l >= 0 && name[l] != '/')")// wrong code
				|| has(sb, "name[0] = '\0';" + // context
						"else if (l > 2 && name[l - 1] == '.' && name[l - 2] == '.' && name[l - 3] == '/') {")// wrong
				|| has(sb, "y += 2;" + // context
						"if (*x == '/' || *x == '\\0')")// wrong ocde
		) {
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
