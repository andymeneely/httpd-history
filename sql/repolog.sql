DROP TABLE IF EXISTS RepoLog;

CREATE TABLE RepoLog Engine=MyISAM PACK_KEYS=0
	SELECT  gl.AuthorDate, 
			gl.AuthorName, 
			gl.AuthorEmail, 
			gl.ReleaseVer, 
			glf.* 
	FROM GitLog gl INNER JOIN GitLogFiles glf ON gl.Commit=glf.Commit;

CREATE UNIQUE INDEX RepoLogID USING BTREE ON RepoLog(ID);
CREATE INDEX RepoLogAuthorDate USING BTREE ON RepoLog(AuthorDate);
CREATE INDEX RepoLogFilepath USING BTREE ON RepoLog(Filepath);
CREATE INDEX RepoLogComponent USING BTREE ON RepoLog(Component);

OPTIMIZE TABLE RepoLog;

DROP TABLE IF EXISTS RepoLogAuthorsAffected;

CREATE TABLE RepoLogAuthorsAffected Engine=MyISAM PACK_KEYS=0
	SELECT  rl.*,
			aa.AuthorAffected
	FROM GitChurnAuthorsAffected aa INNER JOIN RepoLog rl ON aa.Commit=rl.Commit;

CREATE INDEX RepoLogAAAuthorDate USING BTREE ON RepoLogAuthorsAffected(AuthorDate);
CREATE INDEX RepoLogAAFilepath USING BTREE ON RepoLogAuthorsAffected(Filepath);
CREATE INDEX RepoLogAAComponent USING BTREE ON RepoLogAuthorsAffected(Component);

OPTIMIZE TABLE RepoLogAuthorsAffected;

