package edu.rit.se.history.httpd.visualize;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;

import javax.imageio.ImageIO;

import org.chaoticbits.devactivity.DBUtil;

import com.mysql.jdbc.Connection;

import edu.rit.se.history.httpd.RebuildHistory;

public class ActiveVulnHeatMap {
	private static final int IMG_WIDTH = 800;
	private static final int IMG_HEIGHT = 600;
	private static final int TOTAL_START_Y = 9 * IMG_HEIGHT / 10;
	private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(ActiveVulnHeatMap.class);

	public static void main(String[] args) throws Exception {
		RebuildHistory history = new RebuildHistory();
		new ActiveVulnHeatMap().makeVisual(history.getDbUtil(), history.getProps());
		System.out.println("Done.");
	}

	public void makeVisual(DBUtil dbUtil, Properties props) throws IOException, SQLException {
		BufferedImage bi = new BufferedImage(IMG_WIDTH, IMG_HEIGHT, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g2d = bi.createGraphics();
		Connection conn = dbUtil.getConnection();
		ResultSet data = conn.createStatement().executeQuery(
				"SELECT Min(AtTime) Start, Max(AtTime) End, Count(DISTINCT Filepath) NumFiles, "
						+ "Count(Distinct AtTime) NumTimes, Max(NumCVES) MaxVulns FROM Timeline");
		data.next();
		int numFiles = data.getInt("NumFiles");
		int maxVuln = data.getInt("MaxVulns");
		int numTimes = data.getInt("NumTimes");
		fileRows(g2d, conn, numFiles, maxVuln, numTimes);
		totalRows(g2d, conn, numFiles, numTimes);
		conn.close();
		ImageIO.write(bi, "PNG", new File("images/vulnerability-seasons.png"));

	}

	private void fileRows(Graphics2D g2d, Connection conn, int numFiles, int maxVuln, int numTimes) throws SQLException {
		ResultSet rs = conn.createStatement().executeQuery("SELECT * FROM Timeline ORDER BY Filepath ASC, AtTime ASC");
		String lastFilepath = "";
		int x = 0;
		int y = 0;
		while (rs.next()) {
			g2d.setColor(getSingleFileColor(rs.getInt("NumCVEs"), maxVuln));
			if (!rs.getString("Filepath").equals(lastFilepath)) { // new file!
				x = 0;
				y += TOTAL_START_Y / numFiles;
			}
			g2d.fillRect(x, y, IMG_WIDTH / numTimes, TOTAL_START_Y / numFiles);
			x += IMG_WIDTH / numTimes;
			lastFilepath = rs.getString("Filepath");
		}
	}

	private Color getSingleFileColor(int numCVEs, int maxVuln) {
		if (numCVEs == 0) {
			return new Color(40, 31, 225); // dark blue
		}
		if (numCVEs == 1) {
			return new Color(225, 186, 31); // yellow
		}
		if (numCVEs == 2) {
			return new Color(225, 113, 31); // orange
		}
		if (numCVEs == 3) {
			return new Color(225, 40, 31); // red
		}
		if (numCVEs > 3) {
			return Color.WHITE;
		} else
			throw new IllegalArgumentException(" color not handled for numCVEs: " + numCVEs);
	}

	private void totalRows(Graphics2D g2d, Connection conn, int numFiles, int numTimes) throws SQLException {
		ResultSet data = conn.createStatement().executeQuery(
				"SELECT Max(TotalActive) MaxActive FROM (SELECT SUM(NumCVEs) TotalActive, AtTime "
						+ "FROM Timeline GROUP BY AtTime ORDER BY AtTime ASC) TotalTimeline");
		data.next();
		int maxActive = data.getInt("MaxActive");
		ResultSet rs = conn.createStatement().executeQuery(
				"SELECT SUM(NumCVEs) TotalActive, AtTime FROM Timeline GROUP BY AtTime ORDER BY AtTime ASC");
		int x = 0;
		int y = TOTAL_START_Y;
		int height = 9 * (IMG_HEIGHT - TOTAL_START_Y) / 10;
		while (rs.next()) {
			g2d.setColor(getTotalFileColor(rs.getInt("TotalActive"), maxActive));
			g2d.fillRect(x, y, IMG_WIDTH / numTimes, height);
			x += IMG_WIDTH / numTimes;
		}
	}

	private Color getTotalFileColor(int totalActive, int maxActive) {
		if (totalActive == 0) {
			return new Color(40, 31, 225); // dark blue
		}
		float percOfYellow = 1.0f - ((float) totalActive / (float) maxActive);
		int red = 40;
		int yellow = 186;
		int redness = (int) (red + (percOfYellow * (yellow - red)));
		return new Color(225, redness, 31); // gradient
	}
}
