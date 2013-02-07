package edu.rit.se.history.httpd.dbverify;

import org.chaoticbits.devactivity.testutil.dbverify.SQLCountDBVerify;

public class AllCVEToGitInAnalysis extends SQLCountDBVerify {

	public AllCVEToGitInAnalysis() {
		super("Only a few known CVEToGit entries failed to join (non-source files, or N/A files)", 14,
				"SELECT COUNT(*) FROM CVEToGit c2g WHERE "
						+ "c2g.CommitIntroduced NOT IN (SELECT Commit FROM VulnIntroAll WHERE VulnIntro=\"Yes\")");
	}

}