package edu.rit.se.history.httpd.dbverify;

import org.chaoticbits.devactivity.testutil.dbverify.SQLCountDBVerify;

public class ComponentForAllFilepath extends SQLCountDBVerify {

	public ComponentForAllFilepath() {
		super("All filepath should have corresponding component", 0,
				"SELECT COUNT(*) FROM GitLogFiles WHERE component IS NULL");
	}

}
