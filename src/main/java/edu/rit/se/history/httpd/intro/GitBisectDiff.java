package edu.rit.se.history.httpd.intro;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * 
 * <pre>
 *  ./tryBisect <CVE number> <vulnerable file> <git fix commit>
 * </pre>
 * 
 * @author Alberto Rodriguez
 * 
 */
public class GitBisectDiff {

	private static final int GOOD_RETURN_CODE = 0;
	private static final int BAD_RETURN_CODE = 1;
	private static final int SKIP_RETURN_CODE = 125;

	// Context from vulnerable version.
	private static final List<String> oldBlocks = new ArrayList<String>();
	
	// Context from fixed version.
	private static final List<String> newBlocks = new ArrayList<String>();

	public static void main(String[] args) {
		if (args.length != 2) {
			System.err.println("Usage: GitBisectDiff <Vulnerable file> <CVE>");
			System.exit(SKIP_RETURN_CODE);
		}

		String file = args[0];
		String cve = args[1];
		
		File vulnerableFile = new File(file);
		
		String diffFilePath = file.replace("/", "_").replace("\\", "_") + "_" + cve + ".diff";
		File diffFile = new File(diffFilePath);

		System.out.println("===Bisect check for " + cve + ", " + file + "===");
		try {
			parseDiff(diffFile);
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
			System.err.println("Vulnerable file: " + vulnerableFile.getAbsolutePath());
			System.err.println("Diff patch: " + diffFile.getAbsolutePath());
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
	public static boolean isVulnerable(File file) throws IOException {
		StringBuffer sb = readFile(file);

		String fileContent = removeUnwantedChars(sb.toString());

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
		return text.replace("\r", "").replace("\n", "")
				.replace("\t", "").replace(" ", "").replace("\\", "")
				.replace("\"", "");
	}

	private static StringBuffer readFile(File fileName)
			throws FileNotFoundException, IOException {
		// Open the file that is the first
		// command line parameter
		FileInputStream fstream = new FileInputStream(fileName);
		// Get the object of DataInputStream
		DataInputStream in = new DataInputStream(fstream);
		BufferedReader br = new BufferedReader(new InputStreamReader(in));
		String strLine;
		StringBuffer sb = new StringBuffer();
		// Read file line by line, removing newlines
		while ((strLine = br.readLine()) != null) {
			sb.append(strLine.trim());
		}
		// Close the input stream
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

	public static void parseDiff(File diffFile) throws IOException {		
		if (!diffFile.exists()) {
			throw new FileNotFoundException("Diff file not found: "
					+ diffFile.getAbsolutePath());
		}
		BufferedReader br = new BufferedReader(new FileReader(diffFile));
		String line;
		int lineNumber = 0;
		while ((line = br.readLine()) != null) {
			if (lineNumber > 1) {
				if (line.startsWith("@@")) {
					oldBlocks.add("");
					newBlocks.add("");
				} else {
					if (line.startsWith("+") || line.startsWith(" ")) {
						appendBlock(line, newBlocks);
					}
					if (line.startsWith("-") || line.startsWith(" ")) {
						appendBlock(line, oldBlocks);
					}
				}
			}
			lineNumber++;
		}
		br.close();
		if (oldBlocks.size() == 0 || newBlocks.size() == 0) {
			StringBuffer sb = readFile(diffFile);
			throw new IOException("Error parsing diff: \n" + sb.toString());
		}
	}

	private static void appendBlock(String line, List<String> blockList) {
		if (!blockList.isEmpty()) {
			String cleanLn = removeUnwantedChars(line.substring(1));
			int last = blockList.size() - 1;
			blockList.set(last, blockList.get(last) + cleanLn);
		}
	}
}
