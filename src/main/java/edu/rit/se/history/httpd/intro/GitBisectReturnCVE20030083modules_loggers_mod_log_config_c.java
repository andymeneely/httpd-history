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
 * CVE-20030083
 * Vulnerable file: modules/loggers/mod_log_config.c
 * Fix commit: //___FIX___
 * 
 * <pre>
 *  ./tryBisect.sh 20030083 modules/loggers/mod_log_config.c //___FIX___ GitBisectReturnCVE20030083modules_loggers_mod_log_config_c
 * </pre>
 *
 * Result: _ is the first bad commit
 *
 * @author Alberto Rodriguez
 * 
 */
public class GitBisectReturnCVE20030083modules_loggers_mod_log_config_c {

	private static final int GOOD_RETURN_CODE = 0;
	private static final int BAD_RETURN_CODE = 1;
	private static final int SKIP_RETURN_CODE = 125;

	// Context from vulnerable version.
	private static List<String> oldBlocks;

	// Context from fixed version.
	private static List<String> newBlocks;
    
    private static final String CVE = "CVE-20030083";
	private static final String FILE = "modules/loggers/mod_log_config.c";

	public static void main(String[] args) {
		if (args.length > 0) {
			System.out.println("No arguments required to this script!");
		}

        newBlocks = Arrays.asList(
            "return ap_escape_logitem(r->pool, ap_get_remote_host(r->connection,",
            "r->per_dir_config,",
            "REMOTE_NAME, NULL));",
            "return ap_escape_logitem(r->pool, ap_get_remote_logname(r));",
            "else {",
            "rvalue = ap_escape_logitem(r->pool, rvalue);",
            "return ap_escape_logitem(r->pool,",
            "(r->parsed_uri.password)",
            "? apr_pstrcat(r->pool, r->method, \" \",",
            "apr_uri_unparse(r->pool,",
            "&r->parsed_uri, 0),",
            "r->assbackwards ? NULL : \" \",",
            "r->protocol, NULL)",
            ": r->the_request);",
            "return ap_escape_logitem(r->pool, r->filename);",
            "return ap_escape_logitem(r->pool, r->uri);",
            "return ap_escape_logitem(r->pool, r->method);",
            "return ap_escape_logitem(r->pool, r->protocol);",
            "return (r->args) ? apr_pstrcat(r->pool, \"?\",",
            "ap_escape_logitem(r->pool, r->args), NULL)",
            ": \"\";",
            "return ap_escape_logitem(r->pool, apr_table_get(r->headers_in, a));",
            "return ap_escape_logitem(r->pool, cp);",
            "return ap_escape_logitem(r->pool, apr_table_get(r->err_headers_out, a));",
            "return ap_escape_logitem(r->pool, apr_table_get(r->notes, a));",
            "return ap_escape_logitem(r->pool, apr_table_get(r->subprocess_env, a));",
            "return ap_escape_logitem(r->pool, cookie);",
            "return ap_escape_logitem(r->pool, r->server->server_hostname);",
            "return ap_escape_logitem(r->pool, ap_get_server_name(r));");

        oldBlocks = Arrays.asList(
            "return ap_get_remote_host(r->connection, r->per_dir_config,",
            "REMOTE_NAME, NULL);",
            "return ap_get_remote_logname(r);",
            "return (r->parsed_uri.password)",
            "? apr_pstrcat(r->pool, r->method, \" \",",
            "apr_uri_unparse(r->pool, &r->parsed_uri, 0),",
            "r->assbackwards ? NULL : \" \", r->protocol, NULL)",
            ": r->the_request;",
            "return r->filename;",
            "return r->uri;",
            "return r->method;",
            "return r->protocol;",
            "return (r->args != NULL) ? apr_pstrcat(r->pool, \"?\", r->args, NULL)",
            ": \"\";",
            "return apr_table_get(r->headers_in, a);",
            "return cp;",
            "return apr_table_get(r->err_headers_out, a);",
            "return apr_table_get(r->notes, a);",
            "return apr_table_get(r->subprocess_env, a);",
            "return cookie;",
            "return r->server->server_hostname;",
            "return ap_get_server_name(r);");


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

