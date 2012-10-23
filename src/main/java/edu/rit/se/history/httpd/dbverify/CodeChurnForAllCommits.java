package edu.rit.se.history.httpd.dbverify;

import org.chaoticbits.devactivity.testutil.dbverify.SQLCountDBVerify;

public class CodeChurnForAllCommits extends SQLCountDBVerify {

	public CodeChurnForAllCommits() {
		super("All commits should have code churn loaded", 0,
				"SELECT COUNT(*) FROM GitLogFiles WHERE LinesInserted IS NULL");
	}

}
