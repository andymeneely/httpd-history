source('includes.R')

model.release <- function(training.set, testing.set){
  # Logistic Regression Analysis
  
  testing.set <- filter.dataset(testing.set, filter.type = "bug")
  testing.set <- transform.dataset(testing.set)
  
  # Control
  fit.formula = formula(becomes_vulnerable ~ sloc)
  ### Model Summary
  fit.control <- build.model(fit.formula, training.set)
  ### Model Performance
  fit.control.performance <- analyze.performance(
    fit.control, testing.set
  )
  
  # Bugs
  fit.formula = formula(
    becomes_vulnerable ~ sloc + num_bugs
  )
  fit.bugs <- build.model(fit.formula, training.set)
  fit.bugs.performance <-  analyze.performance(
    fit.bugs, testing.set
  )
  
  # Bugs: Enhacements
  fit.formula = formula(
    becomes_vulnerable ~ sloc + num_enhancements
  )
  fit.enhancements <- build.model(fit.formula, training.set)
  fit.enhancements.performance <-  analyze.performance(
    fit.enhancements, testing.set
  )
  
  # Bugs: Enhacements
  fit.formula = formula(
    becomes_vulnerable ~ sloc + num_regression_bugs
  )
  fit.regression <- build.model(fit.formula, training.set)
  fit.regression.performance <- analyze.performance(
    fit.regression, testing.set
  )
  
  cat("##########  CONTROL\n\n")
  
  cat("##########  SUMMARY\n")
  print(summary(fit.control))
  cat("##########  PERFORMANCE\n")
  print(data.frame(fit.control.performance))
  cat("##########  DEVIANCE\n")
  print(Dsquared(model = fit.control))
  cat("\n#################################################\n")
  
  cat("##########  BUG MODELS\n\n")
  
  cat("##########  SUMMARY\n")
  print.summary(fit.bugs)
  cat("##########  PERFORMANCE\n")
  print(data.frame(fit.bugs.performance))
  cat("##########  DEVIANCE\n")
  print(Dsquared(model = fit.bugs))
  cat("\n#################################################\n")
  
  cat("##########  SUMMARY\n")
  print.summary(fit.enhancements)
  cat("##########  PERFORMANCE\n")
  print(data.frame(fit.enhancements.performance))
  cat("##########  DEVIANCE\n")
  print(Dsquared(model = fit.enhancements))
  cat("\n#################################################\n")
  
  cat("##########  SUMMARY\n")
  print.summary(fit.regression)
  cat("##########  PERFORMANCE\n")
  print(data.frame(fit.regression.performance))
  cat("##########  DEVIANCE\n")
  print(Dsquared(model = fit.regression))
  cat("\n#################################################\n")
}

model.overall <- function(training.set){
  ## Logistic Regression Analysis
  
  # Control
  fit.formula = formula(becomes_vulnerable ~ release + sloc)
  ### Model Summary
  fit.control <- build.model(fit.formula, training.set)
  ### Model Performance
  fit.control.performance <- run.kfolds(
    fit.formula, training.set, "becomes_vulnerable", 10, 10
  )
  
  # Bugs
  fit.formula = formula(
    becomes_vulnerable ~ release + sloc + num_bugs
  )
  fit.bugs <- build.model(fit.formula, training.set)
  fit.bugs.performance <- run.kfolds(
    fit.formula, training.set, "becomes_vulnerable", 10, 10
  )
  
  # Bugs: Enhacements
  fit.formula = formula(
    becomes_vulnerable ~ release + sloc + num_enhancements
  )
  fit.enhancements <- build.model(fit.formula, training.set)
  fit.enhancements.performance <- run.kfolds(
    fit.formula, training.set, "becomes_vulnerable", 10, 10
  )
  
  # Bugs: Enhacements
  fit.formula = formula(
    becomes_vulnerable ~ release + sloc + num_regression_bugs
  )
  fit.regression <- build.model(fit.formula, training.set)
  fit.regression.performance <- run.kfolds(
    fit.formula, training.set, "becomes_vulnerable", 10, 10
  )
  
  cat("##########  CONTROL\n\n")
  
  cat("##########  SUMMARY\n")
  print(summary(fit.control))
  cat("##########  PERFORMANCE\n")
  print(data.frame(fit.control.performance))
  cat("##########  DEVIANCE\n")
  print(Dsquared(model = fit.control))
  cat("\n#################################################\n")
  
  cat("##########  BUG MODELS\n\n")
  
  cat("##########  SUMMARY\n")
  print.summary(fit.bugs)
  cat("##########  PERFORMANCE\n")
  print(data.frame(fit.bugs.performance))
  cat("##########  DEVIANCE\n")
  print(Dsquared(model = fit.bugs))
  cat("\n#################################################\n")
  
  cat("##########  SUMMARY\n")
  print.summary(fit.enhancements)
  cat("##########  PERFORMANCE\n")
  print(data.frame(fit.enhancements.performance))
  cat("##########  DEVIANCE\n")
  print(Dsquared(model = fit.enhancements))
  cat("\n#################################################\n")
  
  cat("##########  SUMMARY\n")
  print.summary(fit.regression)
  cat("##########  PERFORMANCE\n")
  print(data.frame(fit.regression.performance))
  cat("##########  DEVIANCE\n")
  print(Dsquared(model = fit.regression))
  cat("\n#################################################\n")
}

analyze.dataset <- function(training.set, testing.set = NULL){
  training.set <- filter.dataset(training.set, filter.type = "bug")
  
  cat("    ############################\n")
  cat("        DATA SUMMARY\n")
  cat("    ############################\n")
  print(dim(training.set))
  print(summary(training.set))
  
  cat("    ############################\n")
  cat("        CORRELATION\n")
  cat("    ############################\n")
  print(round(cor(training.set[,c(4:6)], method = "spearman"), 4))

  # Split Populations
  vuln <- training.set[training.set$becomes_vulnerable == TRUE,]
  neut <- training.set[training.set$becomes_vulnerable == FALSE,]
  
  cat("    ############################\n")
  cat("        VULNERABLE POPULATION\n")
  cat("    ############################\n")
  print(
    cbind(
      Total = length(training.set[,1]),
      Neutral = length(neut[,1]),
      Vulnerable = length(vuln[,1]),
      Percentage = (length(vuln[,1]) / length(neut[,1])) * 100
    )
  )
  cat("    ############################\n")
  cat("        MANN WHITNEY WILCOXON\n")
  cat("    ############################\n")
  run.wilcox(vuln, neut, "num_bugs")
  run.wilcox(vuln, neut, "sloc")
  
  # Log Transform Data
  training.set <- transform.dataset(training.set)
  # Update Split Populations
  vuln <- training.set[training.set$becomes_vulnerable == TRUE,]
  neut <- training.set[training.set$becomes_vulnerable == FALSE,]
  
  cat("    ############################\n")
  cat("        COHEN'S D\n")
  cat("    ############################\n")
  cat("num_bugs\n")
  print(cohensD(vuln$num_bugs, neut$num_bugs))
  cat("SLOC\n")
  print(cohensD(vuln$sloc, neut$sloc))
  
  if(is.null(testing.set)){
    model.overall(training.set)
  } else {
    model.release(training.set, testing.set)
  }
}

analyze.severity <- function(dataset){
  cat("    ############################\n")
  cat("        SEVERITY\n")
  cat("    ############################\n")
  print(cor.test(
    dataset$cvss_score, dataset$num_bugs,
    method = "spearman", na.action = "na.exclude", exact = F
  ))
}