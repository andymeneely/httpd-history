projectchurn <- read.csv(file="D:/Development/httpd-data/project-churn.csv",head=TRUE,sep=",")
x <- projectchurn$interval
y <- projectchurn$churn
plot(x,y, type="o")


