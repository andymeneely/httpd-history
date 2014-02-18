package edu.rit.se.history.httpd.mailinglistparse;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.zip.GZIPInputStream;

public class GzipUnzipper {
	private static final String INPUT_GZIP_FILE = "/Users/abhishekdale/Documents/Mailing List/200201.gz";
	private static final String OUTPUT_FILE = "/Users/abhishekdale/Documents/MLISTUNZIPPED/";

	public static void main(String[] args) {
		GzipUnzipper gZip = new GzipUnzipper();
		gZip.gunzipIt();
	}

	/**
	 * GunZip it
	 */
	public void gunzipIt() {

		byte[] buffer = new byte[1024];

		try {
			GZIPInputStream gzis = null;
			FileOutputStream out = null;
			URL url;

			for (int i = 10; i < 13; i++) {
				url = new URL("http://httpd.apache.org/mail/dev/1995" + i
						+ ".gz");
				gzis = new GZIPInputStream(url.openStream());

				out = new FileOutputStream(OUTPUT_FILE + "1995" + i + ".txt");

				int len;
				while ((len = gzis.read(buffer)) > 0) {
					out.write(buffer, 0, len);
				}

			}

			gzis.close();
			out.close();

			System.out.println("Done");

		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}
}