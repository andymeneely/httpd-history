import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;


public class GitBisectReturnCVE20120053 {


	/**
	 * input to bisect script for CVE-2012-0053
	 * @param args
	 */
	public static void main(String[] args) {
		boolean commitStatus = false;
		//use specific CVE identifier here..
		System.out.println("Bisecting for <CVE-2012-0053>");
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
			if(stringBuffer.indexOf("<pre>\\n%.*s</pre>\\n")>0 
					&&stringBuffer.indexOf("(int)LOG_NAME_MAX_LEN")>0
					&&stringBuffer.indexOf("apr_psprintf(r->pool,")>0
					) {
				System.out.println("Good Commit Context Met, commit was good");
				goodCommit = true;
			} else if(stringBuffer.indexOf("if (!(value = strchr(last_field, ':'))) { /* Find ':' or    */")>0
					&&stringBuffer.indexOf("apr_table_setn(r->notes, \"error-notes\",                                   apr_pstrcat(r->pool,                                               \"Request header field is \"                                               \"missing ':' separator.<br />\\n\"                                               \"<pre>\\n\",                                               ap_escape_html(r->pool,                                                              last_field),                                               \"</pre>\\n\", NULL));")>0
					&&stringBuffer.indexOf("tmp_field = value - 1; /* last character of field-name */")>0
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
	