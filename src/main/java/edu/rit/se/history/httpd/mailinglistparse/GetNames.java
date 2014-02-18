package edu.rit.se.history.httpd.mailinglistparse;

import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.zip.GZIPInputStream;

import org.apache.ivy.util.url.ApacheURLLister;

public class GetNames {

	static List serverDir = null;
	static String file;
	static URL url = null;
	static String fileNamePara = null;

	private static final String OUTPUT_FILE = "C:\\mailinglist\\downloads\\";

	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		try {
			// URL of the location
			URL hostUrl = new URL("http://httpd.apache.org/mail/dev/");

			// apache lister class to get the file names with absolute path in
			// the directory at url location
			ApacheURLLister lister = new ApacheURLLister();

			// serverDir is the list object which stores the filenames with
			// absolute path
			serverDir = lister.listFiles(hostUrl);

			for (int i = 0; i < serverDir.size(); i++) {

				// get the absolute path one by one
				file = serverDir.get(i).toString();

				// extract only the filename from the path
				fileNamePara = file.substring(file.lastIndexOf('/') + 1,
						file.lastIndexOf("."));
				// print
				//System.out.println("\n" + fileNamePara);

				url = new URL(file);
				gunzipIt(url, fileNamePara); // call to the function
			}
			
			System.out.println("Done");
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
		}

	}

	/*
	 * Function: gunzipItParameters: url-> url to the file filename-> name of
	 * the file at the url location Purpose: takes the zipped(.gz) files at the
	 * url location and extracts them to a local directory
	 */

	public static void gunzipIt(URL url, String filename) {

		byte[] buffer = new byte[1024];

		try {
			GZIPInputStream gzis = null;
			FileOutputStream out = null;

			// define the input stream for the unzipper object
			gzis = new GZIPInputStream(url.openStream());

			// set the output file path
			out = new FileOutputStream(OUTPUT_FILE + filename + ".txt");

			// read and write the data
			int len;
			while ((len = gzis.read(buffer)) > 0) {
				out.write(buffer, 0, len);
			}

			gzis.close(); // close the unzipper
			out.close(); // close file stream

			//System.out.println("Done");

		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}

}
