package edu.rit.se.history.httpd.dbverify;

import org.chaoticbits.devactivity.testutil.dbverify.SQLCountDBVerify;

public class RecentChurnForAllCommit extends SQLCountDBVerify {

	public RecentChurnForAllCommit() {
		super("All commits should have code churn loaded", 0,
				"SELECT COUNT(*) FROM GitLogFiles WHERE recentChurn IS NULL");
	}

}
