package edu.rit.se.history.httpd.mailinglistparse;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.List;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import org.apache.commons.io.FileUtils;
import org.bson.NewBSONDecoder;

import weka.core.converters.CSVLoader;
import au.com.bytecode.opencsv.CSVReader;

import com.google.gdata.util.ServiceException;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;

import edu.rit.se.history.httpd.scrapers.GoogleDocExport;

public class MailingListLineMatcher {

	public static void main(String[] args) {

		MailingListLineMatcher lineMatcher = new MailingListLineMatcher();		
		lineMatcher.readFile("C:/mailinglist/VulnIntro.csv");
	}

	private DBCollection emailCollection;

	private void readFile(String path) {

		try {
			CSVReader csvReader = new CSVReader(new FileReader(path));

			List<String[]> content = csvReader.readAll();

			for (Object object : content) {
				String[] row = (String[]) object;

				if (!row[4].isEmpty() && !row[4].equals("Message ID")) {
					normalize(row[4], row[3], row[5], row[6], row[7], row[8]);
				}
			}

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void normalize(String messageId, String vcc, String preCommit, String discussion, String securityCVE,
			String generalSecurity) {

		messageId = messageId.trim();
		
		// normalized variables.
		boolean nVCC = false;
		boolean nPreCommit = false;
		String nDiscussion = "";
		boolean nSecurity = false;
		boolean nSecurityCVE = false;
		boolean nGeneralSecurity = false;
		
		
		// normalization process
		if (vcc.trim().toLowerCase().equals("yes"))
			nVCC = true;
		
		if (preCommit.trim().toLowerCase().equals("yes"))
			nPreCommit = true;
		
		if (securityCVE.trim().toLowerCase().equals("yes"))
			nSecurityCVE = true;
		
		if (generalSecurity.trim().toLowerCase().equals("yes"))
			nGeneralSecurity = true;

		nSecurity = nSecurityCVE || nGeneralSecurity;
		
		if (discussion.trim().toLowerCase().equals("specific")){
			nDiscussion = "SPECIFIC";
		}else{
			nDiscussion = "BROAD";
		}
		
		//update records
		JDBMethods mysql = new JDBMethods("localhost", "mailinglist", "student");
		mysql.updateWithCSV(messageId, nVCC, nPreCommit, nDiscussion, nSecurity, nSecurityCVE, nGeneralSecurity);
	}
}
