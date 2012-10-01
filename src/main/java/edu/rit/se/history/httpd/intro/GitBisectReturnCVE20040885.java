package edu.rit.se.history.httpd.intro;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * CVE-2004-0885: modules/ssl/ssl_engine_init.c
 * 
 * Fix commit: d107d7d6af0fa59166ea547a09a3925fb74e498e
 * 
 * Origin commit: 7e5c35962b46a8492323abedce2614f7f280a35a
 * 
 * <pre>
 *  git bisect start d107d7d6af0fa59166ea547a09a3925fb74e498e^ 7e5c35962b46a8492323abedce2614f7f280a35a^ -- modules/ssl/ssl_engine_init.c
 *  git bisect run java -cp ../httpd-history/src/main/java/ edu.rit.se.history.httpd.intro.GitBisectReturnCVE20040885
 * </pre>
 * 
 * @author Andy Meneely
 * 
 */
public class GitBisectReturnCVE20040885 {

	private static final int GOOD_RETURN_CODE = 0;
	private static final int BAD_RETURN_CODE = 1;
	private static final int SKIP_RETURN_CODE = 125;

	private static final String CVE = "CVE-2004-0885";
	private static final String FILE = "modules/ssl/ssl_engine_init.c";

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
		if (has(sb, //
				"/*" + // context
						"* Configure additional context ingredients" + // context
						"*/" + // context
						"SSL_CTX_set_options(ctx, SSL_OP_SINGLE_DH_USE);" + // context
						"}" + // vulnerability fix was before here
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
