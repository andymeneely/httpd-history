DROP VIEW IF EXISTS CVEResults;
DROP VIEW IF EXISTS FileResults;
DROP VIEW IF EXISTS CVEFixResults;
DROP VIEW IF EXISTS ComponentsWithFixes;
DROP VIEW IF EXISTS AssetsCompromisedResults;
DROP VIEW IF EXISTS CVEFixChurnTemp;
DROP VIEW IF EXISTS CVEFixChurn; 

CREATE VIEW CVEResults AS 
	SELECT 	c.CVE, 
			c.Tomcat55,
			c.Tomcat6,
			c.Tomcat7,
			c.CWE,
			c.CWETop25,
			c.CVSS,
			c.ConfidentialityImpact,
			c.AvailabilityImpact,
			c.AccessComplexity,
			c.AuthRequired,
			c.GainedAccess,
			cg.FixNewCode,
			cg.Cascades,
			cg.InputValidation,
			cg.OutputCleansing,
			cg.NonIOImprovedLogic,
			cg.DomainSpecific,
			cg.Regression,
			cg.SourceCode,
			cg.ConfigFile
	FROM CVE c INNER JOIN CVEGroundedTheory cg ON (c.cve=cg.cve)
;

CREATE VIEW AssetsCompromisedResults AS 
	SELECT 	c.CVE, 
			c.Tomcat55,
			c.Tomcat6,
			c.Tomcat7,
			c.CWE,
			c.CWETop25,
			c.CVSS,
			c.ConfidentialityImpact,
			c.AvailabilityImpact,
			c.AccessComplexity,
			c.AuthRequired,
			c.GainedAccess,
			cg.FixNewCode,
			cg.Cascades,
			cg.InputValidation,
			cg.OutputCleansing,
			cg.NonIOImprovedLogic,
			cg.DomainSpecific,
			cg.Regression,
			cg.SourceCode,
			cg.ConfigFile
	FROM CVE c INNER JOIN CVEGroundedTheory cg ON (c.cve=cg.cve)
;

CREATE VIEW CVEFixResults AS 
	SELECT  f.filepath,
		cf.CVE,
        f.TomcatRelease, 
        f.SLOCType, 
        f.SLOC,
        cf.Component,
        cf.UtilComponent,
        cf.SLOCAdded,
        cf.SLOCDeleted,
        IF(cf.CVE IS NULL, 'neutral','vulnerable') vuln
	FROM (filepaths f INNER JOIN CVENonSVNFix cf ON (f.filepath=cf.filepath))
;

CREATE VIEW CVEFixChurnTemp AS
	SELECT  CVE, 
            TomcatRelease,
            SUM(IF(SLOCType='Java', SLOCAdded, 0)) AS JavaSLOCAdded,
            SUM(IF(SLOCType='Java', SLOCDeleted, 0)) AS JavaSLOCDeleted,
            SUM(IF(SLOCType='Java', SLOC, 0)) AS JavaSLOC, 
            SUM(IF(SLOCType='JSP', SLOCAdded, 0)) AS JSPSLOCAdded,
            SUM(IF(SLOCType='JSP', SLOCDeleted, 0)) AS JSPSLOCDeleted,
            SUM(IF(SLOCType='JSP',SLOC, 0)) AS JSPSLOC,
            SUM(IF(SLOCType='XML', SLOCAdded, 0)) AS XMLSLOCAdded,
            SUM(IF(SLOCType='XML', SLOCDeleted, 0)) AS XMLSLOCDeleted
    FROM CVEFixResults
    GROUP BY CVE, TomcatRelease
;  

CREATE VIEW CVEFixChurn AS 
	SELECT CVE, TomcatRelease,
	    JavaSLOCAdded,JavaSLOCDeleted, JavaSLOC,
	    JavaSLOCAdded + JavaSLOCDeleted AS JavaChurn,
	    (JavaSLOCAdded + JavaSLOCDeleted) / JavaSLOC AS JavaRelChurn,
	    JSPSLOCAdded,JSPSLOCDeleted, JSPSLOC,
	    JSPSLOCAdded + JSPSLOCDeleted as JSPChurn,
	    (JSPSLOCAdded + JSPSLOCDeleted) / JSPSLOC AS JSPRelChurn
	FROM CVEFixChurnTemp	   
;

CREATE VIEW FileResults AS 
	SELECT  f.filepath,
        f.TomcatRelease, 
        f.SLOCType, 
        f.SLOC,
        cf.Component,
        cf.UtilComponent,
        cf.SLOCAdded,
        cf.SLOCDeleted,
        IF(cf.CVE IS NULL, 'neutral','vulnerable') vuln
	FROM (filepaths f LEFT OUTER JOIN CVENonSVNFix cf ON (f.filepath=cf.filepath))
;

CREATE VIEW ComponentsWithFixes AS 
	SELECT Component,
		      UtilComponent,
		      Count(DISTINCT CVE) NumCVEs,
		      Count(DISTINCT Filepath) NumFilepaths,
		      Sum(If(SLOCType='Java', SLOC, 0)) NumJavaSLOC,
		      Sum(If(SLOCType='C' OR SLOCType='C/C++ Header', SLOC, 0)) NumCSLOC,
		      Sum(If(SLOCType='JSP', SLOC, 0)) NumJSPSLOC,
		      Sum(If(SLOCType='XML', SLOC, 0)) NumXMLSLOC,
		      Sum(If(SLOCType='Bourne Shell' or SLOCType='DOS Batch', SLOC, 0)) NumShellSLOC,
		      GROUP_CONCAT(Filepath SEPARATOR '\n') Filepaths
		FROM CVEFixResults GROUP BY Component, UtilComponent
;
