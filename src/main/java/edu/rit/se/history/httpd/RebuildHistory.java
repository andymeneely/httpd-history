package edu.rit.se.history.httpd;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Properties;

import org.apache.log4j.xml.DOMConfigurator;
import org.chaoticbits.devactivity.DBUtil;
import org.chaoticbits.devactivity.PropsLoader;
import org.chaoticbits.devactivity.devnetwork.factory.LoadSVNtoDB;

import com.google.gdata.util.ServiceException;

import edu.rit.se.history.httpd.filter.FilepathFilters;
import edu.rit.se.history.httpd.parse.CVEsParser;
import edu.rit.se.history.httpd.parse.FileListingParser;
import edu.rit.se.history.httpd.parse.GroundedTheoryResultsParser;
import edu.rit.se.history.httpd.parse.SLOCParser;
import edu.rit.se.history.httpd.parse.VulnSVNFixParser;
import edu.rit.se.history.httpd.scrapers.GoogleDocExport;

public class RebuildHistory {
	private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(RebuildHistory.class);

	private final DBUtil dbUtil;
	private final Properties props;
	private File datadir;

	public static void main(String[] args) throws Exception {
		new RebuildHistory().run();
	}

	public DBUtil getDbUtil() {
		return dbUtil;
	}

	public RebuildHistory() throws Exception {
		this.props = setUpProps();
		this.dbUtil = setUpDB(props);
	}

	public void run() throws Exception {
		// downloadGoogleDocs(props);
		rebuildSchema(dbUtil);
		loadSVNXML(dbUtil, props);
		// filterSVNLog(dbUtil, props);
		loadFileListing(dbUtil, props);
		loadVulnerabilitiesToSVN(dbUtil, props);
		// loadGroundedTheoryResults(dbUtil, props);
		loadCVEs(dbUtil, props);
		optimizeTables(dbUtil);
		loadSLOC(dbUtil, props);
		// buildAnalysis(dbUtil, props);
		log.info("Done.");
	}

	private Properties setUpProps() throws IOException {
		Properties props = PropsLoader.getProperties("httpdhistory.properties");
		DOMConfigurator.configure("log4j.properties.xml");
		datadir = new File(props.getProperty("history.datadir"));
		return props;
	}

	private DBUtil setUpDB(Properties props) throws ClassNotFoundException {
		Class.forName("com.mysql.jdbc.Driver");
		DBUtil dbUtil = new DBUtil(props.getProperty("history.dbuser"), props.getProperty("history.dbpw"),
				props.getProperty("history.dburl"));
		return dbUtil;
	}

	private void downloadGoogleDocs(Properties props) throws IOException, ServiceException {
		log.info("Downloading the latest GoogleDocs...");
		GoogleDocExport export = new GoogleDocExport();
		export.add(props.getProperty("history.cves.googledoc"),
				new File(datadir, props.getProperty("history.cves.local")));
		export.add(props.getProperty("history.cve2svn.googledoc"),
				new File(datadir, props.getProperty("history.cve2svn.local")));
		export.add(props.getProperty("history.groundedtheory.googledoc"),
				new File(datadir, props.getProperty("history.groundedtheory.local")));
		export.downloadCSVs(props.getProperty("google.username"), props.getProperty("google.password"));
	}

	private void rebuildSchema(DBUtil dbUtil) throws FileNotFoundException, SQLException, IOException {
		log.info("Rebuilding database schema...");
		dbUtil.executeSQLFile("sql/createTables.sql");
	}

	private void loadSVNXML(DBUtil dbUtil, Properties props) throws Exception {
		log.info("Loading the SVN XML into database...");
		new LoadSVNtoDB(dbUtil, new File(datadir, props.getProperty("history.svnlogxml.httpd"))).run();
		new LoadSVNtoDB(dbUtil, new File(datadir, props.getProperty("history.svnlogxml.apr"))).run();
	}

	private void loadFileListing(DBUtil dbUtil, Properties props) throws FileNotFoundException, SQLException {
		log.info("Parsing release files for HTTPD 2.2.0...");
		new FileListingParser().parse(dbUtil, new File(datadir, props.getProperty("history.filelisting.v22")), "2.2.0");
		log.info("Filtering out filepaths for all versions...");
		new FilepathFilters().filter(dbUtil, new File("filters/ignored-filepaths.txt"));
	}

	private void loadSLOC(DBUtil dbUtil2, Properties props2) throws SQLException, IOException {
		log.info("Loading SLOC counts for HTTPD 2.2.0...");
		new SLOCParser().parse(dbUtil, new File(datadir, props.getProperty("history.sloc.v22")), "2.2.0");
	}

	private void loadGroundedTheoryResults(DBUtil dbUtil, Properties props) throws Exception {
		log.info("Parsing grounded theory results...");
		new GroundedTheoryResultsParser().parse(dbUtil,
				new File(datadir, props.getProperty("history.groundedtheory.local")));
	}

	private void loadCVEs(DBUtil dbUtil, Properties props) throws Exception {
		log.info("Parsing CVE details...");
		new CVEsParser().parse(dbUtil, new File(datadir, props.getProperty("history.cves.local")));
	}

	private void filterSVNLog(DBUtil dbUtil, Properties props) {
		throw new IllegalStateException("unimplemented!");
	}

	private void loadVulnerabilitiesToSVN(DBUtil dbUtil, Properties props) throws Exception {
		log.info("Parsing CVE to SVN fixes...");
		new VulnSVNFixParser().parse(dbUtil, new File(datadir, props.getProperty("history.cve2svn.local")));
	}

	private void optimizeTables(DBUtil dbUtil) throws FileNotFoundException, SQLException, IOException {
		log.info("Optimizing tables...");
		dbUtil.executeSQLFile("sql/optimizeTables.sql");
	}

	private void buildAnalysis(DBUtil dbUtil, Properties props) throws FileNotFoundException, SQLException, IOException {
		dbUtil.executeSQLFile("sql/analysis.sql");
	}
}
