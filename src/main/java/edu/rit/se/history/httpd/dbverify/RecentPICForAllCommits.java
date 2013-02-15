package edu.rit.se.history.httpd.dbverify;

import org.chaoticbits.devactivity.testutil.dbverify.SQLCountDBVerify;

public class RecentPICForAllCommits extends SQLCountDBVerify {

	public RecentPICForAllCommits() {
		super("Recent Percent Interaction Churn metric is incomplete", 0,
				"SELECT COUNT(*) FROM GitLogFiles WHERE LinesInserted IS NULL");
	}

}
