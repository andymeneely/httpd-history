package edu.rit.se.history.httpd.intro;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;

/**
 * 
 */

/**
 * @author harsha
 * @version v2 Checks for good or bad commits for the files in CVE-2012-0883
 * 
 */
public class GitBisectReturnCVE20120883 {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		boolean commitStatus = false;
		System.out.println("Bisecting for CVE-2012-0883");
		try {
			commitStatus = bisectBadOrGood(args[0]);
			System.out.println("CommitStatus::" + commitStatus);
			if (commitStatus == true) {
				System.exit(0);
			} else {
				System.exit(1);
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	public static boolean bisectBadOrGood(String fileName) throws FileNotFoundException {
		System.out.println("entered bisectBadOrGood");
		boolean goodCommit = false;
		try {
			// Open the file that is the first
			// command line parameter
			FileInputStream fstream = new FileInputStream(fileName);
			// Get the object of DataInputStream
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String strLine;
			StringBuffer stringBuffer = new StringBuffer();
			// Read File Line By Line
			while ((strLine = br.readLine()) != null) {
				stringBuffer.append(strLine);
			}
			// Close the input stream
			in.close();
			if (stringBuffer.indexOf("if test \"x$@SHLIBPATH_VAR@\" != \"x\" ; then") > 0
					&& stringBuffer.indexOf("@SHLIBPATH_VAR@=\"@exp_libdir@\"") > 0) {
				System.out.println("Good Commit Context Met, commit was good");
				goodCommit = true;
			} else if (stringBuffer.indexOf("if test \"x$@SHLIBPATH_VAR@\" != \"x\" ; then") < 0
					&& stringBuffer.indexOf("@SHLIBPATH_VAR@=\"@exp_libdir@\"") < 0
					&& stringBuffer.indexOf("@SHLIBPATH_VAR@=\"@exp_libdir@:$@SHLIBPATH_VAR@\"") > 0) {
				System.out.println("Context for good commit not found, bad commit");
				goodCommit = false;
			} else {
				goodCommit = true;
			}
		} catch (Exception e) {// Catch exception if any
			System.err.println("Error: " + e.getMessage());
		}
		System.out.println("exiting bisectBadOrGood");
		return goodCommit;
	}
}
