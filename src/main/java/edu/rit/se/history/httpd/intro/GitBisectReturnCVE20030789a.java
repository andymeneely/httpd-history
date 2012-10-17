package edu.rit.se.history.httpd.intro;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * CVE-2003-0789: modules/generators/mod_cgid.c
 * 
 * Fix commit: 20784b13aa2fcce802cd87992a9fd59204c5baec
 * 
 * Origin commit: cb9d7eb95abe80aab8985acc60043b2da2b55e2c
 * 
 * <pre>
 *  git bisect start 20784b13aa2fcce802cd87992a9fd59204c5baec^ cb9d7eb95abe80aab8985acc60043b2da2b55e2c^ -- modules/generators/mod_cgid.c
 *  git bisect run java -cp ../httpd-history/src/main/java/ edu.rit.se.history.httpd.intro.GitBisectReturnCVE20030789a
 * </pre>
 * 
 * @author Alberto Rodriguez
 * 
 */
public class GitBisectReturnCVE20030789a {

	private static final int GOOD_RETURN_CODE = 0;
	private static final int BAD_RETURN_CODE = 1;
	private static final int SKIP_RETURN_CODE = 125;

	private static final String CVE = "CVE-2003-0789";
	private static final String FILE = "modules/generators/mod_cgid.c";

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
		if (has(sb, "apr_os_pipe_put(&tempsock, &sd, r->pool);")
			&& !has(sb, "apr_os_pipe_put_ex(&tempsock, &sd, 1, r->pool);")) {
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
