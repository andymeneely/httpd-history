/* repo.commit as commitIntroduced if we need it */
SELECT COUNT(DISTINCT cveFixed.cve) 
FROM
    CVEToGit AS cveFixed,
    CVEToGit AS cveIntro,
    RepoLog AS repo
WHERE
    cveFixed.filepath LIKE '%mod_proxy_http%' 
    AND cveIntro.filepath LIKE '%mod_proxy_http%' 
    AND repo.filepath LIKE '%mod_proxy_http%' 
    AND (cveFixed.CommitIntroduced = repo.commit) 
    AND repo.authordate < str_to_date('01/01/2009', '%d/%m/%Y') 
    AND cveIntro.cve in (SELECT cve FROM CVEToGit AS cveFixed,
            				RepoLog AS repo
        				WHERE
            				cveFixed.commitFixed = repo.commit 
            				AND (repo.authordate IS NULL OR repo.authordate > str_to_date('01/01/2009', '%d/%m/%Y')));
/*Query for recent churn*/
DROP VIEW IF EXISTS RecentChurn_Jun07;
CREATE VIEW RecentChurn_Jun07 AS SELECT RepoLog.filepath AS 'File', (sum(Files.LinesInserted)-sum(Files.LinesDeleted)) AS 'RecentChurn'
FROM httpdhistory.GitLogFiles AS Files, httpdhistory.RepoLog
WHERE RepoLog.filepath in (SELECT filepath from httpdhistory.GitLogFiles)
    AND RepoLog.authordate>str_to_date('01/02/2007', '%d/%m/%Y')
    AND RepoLog.authordate<str_to_date('01/06/2007', '%d/%m/%Y')
    AND RepoLog.commit=Files.commit GROUP BY RepoLog.filepath;