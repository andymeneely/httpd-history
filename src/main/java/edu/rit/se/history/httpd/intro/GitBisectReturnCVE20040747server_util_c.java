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
 * CVE-20040747
 * Vulnerable file: server/util.c
 * Fix commit: //___FIX___
 * 
 * <pre>
 *  ./tryBisect.sh 20040747 server/util.c //___FIX___ GitBisectReturnCVE20040747server_util_c
 * </pre>
 *
 * Result: _ is the first bad commit
 *
 * @author Alberto Rodriguez
 * 
 */
public class GitBisectReturnCVE20040747server_util_c {

	private static final int GOOD_RETURN_CODE = 0;
	private static final int BAD_RETURN_CODE = 1;
	private static final int SKIP_RETURN_CODE = 125;

	// Context from vulnerable version.
	private static List<String> oldBlocks;

	// Context from fixed version.
	private static List<String> newBlocks;
    
    private static final String CVE = "CVE-20040747";
	private static final String FILE = "server/util.c";

	public static void main(String[] args) {
		if (args.length > 0) {
			System.out.println("No arguments required to this script!");
		}

        newBlocks = Arrays.asList(
            "return (char *)ap_resolve_env(p,result);",
            "# define SMALL_EXPANSION 5",
            "struct sll {",
            "struct sll *next;",
            "const char *string;",
            "apr_size_t len;",
            "} *result, *current, sresult[SMALL_EXPANSION];",
            "char *res_buf, *cp;",
            "const char *s, *e, *ep;",
            "unsigned spc;",
            "apr_size_t outlen;",
            "s = ap_strchr_c(word, '$');",
            "if (!s) {",
            "return word;",
            "ep = word + strlen(word);",
            "spc = 0;",
            "result = current = &(sresult[spc++]);",
            "current->next = NULL;",
            "current->string = word;",
            "current->len = s - word;",
            "outlen = current->len;",
            "do {",
            "if (current->len) {",
            "current->next = (spc < SMALL_EXPANSION)",
            "? &(sresult[spc++])",
            ": (struct sll *)apr_palloc(p,",
            "sizeof(*current->next));",
            "current = current->next;",
            "current->len = 0;",
            "if (*s == '$') {",
            "if (s[1] == '{' && (e = ap_strchr_c(s, '}'))) {",
            "word = getenv(apr_pstrndup(p, s+2, e-s-2));",
            "if (word) {",
            "current->len = strlen(word);",
            "outlen += current->len;",
            "else {",
            "current->string = s;",
            "current->len = e - s + 1;",
            "s = e + 1;",
            "current->string = s++;",
            "current->len = 1;",
            "++outlen;",
            "word = s;",
            "s = ap_strchr_c(s, '$');",
            "current->len = s ? s - word : ep - word;",
            "} while (s && *s);",
            "res_buf = cp = apr_palloc(p, outlen + 1);",
            "if (result->len) {",
            "memcpy(cp, result->string, result->len);",
            "cp += result->len;",
            "result = result->next;",
            "} while (result);",
            "res_buf[outlen] = '\\0';",
            "return res_buf;");

        oldBlocks = Arrays.asList(
            "return ap_resolve_env(p,result);",
            "char tmp[ MAX_STRING_LEN ];",
            "const char *s, *e;",
            "tmp[0] = '\\0';",
            "if (!(s=ap_strchr_c(word,'$')))",
            "return word;",
            "do {",
            "strncat(tmp,word,s - word);",
            "if ((s[1] == '{') && (e=ap_strchr_c(s,'}'))) {",
            "const char *e2 = e;",
            "char *var;",
            "word = e + 1;",
            "var = apr_pstrndup(p, s+2, e2-(s+2));",
            "e = getenv(var);",
            "if (e) {",
            "strcat(tmp,e);",
            "} else {",
            "strncat(tmp, s, e2-s);",
            "strcat(tmp,\"}\");",
            "word = s+1;",
            "strcat(tmp,\"$\");",
            "};",
            "} while ((s=ap_strchr_c(word,'$')));",
            "strcat(tmp,word);",
            "return apr_pstrdup(p,tmp);");


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

