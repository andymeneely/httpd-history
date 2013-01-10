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
 * CVE-20093094
 * Vulnerable file: modules/proxy/mod_proxy_ftp.c
 * Fix commit: //___FIX___
 * 
 * <pre>
 *  ./tryBisect.sh 20093094 modules/proxy/mod_proxy_ftp.c //___FIX___ GitBisectReturnCVE20093094modules_proxy_mod_proxy_ftp_c
 * </pre>
 *
 * Result: _ is the first bad commit
 *
 * @author Alberto Rodriguez
 * 
 */
public class GitBisectReturnCVE20093094modules_proxy_mod_proxy_ftp_c {

	private static final int GOOD_RETURN_CODE = 0;
	private static final int BAD_RETURN_CODE = 1;
	private static final int SKIP_RETURN_CODE = 125;

	// Context from vulnerable version.
	private static List<String> oldBlocks;

	// Context from fixed version.
	private static List<String> newBlocks;
    
    private static final String CVE = "CVE-20093094";
	private static final String FILE = "modules/proxy/mod_proxy_ftp.c";

	public static void main(String[] args) {
		if (args.length > 0) {
			System.out.println("No arguments required to this script!");
		}

        newBlocks = Arrays.asList(
            "static apr_port_t parse_epsv_reply(char *reply)",
            "char *p, *ep;",
            "long port;",
            "p = ap_strchr(reply, '(');",
            "if (p == NULL || !p[0] || !p[1] || p[1] != p[2] || p[1] != p[3]",
            "|| p[4] == p[1]) {",
            "return 0;",
            "errno = 0;",
            "port = strtol(p + 4, &ep, 10);",
            "if (errno || port < 1 || port > 65535 || ep[0] != p[1] || ep[1] != ')') {",
            "return (apr_port_t)port;",
            "data_port = parse_epsv_reply(ftpmessage);",
            "if (data_port) {",
            "if (data_sock) {",
            "apr_socket_close(data_sock);");

        oldBlocks = Arrays.asList(
            "char *pstr;",
            "char *tok_cntx;",
            "pstr = ftpmessage;",
            "pstr = apr_strtok(pstr, \" \", &tok_cntx);    /* separate result code */",
            "if (pstr != NULL) {",
            "if (*(pstr + strlen(pstr) + 1) == '=') {",
            "pstr += strlen(pstr) + 2;",
            "else {",
            "pstr = apr_strtok(NULL, \"(\", &tok_cntx);    /* separate address &",
            "if (pstr != NULL)",
            "pstr = apr_strtok(NULL, \")\", &tok_cntx);",
            "if (pstr) {",
            "data_port = atoi(pstr + 3);",
            "apr_socket_close(data_sock);");


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

