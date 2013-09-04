DROP TABLE IF EXISTS GitLog;
DROP TABLE IF EXISTS GitLogFiles;
DROP TABLE IF EXISTS GitChurnAuthorsAffected;
DROP TABLE IF EXISTS GitChurnEffectiveAuthors;
DROP TABLE IF EXISTS Filepaths;
DROP TABLE IF EXISTS ReleaseHistory;
DROP TABLE IF EXISTS Components;
DROP TABLE IF EXISTS CVE;
DROP TABLE IF EXISTS CVEToGit;
DROP TABLE IF EXISTS CVEGroundedTheory;
DROP TABLE IF EXISTS Timeline;
DROP TABLE IF EXISTS Counterparts;

CREATE TABLE GitLog (
  ID int(10) unsigned NOT NULL auto_increment,
  Commit VARCHAR(40) NOT NULL,
  Parent VARCHAR(81) NOT NULL,
  AuthorName varchar(45) default NULL,
  AuthorEmail varchar(45) default NULL,
  AuthorDate TIMESTAMP DEFAULT 0,
  Subject VARCHAR(5000) NOT NULL,
  Body longtext NOT NULL,
  NumSignedOffBys INTEGER DEFAULT 0,
  ReleaseVer VARCHAR(15), 
  PRIMARY KEY  (ID)
)ENGINE=MyISAM;

CREATE TABLE GitLogFiles (
  ID int(10) unsigned NOT NULL auto_increment,
  Commit VARCHAR(40) NOT NULL,
  Filepath varchar(50) NOT NULL,
  RecentChurn int(10) unsigned,
  ProjectChurn int(10) unsigned,
  LinesInserted int(10) unsigned,
  LinesDeleted int(10) unsigned,
  LinesDeletedSelf int(10) unsigned,
  LinesDeletedOther int(10) unsigned,
  RecentPercIntChurn double,
  NumAuthorsAffected int(10) unsigned,
  NumEffectiveAuthors int(10) unsigned,
  NewEffectiveAuthor ENUM('Yes', 'No'),
  Component varchar(40), 
  ComponentChurn int(10) unsigned,
  PEACh DOUBLE,
  RecentAuthorsAffected int(10) unsigned,
  LinesOfCode int(10) unsigned,
  KnownPastVulnerable ENUM('Yes','No'),
  PRIMARY KEY  (`ID`)
) ENGINE=MyISAM;

CREATE TABLE GitChurnAuthorsAffected(
  ID int(10) unsigned NOT NULL auto_increment,
  Commit VARCHAR(40) NOT NULL,
  Filepath varchar(100) NOT NULL,
  AuthorAffected VARCHAR(40) NOT NULL,
  PRIMARY KEY  (`ID`)
) ENGINE=MyISAM;

CREATE TABLE GitChurnEffectiveAuthors(
  ID int(10) unsigned NOT NULL auto_increment,
  Commit VARCHAR(40) NOT NULL,
  Filepath varchar(100) NOT NULL,
  EffectiveAuthor VARCHAR(40) NOT NULL,
  PRIMARY KEY  (`ID`)
) ENGINE=MyISAM;

CREATE TABLE ReleaseHistory(
	ID int(10) unsigned NOT NULL auto_increment,
	ReleaseVer	VARCHAR(15) NOT NULL,
	ReleaseDate TIMESTAMP NOT NULL DEFAULT 0,
	PRIMARY KEY  (ID)
) ENGINE=MyISAM;

CREATE TABLE Components(
	ID int(10) unsigned NOT NULL auto_increment,
	ComponentPath VARCHAR(40),
	PRIMARY KEY  (ID)
) ENGINE=MyISAM;

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
  ExceptionHandling ENUM('Yes', 'No') NOT NULL,
  Regression ENUM('Yes', 'No') NOT NULL,
  SourceCode ENUM('Yes', 'No') NOT NULL,
  ConfigFile ENUM('Yes', 'No') NOT NULL,
  PRIMARY KEY  (`ID`)
) ENGINE=MyISAM;

CREATE TABLE CVEToGit (
  ID int(10) unsigned NOT NULL auto_increment,
  CVE VARCHAR(15) NOT NULL,
  Filepath VARCHAR(50) NOT NULL,
  CommitIntroduced VARCHAR(40) NOT NULL,
  CommitFixed VARCHAR(40) NOT NULL,
  IsBaselineCommit ENUM('Yes', 'No'),
  PRIMARY KEY  (`ID`)
) ENGINE=MyISAM;

CREATE TABLE Filepaths (
  ID int(10) unsigned NOT NULL auto_increment,
  Filepath varchar(100) NOT NULL,
  HTTPDRelease varchar(5) NOT NULL,
  SLOCType VARCHAR(100),
  SLOC INTEGER,
  PRIMARY KEY  (`ID`)
) ENGINE=MyISAM;

CREATE TABLE Timeline(
  ID int(10) unsigned NOT NULL auto_increment,
  Filepath VARCHAR(50) NOT NULL,
  NumCVEs INTEGER NOT NULL,
  AtTime TIMESTAMP NOT NULL DEFAULT 0,
  CVEs TEXT NOT NULL,
  PRIMARY KEY  (`ID`)
) ENGINE=MyISAM;

CREATE TABLE Counterparts(
  ID int(10) unsigned NOT NULL auto_increment,
  Commit VARCHAR(40) NOT NULL,
  Counterpart VARCHAR(40) NOT NULL,
  PRIMARY KEY  (`ID`)
) ENGINE=MyISAM;
