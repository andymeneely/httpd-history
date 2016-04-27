# Clear
rm(list = ls())
cat("\014")

# Check for Database Connection Settings
if(!file.exists("db.settings.R")){
  stop(sprintf("db.settings.R file not found."))
}

# Include Libraries
source("db.settings.R")
source("includes.R")
source("library.R")

# Initialize Libraries
init.libraries()

#### Query Data
query <- "
  SELECT release,
    sloc,
    becomes_vulnerable,
    num_bugs,
    num_enhancements,
    num_regression_bugs,
    cvss_score
  FROM file
  WHERE path LIKE '%.c' OR path LIKE '%h'
  ORDER BY release ASC
"

db.connection <- get.db.connection(db.settings)
dataset <- dbGetQuery(db.connection, query)
dbDisconnect(db.connection)

## SQLite Hack
dataset$becomes_vulnerable <- as.logical(dataset$becomes_vulnerable)

cat("#################### 2.2.0 ####################\n")
curr.release <- dataset[dataset$release == "2.2.0",]
next.release <- dataset[dataset$release == "2.4.0",]
analyze.dataset(curr.release, next.release)
analyze.severity(curr.release)

cat("#################### 2.4.0 ####################\n")
curr.release <- dataset[dataset$release == "2.4.0",]
next.release <- dataset[dataset$release == "2.4.0",]
analyze.dataset(curr.release, next.release)
analyze.severity(curr.release)

cat("#################### Overall ####################\n")
analyze.dataset(dataset)
analyze.severity(dataset)
