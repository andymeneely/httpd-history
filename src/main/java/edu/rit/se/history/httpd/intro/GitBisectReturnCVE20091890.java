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
 * CVE-2009-1890: /modules/proxy/mod_proxy_http.c
 * @author harsha
 * @version v2
 * 
 */
public class GitBisectReturnCVE20091890 {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		boolean commitStatus = false;
		//use specific CVE identifier here..
		System.out.println("Bisecting for <CVE-2009-1890>");
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
			if(stringBuffer.indexOf("char *endstr;")>0
					&&stringBuffer.indexOf("status = apr_strtoff(&cl_val, old_cl_val, &endstr, 10);")>0
					&&stringBuffer.indexOf("if (status || *endstr || endstr == old_cl_val || cl_val < 0)")>0
					&&stringBuffer.indexOf("if (bytes_streamed > cl_val) {")>0
					) {
				System.out.println("Good Commit Context Met, commit was good");
				goodCommit = true;
			} else if(stringBuffer.indexOf("proxy: read more bytes of request body than expected")<0
					&&stringBuffer.indexOf("if (bytes_streamed > cl_val)")>0
					&&stringBuffer.indexOf("char *endstr;")<0
					&&stringBuffer.indexOf("if (APR_SUCCESS != (status = apr_strtoff(&cl_val, old_cl_val, NULL,")>0
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
