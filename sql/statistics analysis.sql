/*value returned=15*/
select count(distinct(component)) from componentswithfixes; 

/*value returned = 9
%age of distinct components involving input validation = 60
*/
select 
    count(distinct(components.component))
from
    tomcathistory.cvefixresults AS results, tomcathistory.assetscompromisedresults as assets,
    tomcathistory.componentswithfixes as components
where
    results.CVE = assets.CVE
and results.component = components.component
and assets.inputvalidation = 'Yes' 

/*
value returned = 8
%age of distinct components involving output cleansing = 53
*/
select 
    count(distinct(components.component))
from
    tomcathistory.cvefixresults AS results, tomcathistory.assetscompromisedresults as assets,
    tomcathistory.componentswithfixes as components
where
    results.CVE = assets.CVE
and results.component = components.component
and assets.outputcleansing = 'Yes'

/*value returned = 10*/
SELECT count(distinct(asset)) FROM tomcathistory.cvegroundedtheoryassets; 

/*number of cve's involving input validation = 16*/
SELECT count(*) FROM tomcathistory.cveresults where inputvalidation='Yes';
/*value returned = 1
%age of assets involving sample apps and input validation = 10
%age of assets compromised by sample apps 60 (6/10%)
%age of assets compromised by inputvalidation vulnerabilities = 80 (8/10)
%age of assets compromised by outputcleansing vulnerabilities = 70 (7/10)
8 and 7 might be seen as some of them involved both, so there is an intersection there
*/
select 
    count(asset)
from
    cvegroundedtheoryassets as assets,
    cveresults as cves
where
    cves.cve = assets.cve and assets.asset like '%sample app%' and cves.inputvalidation="Yes";

/*number of assets having sampleapps and outputcleansing = 5*/
select 
    count(asset)
from
    cvegroundedtheoryassets as assets,
    cveresults as cves
where
    cves.cve = assets.cve and assets.asset like '%sample app%' and cves.outputcleansing="Yes";
	
	
/*
distinct CWE id for tomcat
'Not Defined'--9
'79'--11
'22'--7
'200'--9
'264'--8
'16'--1
'20'--3
'255'--1
'119'--1
'399'--1
'189'--1
*/
SELECT distinct(cwe) FROM tomcathistory.cve;
SELECT count(cwe) FROM tomcathistory.cve where cwe="<placeholder from above comment>";

/* number of vulnerabilities in tomcat 5.5 --> 42*/
SELECT count(cve) FROM tomcathistory.cve where tomcat55='Yes';

/* number of vulnerabilities in tomcat 6 --> 38*/
SELECT count(cve) FROM tomcathistory.cve where tomcat6='Yes';

/* number of vulnerabilities in tomcat 7 --> 18*/
SELECT count(cve) FROM tomcathistory.cve where tomcat7='Yes';

/* number of file fixes for 5.5 --> 60*/
select 
    count(filepath)
from
    cvefixresults as files,
    cve as cves
where
    cves.cve = files.cve and files.tomcatrelease = '5.5.0';

/*number of distinct fixes in 5.5.0-- 44*/
select 
    count(distinct(filepath))
from
    cvefixresults as files,
    cve as cves
where
    cves.cve = files.cve and files.tomcatrelease = '5.5.0';
	
/* number of file fixes for 6.0--> 81*/
select 
    count(filepath)
from
    cvefixresults as files,
    cve as cves
where
    cves.cve = files.cve and files.tomcatrelease = '6.0.0';

/*number of files with at least one fix in 6.0.0-- 61*/
select 
    count(distinct(filepath))
from
    cvefixresults as files,
    cve as cves
where
    cves.cve = files.cve and files.tomcatrelease = '6.0.0';

/* number of file fixes for 7.0--> 82*/
select 
    count(filepath)
from
    cvefixresults as files,
    cve as cves
where
    cves.cve = files.cve and files.tomcatrelease = '7.0.0';
/*number of files with at least one fix (distinct fix) in 7.0.0-- 62*/
select 
    count(distinct(filepath))
from
    cvefixresults as files,
    cve as cves
where
    cves.cve = files.cve and files.tomcatrelease = '7.0.0';
	
/*X% of the vulnerabilities had the primary assets of Rendered User Interface, Authentication, or File System*/
SELECT 
    count(*)
FROM
    tomcathistory.cvegroundedtheoryassets
where
    asset = 'rendered ui' or asset = 'authentication' or asset = 'file system';

/* 11 vulnerabilities were both rendered ui and XSS*/
select 
    count(*)
from
    cve as cves,
    cvegroundedtheoryassets as assets
where
    cves.cve = assets.cve and cves.cwe = '79' and assets.asset = 'rendered ui';
	
/*tomcat5.5 file count=1850 vuln=60*/
SELECT count(*) FROM tomcathistory.fileresults where tomcatrelease = '5.5.0';
SELECT count(*) FROM tomcathistory.fileresults where tomcatrelease='5.5.0' and vuln='vulnerable';
/*6.0.0 file count=1339 vuln=81*/
SELECT count(*) FROM tomcathistory.fileresults where tomcatrelease = '6.0.0';
SELECT count(*) FROM tomcathistory.fileresults where tomcatrelease='6.0.0' and vuln='vulnerable';
/*7.0.0 file count=1625*/
SELECT count(*) FROM tomcathistory.fileresults where tomcatrelease = '7.0.0';
SELECT count(*) FROM tomcathistory.fileresults where tomcatrelease='7.0.0' and vuln='vulnerable';

/* most affected assets*/
select 
    count(asset) as assetcount, asset
from
    cvegroundedtheoryassets as assets group by asset order by assetcount desc;