package edu.rit.se.history.httpd.intro;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * CVE-2007-5000: modules/mappers/mod_imagemap.c
 * 
 * Fix commit: b7881ae804aa98ca91cc47616b41fb7ece9f7c70
 * 
 * Origin commit: 9de8a693d16e5ce76ff5b7f727311e783a51f34a
 * 
 * <pre>
 *  ./tryBisect.sh 20075000 modules/mappers/mod_imagemap.c b7881ae804aa98ca91cc47616b41fb7ece9f7c70 GitBisectReturnCVE20075000a
 *  OR
 *  git bisect start b7881ae804aa98ca91cc47616b41fb7ece9f7c70^ 9de8a693d16e5ce76ff5b7f727311e783a51f34a^ -- modules/mappers/mod_imagemap.c
 *  git bisect run java -cp ../httpd-history/src/main/java/ edu.rit.se.history.httpd.intro.GitBisectReturnCVE20075000a
 * </pre>
 * 
 * @author Alberto Rodriguez
 * 
 */
public class GitBisectReturnCVE20075000a {

	private static final int GOOD_RETURN_CODE = 0;
	private static final int BAD_RETURN_CODE = 1;
	private static final int SKIP_RETURN_CODE = 125;

	private static final String CVE = "CVE-2007-5000";
	private static final String FILE = "modules/mappers/mod_imagemap.c";

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
				"ap_set_content_type(r, \"text/html\");",
				"ap_rvputs(r, DOCTYPE_HTML_3_2, \"<html><head>\\n<title>Menu for \", r->uri,",
				"ap_rvputs(r, \"<h1>Menu for \", r->uri, \"</h1>\\n<hr />\\n\\n\", NULL);"};
		String[] mustNotHave = { 
				"ap_set_content_type(r, \"text/html; charset=ISO-8859-1\");", 
				"ap_escape_html(r->pool, r->uri),"};

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
