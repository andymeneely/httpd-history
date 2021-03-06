setwd("D:/Development/httpd-data/churn/files")
files <- list.files(path=".")
for( i in seq_along(files)){
file <- read.csv(files[i],head=TRUE,sep=",")
p1 <- data.frame(x=file$interval, y=file$roc)
p2 <- data.frame(x=file$interval, y=file$vcc)
p3 <- data.frame(x=file$interval, y=file$vccCount)
test <- melt(list(p1=p1, p2=p2, p3=p3), id.vars="x")
ggplot(test, aes(x, y=value, colour=L1, label=value)) + geom_point(data=test[test$L1=="p2",], size=6) + geom_line(data=test[test$L1!="p2", ]) + scale_color_manual(values = c("p1" = "blue", "p2" = "red", "p3" = "black")) + geom_point(data=test[test$L1=="p3",], size=5) + geom_text(data=test[test$L1=="p3",], hjust=0, vjust=0, size=10, aes(label=value)) + opts(legend.position="none") + xlab("Interval") + ylab("Churn Rate")
path <- file.path("D:/Development/httpd-data/churn/plots", paste(files[i], ".png", sep = ""))
ggsave(path, width=19.17, height=10.79)
}
setwd("D:/Development/httpd-data/churn/")

file <- read.csv("30his.txt", head=TRUE, sep=",")
p1 <- data.frame(x=file$x, y=file$y)
p2 <- data.frame(x=c(49, 7, 23), y=c("+ROC","-ROC","=ROC"))
ggplot(p1, aes(x=x, fill=y)) + geom_histogram(alpha=.5, position="identity") + xlab("VCC Count") + geom_vline(data=p2, aes(xintercept=x, colour=p2$y), linetype="dashed", size=1)
ggsave("D:/Development/httpd-data/churn/30his.png", width=19.17, height=10.79)

file <- read.csv("60his.txt", head=TRUE, sep=",")
p1 <- data.frame(x=file$x, y=file$y)
p2 <- data.frame(x=c(43, 7, 23), y=c("+ROC","-ROC","=ROC"))
ggplot(p1, aes(x=x, fill=y)) + geom_histogram(alpha=.5, position="identity") + xlab("VCC Count") + geom_vline(data=p2, aes(xintercept=x, colour=p2$y), linetype="dashed", size=1)
ggsave("D:/Development/httpd-data/churn/60his.png", width=19.17, height=10.79)

file <- read.csv("90his.txt", head=TRUE, sep=",")
p1 <- data.frame(x=file$x, y=file$y)
p2 <- data.frame(x=c(37, 9, 23), y=c("+ROC","-ROC","=ROC"))
ggplot(p1, aes(x=x, fill=y)) + geom_histogram(alpha=.5, position="identity") + xlab("VCC Count") + geom_vline(data=p2, aes(xintercept=x, colour=p2$y), linetype="dashed", size=1)
ggsave("D:/Development/httpd-data/churn/90his.png", width=19.17, height=10.79)