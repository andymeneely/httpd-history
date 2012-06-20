package edu.rit.se.history.httpd.scrapers;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import com.google.gdata.client.Service.GDataRequest;
import com.google.gdata.client.spreadsheet.SpreadsheetService;
import com.google.gdata.util.ServiceException;

public class GoogleDocExport {

	private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(GoogleDocExport.class);
	public static final String EXPORT_URL = "https://spreadsheets.google.com/feeds/download/spreadsheets/Export?key=@&exportFormat=csv";

	private List<File> outputFiles = new ArrayList<File>();
	private List<String> googleDocIDs = new ArrayList<String>();

	public void add(String googleDocID, File outputFile) {
		outputFiles.add(outputFile);
		googleDocIDs.add(googleDocID);
	}

	public void downloadCSVs(String googleUsername, String googlePassword) throws IOException, ServiceException {
		SpreadsheetService service = new SpreadsheetService("RIT Software Archeology");
		service.setUserCredentials(googleUsername, googlePassword);
		for (int i = 0; i < googleDocIDs.size(); i++) {
			log.debug("Downloading: " + googleDocIDs.get(i) + " into " + outputFiles.get(i));
			URL feedUrl = new URL(EXPORT_URL.replaceFirst("@", googleDocIDs.get(i)));
			GDataRequest request = service.createFeedRequest(feedUrl);
			FileOutputStream outStream = null;
			InputStream inStream = null;
			try {
				request.execute();
				inStream = request.getResponseStream();
				outStream = new FileOutputStream(outputFiles.get(i));
				int c;
				while ((c = inStream.read()) != -1)
					outStream.write(c);
			} finally {
				if (inStream != null) {
					inStream.close();
				}
				if (outStream != null) {
					outStream.flush();
					outStream.close();
				}
			}
		}
	}
}
