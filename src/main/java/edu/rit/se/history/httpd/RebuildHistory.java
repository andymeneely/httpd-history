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
import edu.rit.se.history.httpd.parse.VulnerabilitiesToFilesParser;
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
		downloadGoogleDocs(props);
		rebuildSchema(dbUtil);
		loadSVNXML(dbUtil, props);
		// filterSVNLog(dbUtil, props);
		// loadFileListing(dbUtil, props);
		loadVulnerabilitiesToFiles(dbUtil, props);
		// loadGroundedTheoryResults(dbUtil, props);
		loadCVEs(dbUtil, props);
		optimizeTables(dbUtil);
		// loadSLOC(dbUtil, props);
		buildAnalysis(dbUtil, props);
		log.info("Done.");
	}

	private Properties setUpProps() throws IOException {
		Properties props = PropsLoader.getProperties("tomcathistory.properties");
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
		export.add(props.getProperty("history.cve2files.googledoc"),
				new File(datadir, props.getProperty("history.cve2files.local")));
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
		String file = props.getProperty("history.svnlogxml");
		new LoadSVNtoDB(dbUtil, new File(datadir, file)).run();
	}

	private void loadFileListing(DBUtil dbUtil, Properties props) throws FileNotFoundException, SQLException {
		log.info("Parsing release files for Tomcat 5.5.0...");
		new FileListingParser().parse(dbUtil, new File(datadir, props.getProperty("history.filelisting.v5")), "5.5.0");
		log.info("Parsing release files for Tomcat 6.0.0...");
		new FileListingParser().parse(dbUtil, new File(datadir, props.getProperty("history.filelisting.v6")), "6.0.0");
		log.info("Parsing release files for Tomcat 7.0.0...");
		new FileListingParser().parse(dbUtil, new File(datadir, props.getProperty("history.filelisting.v7")), "7.0.0");
		log.info("Filtering out filepaths for all Tomcat versions...");
		new FilepathFilters().filter(dbUtil, new File("filters/ignored-filepaths.txt"));
	}

	private void loadSLOC(DBUtil dbUtil2, Properties props2) throws SQLException, IOException {
		log.info("Loading SLOC counts for Tomcat 5.5.0...");
		new SLOCParser().parse(dbUtil, new File(datadir, props.getProperty("history.sloc.v5")), "5.5.0");
		log.info("Loading SLOC counts for Tomcat 6.0.0...");
		new SLOCParser().parse(dbUtil, new File(datadir, props.getProperty("history.sloc.v6")), "6.0.0");
		log.info("Loading SLOC counts for Tomcat 7.0.0...");
		new SLOCParser().parse(dbUtil, new File(datadir, props.getProperty("history.sloc.v7")), "7.0.0");
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

	private void loadVulnerabilitiesToFiles(DBUtil dbUtil, Properties props) throws Exception {
		log.info("Parsing CVE to Files...");
		new VulnerabilitiesToFilesParser().parse(dbUtil,
				new File(datadir, props.getProperty("history.cve2files.local")));
	}

	private void optimizeTables(DBUtil dbUtil) throws FileNotFoundException, SQLException, IOException {
		log.info("Optimizing tables...");
		dbUtil.executeSQLFile("sql/optimizeTables.sql");
	}

	private void buildAnalysis(DBUtil dbUtil, Properties props) throws FileNotFoundException, SQLException, IOException {
		dbUtil.executeSQLFile("sql/analysis.sql");
	}
}
