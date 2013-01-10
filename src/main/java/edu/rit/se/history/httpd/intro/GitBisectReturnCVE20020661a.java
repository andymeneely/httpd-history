package edu.rit.se.history.httpd.intro;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * CVE-2002-0661
 * Vulnerable file: server/util.c
 * Fix commit: c45d52a8ffeb21a8eaecda00fde3bb0e9ad50454
 * 
 * <pre>
 *  ./tryBisect.sh 20020661 server/util.c c45d52a8ffeb21a8eaecda00fde3bb0e9ad50454 GitBisectReturnCVE20020661a
 * </pre>
 *
 * Result: X is the first bad commit
 *
 * @author Alberto Rodriguez
 * 
 */
public class GitBisectReturnCVE20020661a {

	private static final int GOOD_RETURN_CODE = 0;
	private static final int BAD_RETURN_CODE = 1;
	private static final int SKIP_RETURN_CODE = 125;

	private static final String CVE = "CVE-2002-0661";
	private static final String FILE = "server/util.c";

	public static void main(String[] args) {
		if (args.length > 0) {
			System.out.println("No arguments required to this script!");
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
         // mustHave is usually the vulnerable code and its surrounding context.
		String[] mustHave = { 
				"while (name[l] != '\\0') {",
                "(name[l] == '.' && name[l + 1] == '/' && (l == 0 || name[l - 1] == '/'))",
                "l += 2;",
                "if (w == 1 && name[0] == '.')",
                "w--;",
                "else if (w > 1 && name[w - 1] == '.' && name[w - 2] == '/')",
                "while (name[l] != '\\0') {",
                "if (name[l] == '.' && name[l + 1] == '.' && name[l + 2] == '/' &&",
                "(l == 0 || name[l - 1] == '/')) {",
                "register int m = l + 3, n;",
                "while (l >= 0 && name[l] != '/')",
                "name[0] = '\\0';",
                "else if (l > 2 && name[l - 1] == '.' && name[l - 2] == '.' && name[l - 3] == '/') {",
                "if (l >= 0) {",
                "while (l >= 0 && name[l] != '/')",
                "y += 2;",
                "if (*x == '/' || *x == '\\0')"};
        // mustNotHave is usually the fix code.
//		String[] mustNotHave = { 
//				"#####"};

		/**
		 * if the file contains mustHave and does not contain mustNotHave, then
		 * it is vulnerable
		 */
		//if (hasAll(sb, mustHave) && hasNone(sb, mustNotHave)) {
        //if (hasAll(sb, mustHave) && !hasAll(sb, mustNotHave)) {
        if (hasAll(sb, mustHave)) {
			return true; // It is vulnerable: 
						 // Contains some context from latest bad commit and doesn't contain the fix.
		} else {
			return false; // It is not vulnerable: 
			              // Either contains the fix or doesn't contain context from the latest bad commit.
		}
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
