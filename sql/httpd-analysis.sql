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