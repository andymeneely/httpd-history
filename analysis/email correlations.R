library(RODBC)
library(lattice)
conn <- odbcConnect("MailingList", uid="student")
email <- sqlQuery(conn, "SELECT * FROM `email`")

association <- function(vulnerable, neutral){
  cat("Vuln. Mean:\t",mean(vulnerable, na.rm=TRUE),"\n")
  cat("Neutral Mean:\t",mean(neutral, na.rm=TRUE),"\n")
  wilcox.test(vulnerable,neutral)
}

cat("Number of responses")
repliesVCC <- email$repliesCount[email$VCC==1]
repliesNonVCC <- email$repliesCount[email$VCC==0]
association(repliesVCC,repliesNonVCC)


cat("Number of responders")
respondersVCC <- email$respondersCount[email$VCC==1]
respondersNonVCC <- email$respondersCount[email$VCC==0]
association(respondersVCC ,respondersNonVCC)