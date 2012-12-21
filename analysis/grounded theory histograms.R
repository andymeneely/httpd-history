library(RODBC)
library(lattice)
conn <- odbcConnect("tomcathistory", uid="tomcathistory", pwd="tomcathistory", case="tolower")

cve <- sqlQuery(conn, "SELECT * FROM CVEResults")
fixchurn <- sqlQuery(conn, "SELECT * FROM CVEFixChurn")
files <- sqlQuery(conn, "SELECT * FROM FileResults")

#type="count", col="red", main="Vulnerable NumDevs", freq=TRUE, xlab="Number of Developers"

hist(fixchurn$JavaChurn[fixchurn$JavaChurn!=0], breaks=20, col="red", main="Java Code Fix Churn Histogram", freq=TRUE, xlab="Lines of Java Code, Added+Deleted", labels=TRUE, axes=FALSE) 
axis(2) 
axis(1, at=seq(0,500,20))

hist(cve$CVSS, breaks=10, col="blue", main="CVSS Severity", freq=TRUE, xlab="CVSS Score", labels=TRUE, axes=FALSE) 
axis(2) 
axis(1, at=seq(0,10,1))


#hist(fixchurn$JSPChurn[fixchurn$JSPChurn!=0], breaks=4, col="blue", main="Histogram of JSP Code Fix Churn ", freq=TRUE, xlab="Lines of JSP Code, Added+Deleted", labels=TRUE, axes=TRUE) 
#axis(2) 
#axis(1, at=seq(0,500,20))