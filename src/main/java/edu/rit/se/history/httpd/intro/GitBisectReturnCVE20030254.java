package edu.rit.se.history.httpd.intro;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * CVE-2003-0542: modules/proxy/proxy_ftp.c
 * 
 * Fix commit: 46e359cd54f16e830ea9c8830eaa4773f420f5dc
 * 
 * Origin commit: 5d855a48777529f38b148c19c021a01685677f79
 * 
 * <pre>
 *  git bisect start 46e359cd54f16e830ea9c8830eaa4773f420f5dc^ 5d855a48777529f38b148c19c021a01685677f79^ -- modules/proxy/proxy_ftp.c 
 *  git bisect run java -cp ../httpd-history/src/main/java/ edu.rit.se.history.httpd.intro.GitBisectReturnCVE20030254
 * </pre>
 * 
 * @author Andy Meneely
 * 
 */
public class GitBisectReturnCVE20030254 {

	private static final int GOOD_RETURN_CODE = 0;
	private static final int BAD_RETURN_CODE = 1;
	private static final int SKIP_RETURN_CODE = 125;

	private static final String CVE = "CVE-2003-0542";
	private static final String FILE = "modules/proxy/proxy_ftp.c";

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
		if (has(sb, "" + //
				"if ((rv = apr_socket_create(&sock, connect_addr->family, SOCK_STREAM, r->pool)) != APR_SUCCESS) {" + // context
				"ap_log_rerror(APLOG_MARK, APLOG_ERR, rv, r," + // context
				"\"proxy: FTP: error creating socket\");" + // context
				"continue;" + // vulnerability was before here
				"")// wrong ocde
				|| has(sb, "#ifndef _OSD_POSIX              /* BS2000 has this option \"always on\" */" + //
						"ap_log_rerror(APLOG_MARK, APLOG_ERR, rv, r," + //
						"\"proxy: FTP: error setting reuseaddr option: apr_socket_opt_set(APR_SO_REUSEADDR)\");" + //
						"continue;" + //
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
