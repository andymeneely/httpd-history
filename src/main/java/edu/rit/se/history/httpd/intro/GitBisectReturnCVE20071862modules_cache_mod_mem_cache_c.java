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
 * CVE-20071862
 * Vulnerable file: modules/cache/mod_mem_cache.c
 * Fix commit: //___FIX___
 * 
 * <pre>
 *  ./tryBisect.sh 20071862 modules/cache/mod_mem_cache.c //___FIX___ GitBisectReturnCVE20071862modules_cache_mod_mem_cache_c
 * </pre>
 *
 * Result: _ is the first bad commit
 *
 * @author Alberto Rodriguez
 * 
 */
public class GitBisectReturnCVE20071862modules_cache_mod_mem_cache_c {

	private static final int GOOD_RETURN_CODE = 0;
	private static final int BAD_RETURN_CODE = 1;
	private static final int SKIP_RETURN_CODE = 125;

	// Context from vulnerable version.
	private static List<String> oldBlocks;

	// Context from fixed version.
	private static List<String> newBlocks;
    
    private static final String CVE = "CVE-20071862";
	private static final String FILE = "modules/cache/mod_mem_cache.c";

	public static void main(String[] args) {
		if (args.length > 0) {
			System.out.println("No arguments required to this script!");
		}

        newBlocks = Arrays.asList(
            "static apr_table_t *deep_table_copy(apr_pool_t *p, const apr_table_t *table)",
            "const apr_array_header_t *array = apr_table_elts(table);",
            "apr_table_entry_t *elts = (apr_table_entry_t *) array->elts;",
            "apr_table_t *copy = apr_table_make(p, array->nelts);",
            "int i;",
            "for (i = 0; i < array->nelts; i++) {",
            "if (elts[i].key) {",
            "apr_table_add(copy, elts[i].key, elts[i].val);",
            "return copy;",
            "h->req_hdrs = deep_table_copy(r->pool, mobj->req_hdrs);",
            "h->resp_hdrs = deep_table_copy(r->pool, mobj->header_out);",
            "mobj->req_hdrs = deep_table_copy(mobj->pool, r->headers_in);",
            "mobj->header_out = deep_table_copy(mobj->pool, headers_out);");

        oldBlocks = Arrays.asList(
            "h->req_hdrs = apr_table_copy(r->pool, mobj->req_hdrs);",
            "h->resp_hdrs = apr_table_copy(r->pool, mobj->header_out);",
            "mobj->req_hdrs = apr_table_copy(mobj->pool, r->headers_in);",
            "mobj->header_out = apr_table_copy(mobj->pool, headers_out);");


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

