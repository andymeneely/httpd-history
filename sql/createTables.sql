DROP VIEW IF EXISTS RepoLog;
DROP TABLE IF EXISTS SVNLog;
DROP TABLE IF EXISTS SVNLogFiles;
DROP TABLE IF EXISTS Filepaths;
DROP TABLE IF EXISTS CVE;
DROP TABLE IF EXISTS CVESVNFix;
DROP TABLE IF EXISTS CVEGroundedTheory;
DROP TABLE IF EXISTS CVEGroundedTheoryAssets;

CREATE TABLE SVNLog (
  ID int(10) unsigned NOT NULL auto_increment,
  Revision VARCHAR(20) NOT NULL,
  AuthorName varchar(45) default NULL,
  AuthorDate TIMESTAMP,
  Message longtext,
  PRIMARY KEY  (ID)
)ENGINE=MyISAM;

CREATE TABLE SVNLogFiles (
  ID int(10) unsigned NOT NULL auto_increment,
  Revision VARCHAR(20) NOT NULL,
  Filepath varchar(500) NOT NULL,
  Action varchar(1),
  NumChanges int(10) unsigned,
  LinesInserted int(10) unsigned,
  LinesDeleted int(10) unsigned,
  LinesNew int(10) unsigned,
  PRIMARY KEY  (`ID`)
) ENGINE=MyISAM;

CREATE VIEW RepoLog AS
	SELECT l.id, l.revision, l.authorname, l.authordate, l.message, lf.filepath, lf.Action 
	FROM SVNLog l, SVNLogFiles lf
  		WHERE lf.revision=l.revision;

CREATE TABLE CVE (
  ID int(10) unsigned NOT NULL auto_increment,
  CVE VARCHAR(15) NOT NULL,
  CWE VARCHAR(25) NOT NULL,
  CWETop25 ENUM('Yes', 'No') NOT NULL,
  CVSS DOUBLE NOT NULL,
  ConfidentialityImpact VARCHAR(10) NOT NULL,
  IntegrityImpact VARCHAR(10) NOT NULL,
  AvailabilityImpact VARCHAR(10) NOT NULL,
  AccessComplexity VARCHAR(10) NOT NULL,
  AuthRequired VARCHAR(100) NOT NULL,
  GainedAccess VARCHAR(10) NOT NULL,
  PRIMARY KEY  (`ID`)
) ENGINE=MyISAM;

CREATE TABLE CVEGroundedTheory (
  ID int(10) unsigned NOT NULL auto_increment,
  CVE VARCHAR(15) NOT NULL,
  FixNewCode ENUM('Yes', 'No') NOT NULL,
  Cascades ENUM('Yes', 'No') NOT NULL,
  InputValidation ENUM('Yes', 'No') NOT NULL,
  OutputCleansing ENUM('Yes', 'No') NOT NULL,
  NonIOImprovedLogic ENUM('Yes', 'No') NOT NULL,
  DomainSpecific ENUM('Yes', 'No') NOT NULL,
  Regression ENUM('Yes', 'No') NOT NULL,
  SourceCode ENUM('Yes', 'No') NOT NULL,
  ConfigFile ENUM('Yes', 'No') NOT NULL,
  PRIMARY KEY  (`ID`)
) ENGINE=MyISAM;

CREATE TABLE CVEGroundedTheoryAssets (
  ID int(10) unsigned NOT NULL auto_increment,
  CVE VARCHAR(15) NOT NULL,
  Asset VARCHAR(50) NOT NULL,
  PRIMARY KEY  (`ID`)
) ENGINE=MyISAM;

CREATE TABLE CVESVNFix (
  ID int(10) unsigned NOT NULL auto_increment,
  CVE VARCHAR(15) NOT NULL,
  SVNRevision INTEGER,
  PRIMARY KEY  (`ID`)
) ENGINE=MyISAM;

CREATE TABLE Filepaths (
  ID int(10) unsigned NOT NULL auto_increment,
  Filepath varchar(500) NOT NULL,
  HTTPDRelease varchar(5) NOT NULL,
  SLOCType VARCHAR(100),
  SLOC INTEGER,
  PRIMARY KEY  (`ID`)
) ENGINE=MyISAM;