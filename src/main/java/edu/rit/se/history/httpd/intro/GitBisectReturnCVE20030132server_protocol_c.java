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
 * CVE-20030132
 * Vulnerable file: server/protocol.c
 * Fix commit: //___FIX___
 * 
 * <pre>
 *  ./tryBisect.sh 20030132 server/protocol.c //___FIX___ GitBisectReturnCVE20030132server_protocol_c
 * </pre>
 *
 * Result: _ is the first bad commit
 *
 * @author Alberto Rodriguez
 * 
 */
public class GitBisectReturnCVE20030132server_protocol_c {

	private static final int GOOD_RETURN_CODE = 0;
	private static final int BAD_RETURN_CODE = 1;
	private static final int SKIP_RETURN_CODE = 125;

	// Context from vulnerable version.
	private static List<String> oldBlocks;

	// Context from fixed version.
	private static List<String> newBlocks;
    
    private static final String CVE = "CVE-20030132";
	private static final String FILE = "server/protocol.c";

	public static void main(String[] args) {
		if (args.length > 0) {
			System.out.println("No arguments required to this script!");
		}

        newBlocks = Arrays.asList(
            "for (;;) {",
            "if (last_char && (*last_char == APR_ASCII_LF)) {",
            "break;",
            "apr_brigade_cleanup(bb);",
            "c = *str;",
            "if (bytes_handled >= n) {",
            "*read = n;",
            "return APR_ENOSPC;",
            "else {",
            "rv = ap_rgetline_core(&tmp, next_size,",
            "&next_len, r, 0, bb);",
            "bytes_handled += next_len;",
            "else { /* next character is not tab or space */",
            "int num_blank_lines = 0;",
            "int max_blank_lines = r->server->limit_req_fields;",
            "if (max_blank_lines <= 0) {",
            "max_blank_lines = DEFAULT_LIMIT_REQUEST_FIELDS;",
            "} while ((len <= 0) && (++num_blank_lines < max_blank_lines));");

        oldBlocks = Arrays.asList(
            "if (bytes_handled == 0) {",
            "*read = 0;",
            "return APR_SUCCESS;",
            "if (*last_char != APR_ASCII_LF) {",
            "if (bytes_handled < n) {",
            "apr_size_t next_size, next_len;",
            "char *tmp;",
            "if (do_alloc) {",
            "tmp = NULL;",
            "} else {",
            "tmp = last_char + 1;",
            "next_size = n - bytes_handled;",
            "rv = ap_rgetline_core(&tmp, next_size, &next_len, r, fold, bb);",
            "if (rv != APR_SUCCESS) {",
            "return rv;",
            "if (do_alloc && next_len > 0) {",
            "char *new_buffer;",
            "apr_size_t new_size = bytes_handled + next_len;",
            "new_buffer = apr_palloc(r->pool, new_size);",
            "memcpy(new_buffer, *s, bytes_handled);",
            "memcpy(new_buffer + bytes_handled, tmp, next_len);",
            "current_alloc = new_size;",
            "*s = new_buffer;",
            "bytes_handled += next_len;",
            "last_char = *s + bytes_handled - 1;",
            "else {",
            "*read = n;",
            "return APR_ENOSPC;",
            "*read = bytes_handled;",
            "apr_brigade_destroy(bb);",
            "c = *str;",
            "*read = bytes_handled + next_len;",
            "} while (len <= 0);");


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

