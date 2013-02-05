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
import edu.rit.se.history.httpd.analysis.ComponentChurn;
import edu.rit.se.history.httpd.analysis.Counterparts;
import edu.rit.se.history.httpd.analysis.Peach;
import edu.rit.se.history.httpd.analysis.RecentAuthorsAffected;
import edu.rit.se.history.httpd.analysis.RecentChurn;
import edu.rit.se.history.httpd.analysis.RecentPIC;
import edu.rit.se.history.httpd.analysis.TimelineTables;
import edu.rit.se.history.httpd.dbverify.CodeChurnForAllCommits;
import edu.rit.se.history.httpd.dbverify.ComponentForAllFilepath;
import edu.rit.se.history.httpd.parse.CVEToGit;
import edu.rit.se.history.httpd.parse.CVEsParser;
import edu.rit.se.history.httpd.parse.ChurnParser;
import edu.rit.se.history.httpd.parse.ComponentParser;
import edu.rit.se.history.httpd.parse.GitLogParser;
import edu.rit.se.history.httpd.parse.GitRelease;
import edu.rit.se.history.httpd.parse.GitlogfilesComponent;
import edu.rit.se.history.httpd.parse.ReleaseParser;
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
		/* --- DOWNLOAD STUFF --- */
		// downloadGoogleDocs(props); //Nobody but Andy really needs to run this
		/* --- CLEAN EVERYTHING --- */
		rebuildSchema();
		/* --- LOAD STUFF --- */
		// loadCVEs(dbUtil, props);
		loadGitLog();
		loadComponents();
		loadGitRelease();
		loadReleaseHistory();
		// loadCVEToGit(dbUtil, props);
		/* --- OPTIMIZE & INDEX TABLES --- */
		optimizeTables();
		/* --- COMPUTE & UPDATE TABLES --- */
		updateChurn();
		updateComponent();
		computeRecentChurn();
		/* --- VERIFY --- */
		verify();
		/* --- ANALYZE --- */
		timeline();
		visualizeVulnerabilitySeasons();
		generateCounterparts();
		buildAnalysis();
		// prediction();
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
		export.add(props.getProperty("history.cveintro.googledoc"),
				new File(datadir, props.getProperty("history.cveintro.local")));
		export.downloadCSVs(props.getProperty("google.username"), props.getProperty("google.password"));
	}

	private void rebuildSchema() throws FileNotFoundException, SQLException, IOException {
		log.info("Rebuilding database schema...");
		dbUtil.executeSQLFile("sql/createTables.sql");
	}

	private void loadGitLog() throws Exception {
		log.info("Loading the Git Log into database...");
		new GitLogParser().parse(dbUtil, new File(datadir, props.getProperty("history.gitlog")));
		log.info("Filtering the git log...");
		dbUtil.executeSQLFile("sql/filter-gitlog.sql");
	}

	private void loadReleaseHistory() throws Exception {
		log.info("Loading release history...");
		new ReleaseParser().parse(dbUtil, new File(datadir, props.getProperty("history.release")));
	}

	private void loadGitRelease() throws Exception {
		log.info("Updating major releases according to dates...");
		new GitRelease().load(dbUtil);
	}

	private void loadCVEs(DBUtil dbUtil, Properties props) throws Exception {
		log.info("Parsing CVE details...");
		new CVEsParser().parse(dbUtil, new File(datadir, props.getProperty("history.cves.local")));
	}

	private void updateChurn() throws Exception {
		log.info("Parsing churn data...");
		new ChurnParser().parse(dbUtil, new File(datadir, props.getProperty("history.gitlog.churn")));
	}

	private void computeRecentChurn() throws Exception {
		log.info("Computing recent churn...");
		new RecentChurn().compute(dbUtil, Long.parseLong(props.getProperty("history.churn.recent.step")));
		log.info("Computing recent PIC...");
		new RecentPIC().compute(dbUtil, Long.parseLong(props.getProperty("history.churn.recent.step")));
		log.info("Computing recent Authors Affected...");
		new RecentAuthorsAffected().compute(dbUtil, Long.parseLong(props.getProperty("history.churn.recent.step")));
		log.info("Computing PEACh metric...");
		new Peach().compute(dbUtil, Long.parseLong(props.getProperty("history.churn.recent.step")));
		log.info("Computing component churn...");
		new ComponentChurn().compute(dbUtil, Long.parseLong(props.getProperty("history.churn.recent.step")));
		// log.info("Computing project churn...");
		// new ProjectChurn().compute(dbUtil,
		// Long.parseLong(props.getProperty("history.churn.recent.step")));
	}

	private void loadCVEToGit(DBUtil dbUtil, Properties props) throws Exception {
		log.info("Parsing CVE to Git tracings...");
		new CVEToGit().parse(dbUtil, new File(datadir, props.getProperty("history.cveintro.local")));
	}

	private void optimizeTables() throws FileNotFoundException, SQLException, IOException {
		log.info("Optimizing tables...");
		dbUtil.executeSQLFile("sql/optimizeTables.sql");
	}

	private void timeline() throws Exception {
		log.info("Constructing file-level timelines...");
		new TimelineTables(dbUtil).build(Timestamp.valueOf(props.getProperty("history.timeline.start")),
				Timestamp.valueOf(props.getProperty("history.timeline.stop")),
				Long.valueOf(props.getProperty("history.timeline.step")));
	}

	private void verify() throws Exception {
		log.info("Running db verifications...");
		DBVerifyRunner runner = new DBVerifyRunner(dbUtil);
		runner.add(new CodeChurnForAllCommits());
		runner.add(new ComponentForAllFilepath());
		runner.run();
	}

	private void visualizeVulnerabilitySeasons() throws Exception {
		log.info("Building visualization of vulnerability seasons...");
		// new ActiveVulnHeatMap().makeVisual(dbUtil, props);
	}

	private void generateCounterparts() throws Exception {
		log.info("Generating counterparts..");
		new Counterparts(dbUtil, new File(datadir, props.getProperty("history.cveintro.local")), Integer.valueOf(props
				.getProperty("history.counterparts.num"))).generate(); // .generate(1000) //seeded
	}

	private void buildAnalysis() throws FileNotFoundException, SQLException, IOException {
		dbUtil.executeSQLFile("sql/analysis.sql");
	}

	private void prediction() throws Exception {
		log.info("Prediction analysis...");
		new BayesianPrediction(dbUtil).run();
	}

	private void loadComponents() throws Exception {
		log.info("Loading Component into database...");
		new ComponentParser().parse(dbUtil, new File(datadir, props.getProperty("history.component.paths")));
	}

	private void updateComponent() throws Exception {
		log.info("Updating Gitlogfiles Component...");
		new GitlogfilesComponent().update(dbUtil);

	}
}
