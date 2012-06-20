library(RODBC)
library(lattice)
conn <- odbcConnect("tomcathistory", uid="tomcathistory", pwd="tomcathistory", case="tolower")

cve <- sqlQuery(conn, "SELECT * FROM CVEResults")
fixchurn <- sqlQuery(conn, "SELECT * FROM CVEFixChurn")
files <- sqlQuery(conn, "SELECT * FROM FileResults")

# What percentage of the fixes are new code?
length(cve$FixNewCode[cve$FixNewCode=="Yes"]) / length(cve$FixNewCode)

# What is the general size of a fix?
mean(fixchurn$JavaChurn[fixchurn$JavaChurn!=0])
median(fixchurn$JavaChurn[fixchurn$JavaChurn!=0])
mean(fixchurn$JavaRelChurn[fixchurn$JavaChurn!=0])
median(fixchurn$JavaRelChurn[fixchurn$JavaChurn!=0])

mean(fixchurn$JSPChurn[fixchurn$JSPChurn!=0])
median(fixchurn$JSPChurn[fixchurn$JSPChurn!=0])
mean(fixchurn$JSPRelChurn[fixchurn$JSPChurn!=0])
median(fixchurn$JSPRelChurn[fixchurn$JSPChurn!=0])

# What percentage of the vulnerabilities were cascading?
length(cve$Cascades[cve$Cascades=="Yes"]) / length(cve$Cascades)
mean(cve$CVSS[cve$Cascades=="Yes"])
mean(cve$CVSS[cve$Cascades=="No"])
wilcox.test(cve$CVSS[cve$Cascades=="Yes"],cve$CVSS[cve$Cascades=="No"])

# What percentage of the vulnerabilities involved input validation?
length(cve$InputValidation[cve$InputValidation=="Yes"]) / length(cve$InputValidation)
mean(cve$CVSS[cve$InputValidation=="Yes"])
mean(cve$CVSS[cve$InputValidation=="No"])
wilcox.test(cve$CVSS[cve$InputValidation=="Yes"],cve$CVSS[cve$InputValidation=="No"])

# What percentage of the vulnerabilities involved output cleansing?
length(cve$OutputCleansing[cve$OutputCleansing=="Yes"]) / length(cve$OutputCleansing)

# What percentage of the vulnerabilities did not involve IO, but were better logic?
length(cve$NonIOImprovedLogic[cve$NonIOImprovedLogic=="Yes"]) / length(cve$NonIOImprovedLogic)

# What percentage of the vulnerabilities were domain specific?
length(cve$DomainSpecific[cve$DomainSpecific=="Yes"]) / length(cve$DomainSpecific)
mean(cve$CVSS[cve$DomainSpecific=="Yes"])
mean(cve$CVSS[cve$DomainSpecific=="No"])
wilcox.test(cve$CVSS[cve$DomainSpecific=="Yes"],cve$CVSS[cve$DomainSpecific=="No"])

# What percentage of the vulnerabilities were CWE Top 25?
length(cve$CWETop25[cve$CWETop25=="Yes"]) / length(cve$CWETop25)
mean(cve$CVSS[cve$CWETop25=="Yes"])
mean(cve$CVSS[cve$CWETop25=="No"])
wilcox.test(cve$CVSS[cve$CWETop25=="Yes"],cve$CVSS[cve$CWETop25=="No"])

# What percentage of the vulnerabilities were not even defined in the CWE?
length(cve$CWE[cve$CWE=="Not Defined"]) / length(cve$CWE)

# Are CWETop25 vulnerabilities statistically different than domain-specific?
table(cve$CWETop25, cve$DomainSpecific)
chisq.test(cve$CWETop25, cve$DomainSpecific)

# How many vulnerabilities had source code files?
length(cve$SourceCode[cve$SourceCode=="Yes"]) / length(cve$SourceCode)

# How many vulnerabilities had config files?
length(cve$ConfigFile[cve$ConfigFile=="Yes"]) / length(cve$ConfigFile)

# Source Code x Config File
table(cve$SourceCode,cve$ConfigFile)

# Average severity for a config file vulnerability
mean(cve$CVSS[cve$ConfigFile=="Yes"])
mean(cve$CVSS[cve$ConfigFile=="No"])
wilcox.test(cve$CVSS[cve$ConfigFile=="Yes"],cve$CVSS[cve$ConfigFile=="No"])

# Do vulnerable files have more SLOC than neutral files?
## Java only
javaFiles <- sqlQuery(conn, "SELECT * FROM FileResults WHERE SLOCType='Java'")
mean(javaFiles$SLOC[javaFiles$vuln=="vulnerable"], na.rm=TRUE)
mean(javaFiles$SLOC[javaFiles$vuln=="neutral"], na.rm=TRUE)
wilcox.test(javaFiles$SLOC[javaFiles$vuln=="vulnerable"], javaFiles$SLOC[javaFiles$vuln=="neutral"])

## JSP only
jspFiles <- sqlQuery(conn, "SELECT * FROM FileResults WHERE SLOCType='JSP'")
mean(jspFiles$SLOC[jspFiles$vuln=="vulnerable"], na.rm=TRUE)
mean(jspFiles$SLOC[jspFiles$vuln=="neutral"], na.rm=TRUE)
wilcox.test(jspFiles$SLOC[jspFiles$vuln=="vulnerable"], jspFiles$SLOC[jspFiles$vuln=="neutral"])


#odbcClose(conn)
#rm(conn)