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
DROP TABLE IF EXISTS RecentChurn;
CREATE TABLE RecentChurn AS
SELECT 
    RepoLog.filepath,
    RepoLog.authordate,
    RepoLog.commit,
    SUM(GitLogFiles.LinesInserted) + SUM(GitLogFiles.LinesDeleted),
    COUNT(DISTINCT GitLogFiles.commit),
    GROUP_CONCAT(GitLogFiles.commit)
FROM
    RepoLog,
    GitLogFiles
WHERE
    RepoLog.filepath = GitLogFiles.filepath and 
    RepoLog.authordate > str_to_date('01/03/2007', '%d/%m/%Y') and 
    RepoLog.authordate < DATE_ADD(str_to_date('01/03/2007', '%d/%m/%Y'),
        INTERVAL 30 DAY) AND 
    RepoLog.commit = GitLogFiles.commit
GROUP BY RepoLog.filepath;