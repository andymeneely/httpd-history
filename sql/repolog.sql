DROP TABLE IF EXISTS RepoLog;

CREATE TABLE RepoLog Engine=MyISAM PACK_KEYS=0
	SELECT  gl.AuthorDate, 
			gl.AuthorName, 
			gl.AuthorEmail, 
			gl.ReleaseVer, 
			glf.* 
	FROM GitLog gl INNER JOIN GitLogFiles glf ON gl.Commit=glf.Commit;

ALTER TABLE RepoLog ADD PRIMARY KEY (ID);
CREATE UNIQUE INDEX RepoLogID USING BTREE ON RepoLog(ID);
CREATE INDEX RepoLogAuthorDate USING BTREE ON RepoLog(AuthorDate);
CREATE INDEX RepoLogFilepath USING BTREE ON RepoLog(Filepath);
CREATE INDEX RepoLogComponent USING BTREE ON RepoLog(Component);
CREATE INDEX RepoLogAllThree USING BTREE ON RepoLog(Commit,Filepath,AuthorDate);

OPTIMIZE TABLE RepoLog;



