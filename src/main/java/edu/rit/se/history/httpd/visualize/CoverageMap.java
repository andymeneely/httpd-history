package edu.rit.se.history.httpd.visualize;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

import javax.imageio.ImageIO;

import org.apache.log4j.xml.DOMConfigurator;

import au.com.bytecode.opencsv.CSVReader;

public class CoverageMap {
	private static final int IMG_WIDTH = 600;
	private static final int IMG_HEIGHT = 800;
	private static final int CELL_HEIGHT = 6;
	private static final int CELL_WIDTH = 20;
	private static final int CELL_VSPACE = 2;
	private static final int CELL_HSPACE = 10;
	private static final String PROJECT = "OpenSSL";
	private static final int INIT_X = 50;
	private static final int INIT_Y = 50;

	private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(CoverageMap.class);

	public static void main(String[] args) throws Exception {
		DOMConfigurator.configure("log4j.properties.xml");
		CSVReader reader = new CSVReader(new FileReader(new File("C:/local/data/emse2013data/coverage/" + PROJECT
				+ "-Coverage.csv")), '\t');

		List<String[]> qs = reader.readAll();
		reader.close();
		new CoverageMap().makeVisual(qs);
		System.out.println("Done.");
	}

	public void makeVisual(List<String[]> cves) throws IOException, SQLException {
		BufferedImage bi = new BufferedImage(IMG_WIDTH, IMG_HEIGHT, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g2d = bi.createGraphics();
		int x = INIT_X;
		int y = INIT_Y;
		g2d.setColor(Color.WHITE);
		g2d.fillRect(0, 0, IMG_WIDTH, IMG_HEIGHT);
		g2d.setColor(Color.DARK_GRAY);
		for (String[] cve : cves) {
			for (int question = 2; question < 10; question++) {
				log.info("Doing " + cve[1]);
				if (cve[question].length() > 0)
					g2d.fillRect(x, y, CELL_WIDTH, CELL_HEIGHT);
				x += CELL_WIDTH + CELL_HSPACE;
			}
			y += CELL_HEIGHT + CELL_VSPACE;
			x = INIT_X;
		}
		ImageIO.write(bi, "PNG", new File("images/coverage.png"));

	}
}
