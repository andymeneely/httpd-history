package edu.rit.se.history.httpd.intro;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * CVE-2009-1191: modules/proxy/mod_proxy_ajp.c
 * 
 * Fix commit: eac933c83d00250b8f19e0239f74404743348c65
 * 
 * Origin commit: f0c9a1710063cc875a88fdf03e04227c522da28b
 * 
 * <pre>
 *  git bisect start eac933c83d00250b8f19e0239f74404743348c65^ f0c9a1710063cc875a88fdf03e04227c522da28b^ -- modules/proxy/mod_proxy_ajp.c
 *  git bisect run java -cp ../httpd-history/src/main/java/ edu.rit.se.history.httpd.intro.GitBisectReturnCVE20091191
 * </pre>
 * 
 * @author Andy Meneely
 * 
 */
public class GitBisectReturnCVE20091191 {

	private static final int GOOD_RETURN_CODE = 0;
	private static final int BAD_RETURN_CODE = 1;
	private static final int SKIP_RETURN_CODE = 125;

	private static final String CVE = "CVE-2009-1191";
	private static final String FILE = "modules/proxy/mod_proxy_ajp.c";

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
		if (has(sb, "status = ajp_send_data_msg(conn->sock, msg, 0);" + //
				"if (status != APR_SUCCESS) {" + //
				"/* We had a failure: Close connection to backend */" + //
				"conn->close++;" + //
				"ap_log_error(APLOG_MARK, APLOG_ERR, status, r->server," + //
				"\"proxy: send failed to %pI (%s)\"," + //
				"conn->worker->cp->addr," + //
				"conn->worker->hostname);" + //
				"return HTTP_INTERNAL_SERVER_ERROR;" + //
				"}" + //
				"else {" + //
				"/* Client send zero bytes with C-L > 0" + //
				"*/" + //
				"return HTTP_BAD_REQUEST;" + //
				"}" + //
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
