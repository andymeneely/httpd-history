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
 * Incomplete fix for CVE-2011-3192 on file byterange_filter.c
 * @author harsha
 * @version v2
 * 
 */
public class GitBisectReturnCVE20113192 {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		boolean commitStatus = false;
		// use specific CVE identifier here..
		System.out.println("Bisecting for <CVE-2011-3192>");
		try {
			// args[0] is the full path to the file that was fixed
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

	/**
	 * 
	 * @param fileName
	 * @return boolean good or bad commit
	 * @throws FileNotFoundException
	 */
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
			// System.out.println(stringBuffer);
			/**
			 * if checks for the good commit, else vice versa check for the context here, context is
			 * determined by what the researcher deems important to the fix additional commented lines can be
			 * uncommented for checking other contexts that seem fit
			 */
			if (stringBuffer.indexOf("static apr_status_t copy_brigade_range(apr_bucket_brigade *bb,") > 0
					&& stringBuffer.indexOf("apr_bucket_brigade *tmpbb;") > 0
					&& stringBuffer.indexOf("tmpbb = apr_brigade_create(r->pool, c->bucket_alloc);") > 0
					&& stringBuffer.indexOf("rv = copy_brigade_range(bb, tmpbb, range_start, range_end);") > 0
					&& stringBuffer.indexOf("APR_BRIGADE_CONCAT(bsend, tmpbb);") > 0
					&& stringBuffer.indexOf("apr_brigade_destroy(tmpbb);") > 0) {
				System.out.println("Good Commit Context Met, commit was good");
				goodCommit = true;
			} else if (stringBuffer.indexOf("if (apr_bucket_copy(ec, &foo) != APR_SUCCESS) {") > 0
					&& stringBuffer.indexOf("apr_bucket *e2;") > 0
					&& stringBuffer.indexOf("apr_bucket *ec;") > 0
					&& stringBuffer.indexOf("if ((rv = apr_brigade_partition(bb, range_start, &ec)) != APR_SUCCESS) {") > 0
					&& stringBuffer.indexOf(" } while (ec != e2);") > 0
					&& stringBuffer.indexOf("apr_bucket_copy(ec, &foo);") > 0
					&& stringBuffer
							.indexOf(" if ((rv = apr_brigade_partition(bb, range_end+1, &e2)) != APR_SUCCESS) {") > 0) {
				System.out.println("Context for good commit not found, bad commit");
				goodCommit = false;
			} else {
				goodCommit = true;
			}
		} catch (Exception e) {
			// Catch exception if any
			System.err.println("Error: " + e.getMessage());
		}
		System.out.println("exiting bisectBadOrGood");
		return goodCommit;
	}
}
