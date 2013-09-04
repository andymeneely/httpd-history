DROP TABLE IF EXISTS VulnIntroAll;
DROP TABLE IF EXISTS VulnIntroByCommit;

CREATE TABLE VulnIntroAll AS
	SELECT	r.*,
			r.LinesDeleted + r.LinesInserted as TotalChurn,
			(r.LinesDeleted + r.LinesInserted)/r.LinesOfCode as RelativeChurn,
			r.LinesDeletedOther/r.LinesDeleted as PercIntChurn,
			IF(c2g.CommitIntroduced IS NOT NULL, "Yes", "No") VulnIntro,
			IF(c2g.IsBaselineCommit IS NOT NULL, c2g.IsBaselineCommit, "N/A") IsBaselineCommit
	FROM GitLogFiles r LEFT OUTER JOIN CVEToGit c2g ON (r.Commit=c2g.CommitIntroduced AND r.Filepath=c2g.Filepath)
;

CREATE TABLE VulnIntroByCommit AS
	SELECT 	Commit,
			Sum(via.LinesInserted)+Sum(via.LinesDeleted) TotalChurn,
			Avg((LinesInserted+LinesDeleted)/LinesOfCode) AvgRelChurn,
			Avg(PercIntChurn) AvgPIC,
			Avg(NumAuthorsAffected) AvgNAA,
			Avg(NumEffectiveAuthors) AvgNEA,
			via.VulnIntro
    FROM VulnIntroAll via
    GROUP BY Commit
;
