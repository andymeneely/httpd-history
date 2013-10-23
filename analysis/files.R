setwd("D:/Development/httpd-data/churn")
files <- list.files(path=".")
for( i in seq_along(files)){
file <- read.csv(files[i])
x <- file$interval
y <- file$churn
png(filename=paste(i,".png"), width=1920, height=1080)
plot(x,y, type="o", ylab="Churn", xlab="Interval")
dev.off()
}
