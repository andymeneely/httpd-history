package edu.rit.se.history.httpd.mailinglistparse;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;

public class MailingList {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		String source = "/Users/abhishekdale/Documents/Mailing List/200201.gz";
		String destination = "/Users/abhishekdale/Documents/Mailing List/";
		String password = "password";

		try {
			ZipFile zipFile = new ZipFile(source);
			System.out.println("Hello");
			if (zipFile.isEncrypted()) {
				zipFile.setPassword(password);
			}
			zipFile.extractAll(destination);
		} catch (ZipException e) {
			e.printStackTrace();
		}
	}

}
