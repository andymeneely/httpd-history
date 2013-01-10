package edu.rit.se.history.httpd.intro;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;

/**
 * CVE-20076422
 * Vulnerable file: modules/dav/main/util.c
 * Fix commit: //___FIX___
 * 
 * <pre>
 *  ./tryBisect.sh 20076422 modules/dav/main/util.c //___FIX___ GitBisectReturnCVE20076422modules_dav_main_util_c
 * </pre>
 *
 * Result: _ is the first bad commit
 *
 * @author Alberto Rodriguez
 * 
 */
public class GitBisectReturnCVE20076422modules_dav_main_util_c {

	private static final int GOOD_RETURN_CODE = 0;
	private static final int BAD_RETURN_CODE = 1;
	private static final int SKIP_RETURN_CODE = 125;

	// Context from vulnerable version.
	private static List<String> oldBlocks;

	// Context from fixed version.
	private static List<String> newBlocks;
    
    private static final String CVE = "CVE-20076422";
	private static final String FILE = "modules/dav/main/util.c";

	public static void main(String[] args) {
		if (args.length > 0) {
			System.out.println("No arguments required to this script!");
		}

        newBlocks = Arrays.asList(
            "static int dav_meets_conditions(request_rec *r, int resource_state)",
            "const char *if_match, *if_none_match;",
            "int retVal;",
            "if ((if_match = apr_table_get(r->headers_in, \"If-Match\")) != NULL) {",
            "if(if_match[0] == '*' && resource_state != DAV_RESOURCE_EXISTS)",
            "return HTTP_PRECONDITION_FAILED;",
            "retVal = ap_meets_conditions(r);",
            "if(retVal == HTTP_PRECONDITION_FAILED) {",
            "if((if_none_match =",
            "apr_table_get(r->headers_in, \"If-None-Match\")) != NULL) {",
            "if(if_none_match[0] == '*' && resource_state != DAV_RESOURCE_EXISTS)",
            "return OK;",
            "return retVal;",
            "int resource_state;",
            "if ((err = (*resource->hooks->set_headers)(r, resource)) != NULL) {",
            "return dav_push_error(r->pool, err->status, 0,",
            "\"Unable to set up HTTP headers.\",",
            "err);",
            "resource_state = dav_get_resource_state(r, resource);",
            "if ((result = dav_meets_conditions(r, resource_state)) != OK) {");

        oldBlocks = Arrays.asList(
            "if ((result = ap_meets_conditions(r)) != OK) {");


		File vulnerableFile = new File(FILE);

		System.out.println("===Bisect check for " + CVE + ", " + FILE + "===");
		try {
			if (isVulnerable(vulnerableFile)) {
				System.out.println("===VULNERABLE===");
				System.exit(BAD_RETURN_CODE); // vulnerable --> commit was "bad"
												// --> abnormal termination
			} else {
				System.out.println("===NEUTRAL===");
				System.exit(GOOD_RETURN_CODE); // neutral --> commit was "good"
												// --> normal termination
			}
		} catch (IOException e) {
			System.err.println("===IOException! See stack trace below===");
			System.err.println("Vulnerable file: "
					+ vulnerableFile.getAbsolutePath());
			e.printStackTrace();
			System.exit(SKIP_RETURN_CODE);
		}
	}

	/**
	 * 
	 * @param file
	 * @return boolean good or bad commit
	 * @throws IOException
	 */
	private static boolean isVulnerable(File file) throws IOException {
		StringBuffer sb = readFile(file);

		String fileContent = escapeChars(sb.toString());

		if (hasAll(fileContent, oldBlocks) && hasNone(fileContent, newBlocks)) {
			return true; // It is vulnerable:
							// Contains some context from latest bad commit and
							// doesn't contain the fix.
		} else {
			return false; // It is not vulnerable:
							// Either contains the fix or doesn't contain
							// context from the latest bad commit.
		}
	}
	
	private static String escapeChars(String text) {
		return text.replace("\\", "\\\\")
				   .replace("\"", "\\\"");
	}

	private static StringBuffer readFile(File fileName)
			throws FileNotFoundException, IOException {
		FileInputStream fstream = new FileInputStream(fileName);
		DataInputStream in = new DataInputStream(fstream);
		BufferedReader br = new BufferedReader(new InputStreamReader(in));
		String strLine;
		StringBuffer sb = new StringBuffer();
		while ((strLine = br.readLine()) != null) {
			sb.append(strLine.trim());
		}
		in.close();
		return sb;
	}

	private static boolean hasNone(String fileContent, List<String> mustNotHave) {
		for (String text : mustNotHave) {
			if (has(fileContent, text)) {
				return false;
			}
		}
		return true;
	}

	private static boolean hasAll(String fileContent, List<String> list) {
		for (String text : list) {
			if (!has(fileContent, text)) {
				return false;
			}
		}
		return true;
	}

	private static boolean has(String fileContent, String str) {
		boolean has = fileContent.indexOf(str) > 0;
		if (!has)
			System.out.println("\tContext not found: " + str);
		return has;
	}
}

