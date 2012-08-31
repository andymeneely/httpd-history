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
 * CVE-2010-0434: /server/protocol.c
 * @author harsha
 * @version v2
 * 
 */
public class GitBisectReturnCVE20100434_3 {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		boolean commitStatus = false;
		//use specific CVE identifier here..
		System.out.println("Bisecting for <CVE-2010-0434>");
		try {
			//args[0] is the full path to the file that was fixed
			commitStatus = bisectBadOrGood(args[0]);
			System.out.println("CommitStatus::" + commitStatus);
			if(commitStatus==true) {
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
	public static boolean bisectBadOrGood(String fileName)
			throws FileNotFoundException {
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
			//System.out.println(stringBuffer);
			/**
			 * if checks for the good commit, else vice versa
			 * check for the context here, context is determined by what the 
			 * researcher deems important to the fix
			 * additional commented lines can be uncommented for checking other 
			 * contexts that seem fit
			 */
			if(stringBuffer.indexOf("rnew->headers_in = apr_table_copy(rnew->pool, r->headers_in);")<0
					&&stringBuffer.indexOf("strip_headers_request_body(rnew);")>0
					&&stringBuffer.indexOf("/* if a request with a body creates a subrequest, remove original request's")>0
					&&stringBuffer.indexOf("* input headers which pertain to the body which has already been read.")>0
					) {
				System.out.println("Good Commit Context Met, commit was good");
				goodCommit = true;
			} else if(stringBuffer.indexOf("/* if a request with a body creates a subrequest, clone the original request's")>0
					&&stringBuffer.indexOf("* input headers minus any headers pertaining to the body which has already")>0
					&&stringBuffer.indexOf("rnew->headers_in = apr_table_copy(rnew->pool, r->headers_in);")>0
					&&stringBuffer.indexOf("clone_headers_no_body(rnew, r);")>0
					&&stringBuffer.indexOf("/* no body (common case).  clone headers the cheap way */")>0
					) {
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
