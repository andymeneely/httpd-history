init.libraries <- function(){
  suppressPackageStartupMessages(library("DBI"))
  suppressPackageStartupMessages(library("ggplot2"))
  suppressPackageStartupMessages(library("gridExtra"))
  suppressPackageStartupMessages(library("lsr"))
  suppressPackageStartupMessages(library("reshape2"))
  suppressPackageStartupMessages(library("ROCR"))
}

get.db.connection <- function(db.settings){
  connection <- db.connect(
    provider = db.settings$default$provider,
    host = db.settings$default$host, port = db.settings$default$port,
    user = db.settings$default$user, password = db.settings$default$password,
    dbname = db.settings$default$dbname
  )
  return(connection)
}

db.connect <- function(provider, host, port, user, password, dbname){
  connection <- NULL

  if(provider == "PostgreSQL"){
    library("RPostgreSQL")
    connection <- dbConnect(
      dbDriver(provider),
      host = host, port = port, user = user, password = password, dbname = dbname
    )
  } else if(provider == "MySQL"){
    library("RMySQL")
    connection <- dbConnect(
      dbDriver(provider),
      host = host, port = port, user = user, password = password, dbname = dbname
    )
  } else if(provider == "SQLite"){
    library("RSQLite")
    connection <- dbConnect(
      dbDriver(provider), dbname = dbname
    )
  } else {
    stop(sprintf("Database provider %s not supported.", provider))
  }

  return(connection)
}

db.disconnect <- function(connection){
  return(dbDisconnect(connection))
}

db.get.data <- function(connection, query){
  return(dbGetQuery(connection, query))
}

run.wilcox <- function(vulnerable, neutral, metric){
  cat(paste("Metric: ", metric, "\n"))

  vuln <- vulnerable[[metric]]
  neut <- neutral[[metric]]

  print(wilcox.test(vuln, neut))
  print(
    cbind(
      vuln.median = median(vuln, na.rm=TRUE),
      neut.median = median(neut, na.rm=TRUE)
    )
  )
  print(
    cbind(
      vuln.mean = mean(vuln, na.rm=TRUE),
      neut.mean = mean(neut, na.rm=TRUE)
    )
  )
}

build.model <- function(formula, dataset){
  model <- glm(formula = formula, data = dataset, family = "binomial")
  return(model)
}

print.summary <- function(model){
  print(model$formula)
  print(summary(model))
}

compute.fmeasure <- function(precision, recall, beta = 1){
    return(
      ((1 + beta ^ 2) * precision * recall)
      /
        ((beta ^ 2 * precision) + recall)
    )
}

aggregate.performance <- function(measures){
  precision <- numeric(length(measures))
  recall <- numeric(length(measures))
  auc <- numeric(length(measures))

  index <- 1
  for(measure in measures){
    precision[index] <- measure$mean_precision
    recall[index] <- measure$mean_recall
    auc[index] <- measure$auc
    index <- index + 1
  }

  avg_precision <- mean(precision)
  avg_recall <- mean(recall)
  fmeasure <- compute.fmeasure(avg_precision, avg_recall)
  avg_auc <- mean(auc)

  performance <- list(
    "avg_precision" = avg_precision, "avg_recall" = avg_recall,
    "fmeasure" = fmeasure, "avg_auc" = avg_auc
  )

  return(performance)
}

get.kfolds <- function(dataset, switch, k){
  # Split the population into Neutral and Vulnerable sub-populations
  neut <- dataset[dataset[switch] == FALSE,]
  vuln <- dataset[dataset[switch] == TRUE,]
  # Count the number of Neutral and Vulnerable observations per fold
  #   The proportion of neutral and vulnerable observations must be kept the
  #   same in each fold
  fold.num.neut <- ceiling(nrow(neut) / k)
  fold.num.vuln <- ceiling(nrow(vuln) / k)
  # Randomization
  random.neut.indices <- sample(1:nrow(neut))
  random.vuln.indices <- sample(1:nrow(vuln))

  # Sub-sample indices
  fold.neut.beg <- 1
  fold.neut.end <- fold.num.neut
  fold.vuln.beg <- 1
  fold.vuln.end <- fold.num.vuln

  folds <- list()
  for(index in 1:k){
    folds[[index]] <- rbind(
      neut[random.neut.indices[fold.neut.beg:fold.neut.end],],
      vuln[random.vuln.indices[fold.vuln.beg:fold.vuln.end],]
    )

    fold.neut.beg <- fold.neut.end + 1
    fold.neut.end <- min(nrow(neut), (fold.neut.end + fold.num.neut))
    fold.vuln.beg <- fold.vuln.end + 1
    fold.vuln.end <- min(nrow(vuln), (fold.vuln.end + fold.num.vuln))
  }

  return(folds)
}

