package edu.rit.se.history.httpd;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Properties;

import org.apache.log4j.xml.DOMConfigurator;
import org.chaoticbits.devactivity.DBUtil;
import org.chaoticbits.devactivity.PropsLoader;
import org.chaoticbits.devactivity.testutil.dbverify.DBVerifyRunner;

import com.google.gdata.util.ServiceException;

import edu.rit.se.history.httpd.analysis.BayesianPrediction;
import edu.rit.se.history.httpd.analysis.Counterparts;
import edu.rit.se.history.httpd.analysis.ProjectChurn;
import edu.rit.se.history.httpd.analysis.RecentChurn;
import edu.rit.se.history.httpd.analysis.TimelineTables;
import edu.rit.se.history.httpd.dbverify.CodeChurnForAllCommits;
import edu.rit.se.history.httpd.filter.FilepathFilters;
import edu.rit.se.history.httpd.parse.CVEToGit;
import edu.rit.se.history.httpd.parse.CVEsParser;
import edu.rit.se.history.httpd.parse.ChurnParser;
import edu.rit.se.history.httpd.parse.FileListingParser;
import edu.rit.se.history.httpd.parse.GitLogParser;
import edu.rit.se.history.httpd.parse.GitRelease;
import edu.rit.se.history.httpd.parse.GroundedTheoryResultsParser;
import edu.rit.se.history.httpd.parse.ReleaseParser;
import edu.rit.se.history.httpd.parse.SLOCParser;
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

	public Properties getProps() {
		return props;
	}

	public RebuildHistory() throws Exception {
		this.props = setUpProps();
		this.dbUtil = setUpDB(props);
	}

	public void run() throws Exception {
//		downloadGoogleDocs(props);
//		rebuildSchema(dbUtil);
//		loadGitLog(dbUtil, props);
//		filterGitLog(dbUtil);
//		// loadCVEToGit(dbUtil, props);
//		optimizeTables(dbUtil);
//		loadChurn(dbUtil, props);
//		// computeChurn(dbUtil,props);
//		loadReleaseHistory(dbUtil, props);
//		loadGitRelease(dbUtil);
//		// loadFileListing(dbUtil, props);
//		// loadGroundedTheoryResults(dbUtil, props);
//		// loadCVEs(dbUtil, props);
//		// timeline(dbUtil, props);
//		verify(dbUtil);
//		visualizeVulnerabilitySeasons();
//		// buildAnalysis(dbUtil, props);
//		// prediction();
		generateCounterparts( dbUtil, props );
		log.info("Done.");
	}

	private void generateCounterparts( DBUtil dbUtil, Properties props )
			throws Exception {
		log.info( "Generating counterparts.." );
		new Counterparts(dbUtil,props).generate(); //.generate(1000)  //seeded
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
		export.add(props.getProperty("history.cveintro.googledoc"),
				new File(datadir, props.getProperty("history.cveintro.local")));
		export.downloadCSVs(props.getProperty("google.username"), props.getProperty("google.password"));
	}

	private void rebuildSchema(DBUtil dbUtil) throws FileNotFoundException, SQLException, IOException {
		log.info("Rebuilding database schema...");
		dbUtil.executeSQLFile("sql/createTables.sql");
	}

	private void loadGitLog(DBUtil dbUtil, Properties props) throws Exception {
		log.info("Loading the Git Log into database...");
		new GitLogParser().parse(dbUtil, new File(datadir, props.getProperty("history.gitlog")));
	}

	private void filterGitLog(DBUtil dbUtil) throws Exception {
		log.info("Filtering the git log...");
		dbUtil.executeSQLFile("sql/filter-gitlog.sql");
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

	private void loadChurn(DBUtil dbUtil, Properties props) throws Exception {
		log.info("Parsing churn data...");
		new ChurnParser().parse(dbUtil, new File(datadir, props.getProperty("history.gitlog.churn")));
	}

	private void computeChurn(DBUtil dbUtil, Properties props) throws Exception {
		log.info("Computing recent churn...");
		new RecentChurn().compute(dbUtil, Long.parseLong(props.getProperty("history.timeline.step")));
		log.info("Computing project churn...");
		new ProjectChurn().compute(dbUtil, Long.parseLong(props.getProperty("history.timeline.step")));
	}

	private void filterSVNLog(DBUtil dbUtil, Properties props) {
		throw new IllegalStateException("unimplemented!");
	}

	private void loadCVEToGit(DBUtil dbUtil, Properties props) throws Exception {
		log.info("Parsing CVE to Git tracings...");
		new CVEToGit().parse(dbUtil, new File(datadir, props.getProperty("history.cveintro.local")));
	}

	private void optimizeTables(DBUtil dbUtil) throws FileNotFoundException, SQLException, IOException {
		log.info("Optimizing tables...");
		dbUtil.executeSQLFile("sql/optimizeTables.sql");
	}

	private void timeline(DBUtil dbUtil, Properties props) throws Exception {
		log.info("Constructing file-level timelines...");
		new TimelineTables(dbUtil).build(Timestamp.valueOf(props.getProperty("history.timeline.start")),
				Timestamp.valueOf(props.getProperty("history.timeline.stop")),
				Long.valueOf(props.getProperty("history.timeline.step")));
	}

	private void verify(DBUtil dbUtil) throws Exception {
		log.info("Running db verifications...");
		DBVerifyRunner runner = new DBVerifyRunner(dbUtil);
		runner.add(new CodeChurnForAllCommits());
		runner.run();
	}

	private void visualizeVulnerabilitySeasons() throws Exception {
		log.info("Building visualization of vulnerability seasons...");
		// new ActiveVulnHeatMap().makeVisual(dbUtil, props);
	}

	private void buildAnalysis(DBUtil dbUtil, Properties props) throws FileNotFoundException, SQLException, IOException {
		dbUtil.executeSQLFile("sql/analysis.sql");
	}

	private void prediction() throws Exception {
		log.info("Prediction analysis...");
		new BayesianPrediction(dbUtil).run();
	}

	private void loadReleaseHistory(DBUtil dbUtil, Properties props) throws Exception {
		log.info("Loading HTTPD Release history into database...");
		new ReleaseParser().parse(dbUtil, new File(datadir, props.getProperty("history.release")));
	}

	private void loadGitRelease(DBUtil dbUtil2) throws Exception {
		log.info("Updating Gitlog Major Release...");
		new GitRelease().load(dbUtil);

	}
}
