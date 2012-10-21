select distinct
    cveFixed.cve, repo.commit
from
    CVEToGit as cveFixed,
    CVEToGit as cveIntro,
    RepoLog as repo
where
    cveFixed.filepath like '%mod_proxy_http%' and cveIntro.filepath like '%mod_proxy_http%' and repo.filepath like '%mod_proxy_http%' and (cveFixed.CommitIntroduced = repo.commit) and repo.authordate < str_to_date('01/01/2009', '%d/%m/%Y') and cveIntro.cve in (select 
            cve
        from
            CVEToGit as cveFixed,
            RepoLog as repo
        where
            cveFixed.commitFixed = repo.commit and (repo.authordate is null or repo.authordate > str_to_date('01/01/2009', '%d/%m/%Y')));