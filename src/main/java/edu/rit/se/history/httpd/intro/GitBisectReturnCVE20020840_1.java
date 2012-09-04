package edu.rit.se.history.httpd.intro;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * CVE-2002-0840: server/core.c
 * 
 * 
 * Fix commit: deaeb1407a553fcfe9a0c8158cad8a780c52139d
 * 
 * Origin commit: ca8088ad1e5634b34a5ac68565fcaee6e8538c78
 * 
 * @author Andy Meneely
 * 
 */
public class GitBisectReturnCVE20020840_1 {

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
		System.out.println("===Bisect check for CVE-2002-0840===");
		try {
			if (isVulnerable("server/core.c")) {
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
			stringBuffer.append(strLine);
		}
		// Close the input stream
		in.close();
		/**
		 * if the file contains this code, then it's vulnerable
		 */
		if (has(stringBuffer, "return apr_pstrcat(r->pool, prefix, \"<address>\" AP_SERVER_BASEVERSION")
				&& has(stringBuffer, "\" Server at <a href=\\\"mailto:\"")
				&& has(stringBuffer, "r->server->server_admin, \"\\\">\",")
				&& has(stringBuffer, "\"</address>\\n\", NULL);")
				&& has(stringBuffer, "\" Server at \", ap_get_server_name(r), \" Port \", sport,")) {
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
