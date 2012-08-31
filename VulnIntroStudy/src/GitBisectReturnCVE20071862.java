import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;

/**
 * 
 */

/**
 * CVE-2007-1862: /modules/cache/mod_mem_cache.c
 * @author harsha
 * @version v2
 * 
 */
public class GitBisectReturnCVE20071862 {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		boolean commitStatus = false;
		//use specific CVE identifier here..
		System.out.println("Bisecting for <CVE-2007-1862>");
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
			if(stringBuffer.indexOf("static apr_table_t *deep_table_copy(apr_pool_t *p, const apr_table_t *table)")>0
					&&stringBuffer.indexOf("h->req_hdrs = deep_table_copy(r->pool, mobj->req_hdrs);")>0
					&&stringBuffer.indexOf("h->resp_hdrs = deep_table_copy(r->pool, mobj->header_out);")>0
					&&stringBuffer.indexOf("mobj->req_hdrs = deep_table_copy(mobj->pool, r->headers_in);")>0
					&&stringBuffer.indexOf("mobj->header_out = deep_table_copy(mobj->pool, headers_out);")>0
					&&stringBuffer.indexOf("if (cc_cresp && ap_cache_liststr(r->pool, cc_cresp, \"max-age\", &val)")>0
					&&stringBuffer.indexOf("maxstale = APR_INT64_C(86400*365);")>0
					) {
				System.out.println("Good Commit Context Met, commit was good");
				goodCommit = true;
			} else if(stringBuffer.indexOf("static apr_table_t *deep_table_copy(apr_pool_t *p, const apr_table_t *table)")<0
					&&stringBuffer.indexOf("h->req_hdrs = apr_table_copy(r->pool, mobj->req_hdrs);")>0
					&&stringBuffer.indexOf("h->resp_hdrs = apr_table_copy(r->pool, mobj->header_out);")>0
					&&stringBuffer.indexOf("mobj->req_hdrs = apr_table_copy(mobj->pool, r->headers_in);")>0
					&&stringBuffer.indexOf("mobj->header_out = apr_table_copy(mobj->pool, headers_out);")>0
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
