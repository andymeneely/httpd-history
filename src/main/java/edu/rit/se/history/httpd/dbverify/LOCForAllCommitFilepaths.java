package edu.rit.se.history.httpd.dbverify;

import org.chaoticbits.devactivity.testutil.dbverify.SQLCountDBVerify;

public class LOCForAllCommitFilepaths extends SQLCountDBVerify {

	public LOCForAllCommitFilepaths() {
		super("All filepaths for commits should have LOC", 0,
				"SELECT COUNT(*) FROM GitLogFiles WHERE LinesOfCode IS NULL");
	}

}
