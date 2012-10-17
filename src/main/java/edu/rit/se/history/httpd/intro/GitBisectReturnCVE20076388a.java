package edu.rit.se.history.httpd.intro;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * CVE-2007-6388: modules/generators/mod_status.c
 * 
 * Fix commit: 0d32dc95744b81c234b76ae54d199c7d4943839f
 * 
 * Origin commit: 5d855a48777529f38b148c19c021a01685677f79
 * 
 * <pre>
 *  git bisect start 0d32dc95744b81c234b76ae54d199c7d4943839f^ 5d855a48777529f38b148c19c021a01685677f79^ -- modules/generators/mod_status.c
 *  git bisect run java -cp ../httpd-history/src/main/java/ edu.rit.se.history.httpd.intro.GitBisectReturnCVE20076388a
 * </pre>
 * 
 * @author Alberto Rodriguez
 * 
 */
public class GitBisectReturnCVE20076388a {

	private static final int GOOD_RETURN_CODE = 0;
	private static final int BAD_RETURN_CODE = 1;
	private static final int SKIP_RETURN_CODE = 125;

	private static final String CVE = "CVE-2007-6388";
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
		if (!has(sb, "#include \"apr_strings.h\"")
		 && !has(sb, "case STAT_OPT_REFRESH: {")
		 && !has(sb, "apr_size_t len = strlen(status_options[i].form_data_str);")
		 &&  has(sb, "switch (status_options[i].id) {")) {
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
