/* Number of vulnerable files per component */
select Component, count(component) "Number of Vulnerable Files" 
from `tomcathistory`.`cvenonsvnfix` 
group by Component;

/* CVE and number of components affected */
SELECT CVE, count(distinct Component) "Number of Components Affected" 
FROM `tomcathistory`.`cvenonsvnfix` 
group by CVE;

/* CVE affecting more than one component */
SELECT CVE, count(distinct Component) NumComponentsAffected
FROM `tomcathistory`.`cvenonsvnfix`
group by CVE
having count(distinct Component) > 1;

/* All subsystems */
SELECT concat(Component, "/", UtilComponent) AS Subsystem, CVE 
         FROM cvenonsvnfix

/* All subsystems affected by at least one vulnerablity */
SELECT Subsystem, count(DISTINCT CVE) NumCVEs
    FROM
        (SELECT concat(Component, "/", UtilComponent) AS Subsystem, CVE 
         FROM cvenonsvnfix) AS Subsystems
    GROUP BY Subsystem
    
   