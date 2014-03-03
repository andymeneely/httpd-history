package edu.rit.se.history.httpd.mailinglistparse;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import org.apache.commons.io.FileUtils;

public class MailingListLineMatcher {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		MailingListLineMatcher lineMatcher = new MailingListLineMatcher();
		lineMatcher.readFile("C:/mailinglist/VulnIntro.csv");
	}

	private void readFile(String path) {

		final String DELIMITER = ",";
		String line = "";

		BufferedReader fileReader;
		try {
			fileReader = new BufferedReader(new FileReader(path));

			// Read the csv file line by line
			while ((line = fileReader.readLine()) != null) {
				// Get all tokens available in line
				String[] tokens = line.split(DELIMITER);
				getEmailID(tokens[0], tokens[1], tokens[2]);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private void getEmailID(String comitId, String filename, String lineNumber) {

		File file = new File("C:/mailinglist/downloads/" + filename + ".txt");
		int startLineNumber = Integer.valueOf(lineNumber);
		try {
			
			
			while (true) {
				String startLine = (String) FileUtils.readLines(file).get(startLineNumber);
				if (startLine.toLowerCase().startsWith("message-id:")) {
					System.out.println(comitId + "," + startLine.split(" ")[1]);
					break;
				}
				startLineNumber++;
			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
