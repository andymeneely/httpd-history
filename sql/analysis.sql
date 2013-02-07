DROP TABLE IF EXISTS VulnIntroAll;

CREATE TABLE VulnIntroAll AS
	SELECT	r.*,
			r.LinesDeleted + r.LinesInserted as TotalChurn,
			IF(r.LinesDeleted=0, 0, r.LinesDeletedOther/r.LinesDeleted) as PercIntChurn,
			IF(c2g.CommitIntroduced IS NOT NULL, "Yes", "No") VulnIntro
	FROM RepoLog r LEFT OUTER JOIN CVEToGit c2g ON (r.Commit=c2g.CommitIntroduced AND r.Filepath=c2g.Filepath)
;