split.kfolds <- function(folds, testing.fold){
  training <- data.frame()
  testing <- NA
  for(index in 1:length(folds)){
    if(index == testing.fold){
      testing <- folds[[index]]
      next
    }
    training <- rbind(training, folds[[index]])
  }
  return(list("training" = training, "testing" = testing))
}

run.kfolds <- function(formula, dataset, switch, k, n){
  performance <- vector(mode = "list", length = k * n)
  index <- 1
  for(iteration in 1:n){
    folds <- get.kfolds(dataset, switch, k)
    for(fold in 1:k){
      fold.dataset <- split.kfolds(folds, testing.fold = fold)
      model <- build.model(formula, fold.dataset$training)
      performance[[index]] <- analyze.performance(model, fold.dataset$testing)
      index <- index + 1
    }
  }

  return(aggregate.performance(performance))
}

filter.dataset <- function(dataset, filter.type){
  if(filter.type == "bug"){
    dataset <- subset(dataset,
      (
        dataset$num_bugs !=0 |
        dataset$becomes_vulnerable != FALSE
      ) & dataset$sloc > 0
    )
  } else {
    warning("Unknown filter: ", filter.type, ". Data set unaltered.")
  }

  return(dataset)
}

transform.dataset <- function(dataset){
  numeric.columns <- sapply(dataset, is.numeric)
  dataset <- cbind(
    log(dataset[, numeric.columns] + 1),
    dataset[, !numeric.columns]
  )
  return(dataset)
}

Dsquared <-function(obs = NULL, pred = NULL, model = NULL, adjust = FALSE) {
  # version 1.3 (3 Jan 2015)

  model.provided <- ifelse(is.null(model), FALSE, TRUE)

  if (model.provided) {
    if (!("glm" %in% class(model)))
      stop ("'model' must be of class 'glm'.")
    if (!is.null(pred))
      message("Argument 'pred' ignored in favour of 'model'.")
    if (!is.null(obs))
      message("Argument 'obs' ignored in favour of 'model'.")
    obs <- model$y
    pred <- model$fitted.values

  } else { # if model not provided
    if (is.null(obs) | is.null(pred))
      stop("You must provide either 'obs' and 'pred', or a 'model' object of class 'glm'")
    if (length(obs) != length(pred))
      stop ("'obs' and 'pred' must be of the same length (and in the same order).")
    if (!(obs %in% c(0, 1)) | pred < 0 | pred > 1)
      stop ("Sorry, 'obs' and 'pred' options currently only implemented for binomial GLMs (binary response variable with values 0 or 1) with logit link.")
    logit <- log(pred / (1 - pred))
    model <- glm(obs ~ logit, family = "binomial")
  }

  D2 <- (model$null.deviance - model$deviance) / model$null.deviance

  if (adjust) {
    if (!model.provided)
      return(message("Adjusted D-squared not calculated, as it requires a model object (with its number of parameters) rather than just 'obs' and 'pred' values."))

    n <- length(model$fitted.values)
    #p <- length(model$coefficients)
    p <- attributes(logLik(model))$df
    D2 <- 1 - ((n - 1) / (n - p)) * (1 - D2)
  }  # end if adj

  return (D2)
}

analyze.performance <- function(model, testing.set){
  prediction <- prediction(
    predict(model, newdata = testing.set, type = "response"),
    testing.set$becomes_vulnerable
  )
  performance <- performance(prediction, "prec", "rec")
  precision <- unlist(slot(performance, "y.values"))
  recall <- unlist(slot(performance, "x.values"))
  f <- compute.fmeasure(precision, recall)

  mean_precision= mean(precision, na.rm=TRUE)
  mean_recall = mean(recall, na.rm=TRUE)
  mean_f = mean(f, na.rm=TRUE)

  auc <- unlist(slot(performance(prediction, "auc"), "y.values"))

  return (as.data.frame(cbind(mean_precision, mean_recall, mean_f, auc)))
}
