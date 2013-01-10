package edu.rit.se.history.httpd.intro;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * CVE-2010-1623
 * Vulnerable file: modules/filters/mod_reqtimeout.c 
 * Fix commit: ceadbb2ba891b0822f5f6a493a80e4adda23431d
 * 
 * <pre>
 *  ./tryBisect.sh 20101623 modules/filters/mod_reqtimeout.c ceadbb2ba891b0822f5f6a493a80e4adda23431d GitBisectReturnCVE20101623a
 * </pre>
 *
 * Result: ed10ef0f7f93cba9f05887f0343895a2cd05964e is the first bad commit
 *
 * @author Alberto Rodriguez
 * 
 */
public class GitBisectReturnCVE20101623a {

	private static final int GOOD_RETURN_CODE = 0;
	private static final int BAD_RETURN_CODE = 1;
	private static final int SKIP_RETURN_CODE = 125;

	private static final String CVE = "CVE-2010-1623";
	private static final String FILE = "modules/filters/mod_reqtimeout.c";

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
				"ccfg->tmpbb = apr_brigade_create(f->c->pool, f->c->bucket_alloc);",
                "APR_BRIGADE_CONCAT(ccfg->tmpbb, bb);"};
        // mustNotHave is usually the fix code.
//		String[] mustNotHave = { 
//				"apr_psprintf(r->pool,",
//                "\"<pre>\\n%.*s\\n</pre>\\n\",",
//                "field_name_len(field),",
//                "ap_escape_html(r->pool, field)));",
//                "apr_psprintf(r->pool,",
//                "\"<pre>\\n%.*s\\n</pre>\\n\",",
//                "field_name_len(last_field),",
//                "ap_escape_html(r->pool, last_field)));"};

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
