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
 * CVE-2012-0053
 * Vulnerable file: server/protocol.c
 * Fix commit: ae522645f034c56ed8784ba127b716447c55ba5b
 * 
 * <pre>
 *  ./tryBisect.sh 20120053 server/protocol.c ae522645f034c56ed8784ba127b716447c55ba5b GitBisectReturnCVE20120053ab
 * </pre>
 *
 * Result: 91184351d4e4f6fc7da36b3134177f843d2ba770 is the first bad commit
 *
 * @author Alberto Rodriguez
 * 
 */
public class GitBisectReturnCVE20120053ab {

	private static final int GOOD_RETURN_CODE = 0;
	private static final int BAD_RETURN_CODE = 1;
	private static final int SKIP_RETURN_CODE = 125;

	// Context from vulnerable version.
	private static List<String> oldBlocks;

	// Context from fixed version.
	private static List<String> newBlocks;
    
    private static final String CVE = "CVE-2012-0053";
	private static final String FILE = "server/protocol.c";

	public static void main(String[] args) {
		if (args.length > 0) {
			System.out.println("No arguments required to this script!");
		}

		newBlocks = Arrays.asList(
			    "field[len-1]='0';apr_table_setn(r->notes,error-notes,apr_psprintf(r->pool,Sizeofarequestheaderfieldexceedsserverlimit.<br/>n<pre>n%.*sn</pre>n,field_name_len(field),ap_escape_html(r->pool,field)));ap_log_rerror(APLOG_MARK,APLOG_INFO,0,r,APLOGNO(00561)RequestheaderexceedsLimitRequestFieldSize:%.*s,field_name_len(field),field);",
			    "apr_table_setn(r->notes,error-notes,apr_psprintf(r->pool,Sizeofarequestheaderfieldafterfoldingexceedsserverlimit.<br/>n<pre>n%.*sn</pre>n,field_name_len(last_field),ap_escape_html(r->pool,last_field)));ap_log_rerror(APLOG_MARK,APLOG_INFO,0,r,APLOGNO(00562)RequestheaderexceedsLimitRequestFieldSizeafterfolding:%.*s,",
			    "if(!(value=strchr(last_field,':'))){r->status=HTTP_BAD_REQUEST;apr_table_setn(r->notes,error-notes,apr_psprintf(r->pool,Requestheaderfieldismissing':'separator.<br/>n<pre>n%.*s</pre>n,(int)LOG_NAME_MAX_LEN,ap_escape_html(r->pool,last_field)));ap_log_rerror(APLOG_MARK,APLOG_INFO,0,r,APLOGNO(00564)Requestheaderfieldismissing':'separator:%.*s,(int)LOG_NAME_MAX_LEN,");

			oldBlocks = Arrays.asList(
			    "field[len-1]='0';apr_table_setn(r->notes,error-notes,apr_pstrcat(r->pool,Sizeofarequestheaderfieldexceedsserverlimit.<br/>n<pre>n,ap_escape_html(r->pool,field),</pre>n,NULL));ap_log_rerror(APLOG_MARK,APLOG_INFO,0,r,APLOGNO(00561)RequestheaderexceedsLimitRequestFieldSize:%.*s,field_name_len(field),field);",
			    "apr_table_setn(r->notes,error-notes,apr_pstrcat(r->pool,Sizeofarequestheaderfieldafterfoldingexceedsserverlimit.<br/>n<pre>n,ap_escape_html(r->pool,last_field),</pre>n,NULL));ap_log_rerror(APLOG_MARK,APLOG_INFO,0,r,APLOGNO(00562)RequestheaderexceedsLimitRequestFieldSizeafterfolding:%.*s,",
			    "if(!(value=strchr(last_field,':'))){r->status=HTTP_BAD_REQUEST;apr_table_setn(r->notes,error-notes,apr_pstrcat(r->pool,Requestheaderfieldismissing':'separator.<br/>n<pre>n,ap_escape_html(r->pool,last_field),</pre>n,NULL));ap_log_rerror(APLOG_MARK,APLOG_INFO,0,r,APLOGNO(00564)Requestheaderfieldismissing':'separator:%.*s,(int)LOG_NAME_MAX_LEN,");



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

		String fileContent = removeComments(removeUnwantedChars(sb.toString()));

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

	private static String removeUnwantedChars(String text) {
		return text.replace("\r", "").replace("\n", "").replace("\t", "")
				.replace(" ", "").replace("\\", "").replace("\"", "");
	}

	private static String removeComments(String text) {
		return text
		// Matches this: "/* comment */"
				.replaceAll("/\\*(?:.)*?\\*/", "")
				// Matches this: "comment */"
				.replaceAll("^(?:.)*?\\*/", "")
				// Matches this: "/* comment"
				.replaceAll("/\\*(?:.)*?$", "");
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
