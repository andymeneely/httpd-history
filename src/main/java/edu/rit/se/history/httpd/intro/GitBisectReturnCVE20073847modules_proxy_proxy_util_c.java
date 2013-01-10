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
 * CVE-20073847
 * Vulnerable file: modules/proxy/proxy_util.c
 * Fix commit: //___FIX___
 * 
 * <pre>
 *  ./tryBisect.sh 20073847 modules/proxy/proxy_util.c //___FIX___ GitBisectReturnCVE20073847modules_proxy_proxy_util_c
 * </pre>
 *
 * Result: _ is the first bad commit
 *
 * @author Alberto Rodriguez
 * 
 */
public class GitBisectReturnCVE20073847modules_proxy_proxy_util_c {

	private static final int GOOD_RETURN_CODE = 0;
	private static final int BAD_RETURN_CODE = 1;
	private static final int SKIP_RETURN_CODE = 125;

	// Context from vulnerable version.
	private static List<String> oldBlocks;

	// Context from fixed version.
	private static List<String> newBlocks;
    
    private static final String CVE = "CVE-20073847";
	private static final String FILE = "modules/proxy/proxy_util.c";

	public static void main(String[] args) {
		if (args.length > 0) {
			System.out.println("No arguments required to this script!");
		}

        newBlocks = Arrays.asList(
            "ap_proxy_date_canon(apr_pool_t *p, const char *date)",
            "apr_status_t rv;",
            "apr_time_exp_t tm;",
            "apr_size_t retsize;",
            "char* ndate;",
            "static const char format[] = \"%a, %d %b %Y %H:%M:%S GMT\";",
            "apr_time_t time = apr_date_parse_http(date);",
            "if (!time) {",
            "return date;",
            "rv = apr_time_exp_gmt(&tm, time);",
            "if (rv != APR_SUCCESS) {",
            "ndate = apr_palloc(p, APR_RFC822_DATE_LEN);",
            "rv = apr_strftime(ndate, &retsize, APR_RFC822_DATE_LEN, format, &tm);",
            "if (rv != APR_SUCCESS || !retsize) {",
            "return ndate;");

        oldBlocks = Arrays.asList(
            "static const char * const lwday[7] =",
            "{\"Sunday\", \"Monday\", \"Tuesday\", \"Wednesday\", \"Thursday\", \"Friday\", \"Saturday\"};",
            "ap_proxy_date_canon(apr_pool_t *p, const char *x1)",
            "char *x = apr_pstrdup(p, x1);",
            "int wk, mday, year, hour, min, sec, mon;",
            "char *q, month[4], zone[4], week[4];",
            "q = strchr(x, ',');",
            "if (q != NULL && q - x > 3 && q[1] == ' ') {",
            "*q = '\\0';",
            "for (wk = 0; wk < 7; wk++) {",
            "if (strcmp(x, lwday[wk]) == 0) {",
            "break;",
            "*q = ',';",
            "if (wk == 7) {",
            "return x;       /* not a valid date */",
            "if (q[4] != '-' || q[8] != '-' || q[11] != ' ' || q[14] != ':' ||",
            "q[17] != ':' || strcmp(&q[20], \" GMT\") != 0) {",
            "return x;",
            "if (sscanf(q + 2, \"%u-%3s-%u %u:%u:%u %3s\", &mday, month, &year,",
            "&hour, &min, &sec, zone) != 7) {",
            "if (year < 70) {",
            "year += 2000;",
            "else {",
            "year += 1900;",
            "if (x[3] != ' ' || x[7] != ' ' || x[10] != ' ' || x[13] != ':' ||",
            "x[16] != ':' || x[19] != ' ' || x[24] != '\\0') {",
            "if (sscanf(x, \"%3s %3s %u %u:%u:%u %u\", week, month, &mday, &hour,",
            "&min, &sec, &year) != 7) {",
            "if (strcmp(week, apr_day_snames[wk]) == 0) {",
            "for (mon = 0; mon < 12; mon++) {",
            "if (strcmp(month, apr_month_snames[mon]) == 0) {",
            "if (mon == 12) {",
            "q = apr_palloc(p, 30);",
            "apr_snprintf(q, 30, \"%s, %.2d %s %d %.2d:%.2d:%.2d GMT\", apr_day_snames[wk],",
            "mday, apr_month_snames[mon], year, hour, min, sec);",
            "return q;");


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

