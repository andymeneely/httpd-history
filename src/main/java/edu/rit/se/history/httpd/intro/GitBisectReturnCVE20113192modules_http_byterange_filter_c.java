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
 * CVE-20113192
 * Vulnerable file: modules/http/byterange_filter.c
 * Fix commit: //___FIX___
 * 
 * <pre>
 *  ./tryBisect.sh 20113192 modules/http/byterange_filter.c //___FIX___ GitBisectReturnCVE20113192modules_http_byterange_filter_c
 * </pre>
 *
 * Result: _ is the first bad commit
 *
 * @author Alberto Rodriguez
 * 
 */
public class GitBisectReturnCVE20113192modules_http_byterange_filter_c {

	private static final int GOOD_RETURN_CODE = 0;
	private static final int BAD_RETURN_CODE = 1;
	private static final int SKIP_RETURN_CODE = 125;

	// Context from vulnerable version.
	private static List<String> oldBlocks;

	// Context from fixed version.
	private static List<String> newBlocks;
    
    private static final String CVE = "CVE-20113192";
	private static final String FILE = "modules/http/byterange_filter.c";

	public static void main(String[] args) {
		if (args.length > 0) {
			System.out.println("No arguments required to this script!");
		}

        newBlocks = Arrays.asList(
            "static apr_status_t copy_brigade_range(apr_bucket_brigade *bb,",
            "apr_bucket_brigade *bbout,",
            "apr_off_t start,",
            "apr_off_t end)",
            "apr_bucket *first = NULL, *last = NULL, *out_first = NULL, *e;",
            "apr_off_t pos = 0, off_first = 0, off_last = 0;",
            "apr_status_t rv;",
            "const char *s;",
            "apr_size_t len;",
            "if (start < 0 || start > end)",
            "return APR_EINVAL;",
            "for (e = APR_BRIGADE_FIRST(bb);",
            "e != APR_BRIGADE_SENTINEL(bb);",
            "e = APR_BUCKET_NEXT(e))",
            "AP_DEBUG_ASSERT(e->length != (apr_size_t)(-1));",
            "if (!first && (e->length > start || e->length + pos > start)) {",
            "first = e;",
            "off_first = pos;",
            "if (!last && (e->length >= end || e->length + pos >= end)) {",
            "last = e;",
            "off_last = pos;",
            "break;",
            "pos += e->length;",
            "if (!first || !last)",
            "e = first;",
            "for (; ; )",
            "apr_bucket *copy;",
            "AP_DEBUG_ASSERT(e != APR_BRIGADE_SENTINEL(bb));",
            "rv = apr_bucket_copy(e, &copy);",
            "if (rv != APR_SUCCESS)",
            "goto err; /* XXX try apr_bucket_read */",
            "APR_BRIGADE_INSERT_TAIL(bbout, copy);",
            "if (e == first) {",
            "if (off_first != start) {",
            "rv = apr_bucket_split(copy, start - off_first);",
            "if (rv == APR_ENOTIMPL) {",
            "rv = apr_bucket_read(copy, &s, &len, APR_BLOCK_READ);",
            "goto err;",
            "out_first = APR_BUCKET_NEXT(copy);",
            "APR_BUCKET_REMOVE(copy);",
            "apr_bucket_destroy(copy);",
            "else {",
            "out_first = copy;",
            "if (e == last) {",
            "off_last += start - off_first;",
            "copy = out_first;",
            "if (end - off_last != e->length) {",
            "rv = apr_bucket_split(copy, end + 1 - off_last);",
            "copy = APR_BUCKET_NEXT(copy);",
            "e = APR_BUCKET_NEXT(e);",
            "AP_DEBUG_ASSERT(APR_SUCCESS == apr_brigade_length(bbout, 1, &pos));",
            "AP_DEBUG_ASSERT(pos == end - start + 1);",
            "return APR_SUCCESS;",
            "err:",
            "apr_brigade_cleanup(bbout);",
            "return rv;",
            "apr_bucket_brigade *tmpbb;",
            "tmpbb = apr_brigade_create(r->pool, c->bucket_alloc);",
            "rv = copy_brigade_range(bb, tmpbb, range_start, range_end);",
            "if (rv != APR_SUCCESS ) {",
            "\"brigade_copy_range() failed \" \"[%\" APR_OFF_T_FMT",
            "\"-%\" APR_OFF_T_FMT \",%\"",
            "APR_OFF_T_FMT \"]\",",
            "range_start, range_end, clength);",
            "APR_BRIGADE_CONCAT(bsend, tmpbb);",
            "apr_brigade_destroy(tmpbb);");

        oldBlocks = Arrays.asList(
            "apr_bucket *e2;",
            "apr_bucket *ec;",
            "if ((rv = apr_brigade_partition(bb, range_start, &ec)) != APR_SUCCESS) {",
            "ap_log_rerror(APLOG_MARK, APLOG_ERR, rv, r,",
            "PARTITION_ERR_FMT, range_start, clength);",
            "continue;",
            "if ((rv = apr_brigade_partition(bb, range_end+1, &e2)) != APR_SUCCESS) {",
            "PARTITION_ERR_FMT, range_end+1, clength);",
            "do {",
            "apr_bucket *foo;",
            "const char *str;",
            "apr_size_t len;",
            "if (apr_bucket_copy(ec, &foo) != APR_SUCCESS) {",
            "apr_bucket_read(ec, &str, &len, APR_BLOCK_READ);",
            "apr_bucket_copy(ec, &foo);",
            "APR_BRIGADE_INSERT_TAIL(bsend, foo);",
            "ec = APR_BUCKET_NEXT(ec);",
            "} while (ec != e2);");


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

