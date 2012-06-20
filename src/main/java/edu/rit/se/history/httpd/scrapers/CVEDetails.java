package edu.rit.se.history.httpd.scrapers;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Scanner;

import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlTable;

public class CVEDetails {

	public static void main(String[] args) throws FailingHttpStatusCodeException, MalformedURLException, IOException {
		// DOMConfigurator.configure("log4j.properties.xml");
		WebClient client = new WebClient();
		Scanner scanner = new Scanner(new File("temp.txt"));
		while (scanner.hasNextLine()) {
			String cve = scanner.nextLine().trim();
			System.out.print(cve + "\t");
			HtmlPage page = client.getPage("http://www.cvedetails.com/cve-details.php?cve_id=" + cve);
			HtmlTable table = page.getHtmlElementById("cvssscorestable");
			for (int i = 0; i < 7; i++) {
				String cellText = table.getRow(i).getCell(1).asText().split("\\(")[0];
				System.out.print(cellText + "\t");
			}
			System.out.print("\n");
		}
		scanner.close();
		client.closeAllWindows();
	}
}
