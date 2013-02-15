package edu.rit.se.history.httpd.dbverify;

import org.chaoticbits.devactivity.testutil.dbverify.SQLCountDBVerify;

public class PeachForAllCommit extends SQLCountDBVerify {

	public PeachForAllCommit() {
		super("PEACh not computed", 0,
				"SELECT COUNT(*) FROM GitLogFiles WHERE peach IS NULL");
	}

}
