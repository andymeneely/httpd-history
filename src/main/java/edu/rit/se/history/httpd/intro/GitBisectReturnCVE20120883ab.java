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
 * CVE-2012-0883
 * Vulnerable file: support/envvars-std.in
 * Fix commit: 55c244c694d68cb578551c372fc2364caccebae1
 * 
 * <pre>
 *  ./tryBisect.sh 20120883 support/envvars-std.in 55c244c694d68cb578551c372fc2364caccebae1 GitBisectReturnCVE20120883ab
 * </pre>
 *
 * Result: 91184351d4e4f6fc7da36b3134177f843d2ba770 is the first bad commit
 *
 * @author Alberto Rodriguez
 * 
 */
public class GitBisectReturnCVE20120883ab {

	private static final int GOOD_RETURN_CODE = 0;
	private static final int BAD_RETURN_CODE = 1;
	private static final int SKIP_RETURN_CODE = 125;

	// Context from vulnerable version.
	private static List<String> oldBlocks;

	// Context from fixed version.
	private static List<String> newBlocks;
    
    private static final String CVE = "CVE-2012-0883";
	private static final String FILE = "support/envvars-std.in";

	public static void main(String[] args) {
		if (args.length > 0) {
			System.out.println("No arguments required to this script!");
		}

		newBlocks = Arrays.asList(
            "##Thisfileisgeneratedfromenvvars-std.in#iftestx$@SHLIBPATH_VAR@!=x;then@SHLIBPATH_VAR@=@exp_libdir@:$@SHLIBPATH_VAR@else@SHLIBPATH_VAR@=@exp_libdir@fiexport@SHLIBPATH_VAR@#@OS_SPECIFIC_VARS@");

        oldBlocks = Arrays.asList(
            "##Thisfileisgeneratedfromenvvars-std.in#@SHLIBPATH_VAR@=@exp_libdir@:$@SHLIBPATH_VAR@export@SHLIBPATH_VAR@#@OS_SPECIFIC_VARS@");


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
